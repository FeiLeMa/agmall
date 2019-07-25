package com.alag.mmall.service.impl;

import com.alag.mmall.common.*;
import com.alag.mmall.mapper.*;
import com.alag.mmall.model.*;
import com.alag.mmall.service.OrderService;
import com.alag.mmall.vo.OrderItemVo;
import com.alag.mmall.vo.OrderProductVo;
import com.alag.mmall.vo.OrderVo;
import com.alag.mmall.vo.ShippingVo;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private PayInfoMapper payInfoMapper;
    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private ShippingMapper shippingMapper;

    @Override
    public ServerResponse createOrder(Integer userId, Integer shippingId) {
        List<Cart> cartList = cartMapper.selectAllCartByUserId(userId);

        ServerResponse serverResponse = this.getOrderItemList(cartList);
        if (!serverResponse.isSuccess()) {
            return serverResponse;
        }

        BigDecimal payment = this.calPayment((List<OrderItem>) serverResponse.getData());

        Order order = this.assembleOrder(userId, shippingId, payment);
        if (order == null) {
            return ServerResponse.createByErrorMessage("创建订单失败");
        }

        List<OrderItem> orderItemList = (List<OrderItem>) serverResponse.getData();
        for (OrderItem orderItem : orderItemList) {
            orderItem.setOrderNo(order.getOrderNo());
        }

        //批量插入Orderitem
        orderItemMapper.batchInsert(orderItemList);

        //减少库存
        this.reduceProductStock(orderItemList);

        //清空购物车
        this.cleanCart(cartList);

        //返回前端OrderVo
        OrderVo orderVo = this.assembleOrderVo(order, orderItemList);

        return ServerResponse.createBySuccess(orderVo);

    }

    @Override
    public ServerResponse canncelOrder(Integer userId, Long orderNo) {
        Order order = orderMapper.selectByOrderNoAndUserId(orderNo, userId);
        if (order == null) {
            return ServerResponse.createByErrorMessage("该用户下没有此订单");
        }
        order.setStatus(Const.OrderStatusEnum.CANCELED.getCode());
        int rowCount = orderMapper.updateByPrimaryKeySelective(order);
        if (rowCount > 0) {
            return ServerResponse.createBySuccessMessage("取消此订单成功");
        }
        return ServerResponse.createByErrorMessage("取消订单失败");
    }

    @Override
    public ServerResponse getCartProduct(Integer userId) {
        List<Cart> cartList = cartMapper.selectCheckedCardByUserId(userId);
        if (cartList == null) {
            return ServerResponse.createByErrorMessage("该用户购物车下没有勾选的商品");
        }
        ServerResponse response = this.getOrderItemList(cartList);
        if (!response.isSuccess()) {
            return response;
        }

        List<OrderItemVo> orderItemVoList = Lists.newArrayList();
        BigDecimal payment = new BigDecimal(0);
        for (OrderItem orderItem : (List<OrderItem>) response.getData()) {
            payment = BigDecimalUtil.add(payment.doubleValue(), orderItem.getTotalPrice().doubleValue());
            orderItemVoList.add(this.assembleOrderItemVo(orderItem));
        }

        OrderProductVo orderProductVo = new OrderProductVo();
        orderProductVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        orderProductVo.setOrderItemVoList(orderItemVoList);
        orderProductVo.setProductTotalPrice(payment);

        return ServerResponse.createBySuccess(orderProductVo);
    }

    @Override
    public ServerResponse getDetail(Integer userId, Long orderNo) {
        Order order = orderMapper.selectByOrderNoAndUserId(orderNo, userId);
        if (null == order) {
            return ServerResponse.createByErrorMessage("没找到该订单");
        }
        List<OrderItem> orderItemList = orderItemMapper.selectByOrderNoAndUserId(orderNo, userId);
        OrderVo orderVo = this.assembleOrderVo(order, orderItemList);

        return ServerResponse.createBySuccess(orderVo);
    }

    @Override
    public ServerResponse list(Integer userId, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Order> orderList = orderMapper.selectByUserId(userId);
        List<OrderVo> orderVoList = this.assembleOrderVoList(orderList);
        PageInfo pageInfo = new PageInfo(orderList);
        pageInfo.setList(orderVoList);

        return ServerResponse.createBySuccess(pageInfo);
    }

    @Override
    public void closeOrder(int hour) {
        Date closeTime = DateTimeUtil.addHours(new Date(), -hour);
        List<Order> needCloseOrderList = orderMapper.selectByTimeAndStatus(closeTime, Const.OrderStatusEnum.NO_PAY.getCode());
        for (Order order : needCloseOrderList) {
            //将订单状态改为交易关闭
            Long orderNo = order.getOrderNo();
            order.setStatus(Const.OrderStatusEnum.ORDER_CLOSE.getCode());
            order.setCloseTime(new Date());
            int row = orderMapper.updateByPrimaryKeySelective(order);
            if (row > 0) {
                log.info("订单{},已被关闭", orderNo);
            }
            List<OrderItem> orderItemList = orderItemMapper.selectProductIdByOrderNo(orderNo);
            for (OrderItem orderItem : orderItemList) {
                Integer id = orderItem.getProductId();
                Integer quantity = orderItem.getQuantity();
                Integer stock = productMapper.selectStockByProductId(id);

                Product product = new Product();
                product.setId(id);
                product.setStock(stock + quantity);

                row = productMapper.updateByPrimaryKeySelective(product);
                if (row > 0) {
                    log.info("产品id{},库存{},已经恢复至{}",id,stock,stock+orderItem.getQuantity());
                }
            }
        }
    }

    private List<OrderVo> assembleOrderVoList(List<Order> orderList) {
        List<OrderVo> orderVoList = Lists.newArrayList();
        for (Order order : orderList) {
            Long orderNo = order.getOrderNo();
            Integer userId = order.getUserId();
            List<OrderItem> orderItemList = orderItemMapper.selectByOrderNoAndUserId(orderNo, userId);
            OrderVo orderVo = this.assembleOrderVo(order, orderItemList);
            orderVoList.add(orderVo);
        }
        return orderVoList;
    }


    private OrderVo assembleOrderVo(Order order, List<OrderItem> orderItemList) {
        OrderVo orderVo = new OrderVo();
        orderVo.setOrderNo(order.getOrderNo());
        orderVo.setPayment(order.getPayment());
        orderVo.setPostage(order.getPostage());
        orderVo.setPaymentType(order.getPaymentType());
        orderVo.setShippingId(order.getShippingId());
        orderVo.setPaymentTypeDesc(Const.PaymentTypeEnum.codeOf(order.getPaymentType()).getValue());

        orderVo.setStatus(order.getStatus());
        orderVo.setStatusDesc(Const.OrderStatusEnum.codeOf(order.getStatus()).getValue());

        Shipping shipping = shippingMapper.selectByPrimaryKey(order.getShippingId());
        if (shipping != null) {
            orderVo.setReceiverName(shipping.getReceiverName());
            orderVo.setShippingVo(this.assembleShippingVo(shipping));
        }

        orderVo.setPaymentTime(DateTimeUtil.dateToStr(order.getPaymentTime()));
        orderVo.setSendTime(DateTimeUtil.dateToStr(order.getSendTime()));
        orderVo.setEndTime(DateTimeUtil.dateToStr(order.getEndTime()));
        orderVo.setCreateTime(DateTimeUtil.dateToStr(order.getCreateTime()));
        orderVo.setCloseTime(DateTimeUtil.dateToStr(order.getCloseTime()));


        orderVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        List<OrderItemVo> orderItemVoList = Lists.newArrayList();

        for (OrderItem orderItem : orderItemList) {
            OrderItemVo orderItemVo = assembleOrderItemVo(orderItem);
            orderItemVoList.add(orderItemVo);
        }
        orderVo.setOrderItemVoList(orderItemVoList);
        return orderVo;

    }

    private OrderItemVo assembleOrderItemVo(OrderItem orderItem) {
        OrderItemVo orderItemVo = new OrderItemVo();
        orderItemVo.setOrderNo(orderItem.getOrderNo());
        orderItemVo.setProductId(orderItem.getProductId());
        orderItemVo.setProductName(orderItem.getProductName());
        orderItemVo.setProductImage(orderItem.getProductImage());
        orderItemVo.setCurrentUnitPrice(orderItem.getCurrentUnitPrice());
        orderItemVo.setQuantity(orderItem.getQuantity());
        orderItemVo.setTotalPrice(orderItem.getTotalPrice());

        orderItemVo.setCreateTime(DateTimeUtil.dateToStr(orderItem.getCreateTime()));
        return orderItemVo;
    }

    private ShippingVo assembleShippingVo(Shipping shipping) {
        ShippingVo shippingVo = new ShippingVo();
        shippingVo.setReceiverName(shipping.getReceiverName());
        shippingVo.setReceiverAddress(shipping.getReceiverAddress());
        shippingVo.setReceiverProvince(shipping.getReceiverProvince());
        shippingVo.setReceiverCity(shipping.getReceiverCity());
        shippingVo.setReceiverDistrict(shipping.getReceiverDistrict());
        shippingVo.setReceiverMobile(shipping.getReceiverMobile());
        shippingVo.setReceiverZip(shipping.getReceiverZip());
        shippingVo.setReceiverPhone(shipping.getReceiverPhone());
        return shippingVo;
    }

    private void cleanCart(List<Cart> cartList) {
        for (Cart cart : cartList) {
            cartMapper.deleteByPrimaryKey(cart.getId());
        }
    }

    private void reduceProductStock(List<OrderItem> orderItemList) {
        for (OrderItem orderItem : orderItemList) {
            Product product = productMapper.selectByPrimaryKey(orderItem.getProductId());
            product.setStock(product.getStock() - orderItem.getQuantity());
            productMapper.updateByPrimaryKeySelective(product);
        }
    }

    private Order assembleOrder(Integer userId, Integer shippingId, BigDecimal payment) {
        Order order = new Order();
        order.setUserId(userId);
        order.setShippingId(shippingId);
        order.setPayment(payment);
        order.setOrderNo(this.generateOrderNo());
        order.setStatus(Const.OrderStatusEnum.NO_PAY.getCode());
        order.setPaymentType(Const.PaymentTypeEnum.ONLINE_PAY.getCode());
        order.setPostage(0);

        int rowCount = orderMapper.insert(order);
        if (rowCount > 0) {
            return order;
        }
        return null;
    }

    private BigDecimal calPayment(List<OrderItem> orderItemList) {
        BigDecimal payment = new BigDecimal("0");
        for (OrderItem orderItem : orderItemList) {
            payment = BigDecimalUtil.add(payment.doubleValue(), orderItem.getTotalPrice().doubleValue());
        }
        return payment;
    }

    private ServerResponse getOrderItemList(List<Cart> cartList) {
        List<OrderItem> orderItemList = Lists.newArrayList();
        for (Cart cartItem : cartList) {
            OrderItem orderItem = new OrderItem();
            Product product = productMapper.selectByPrimaryKey(cartItem.getProductId());
            //检验产品是否下架
            if (product.getStatus() != Const.ProductStatusEnum.ON_SALE.getCode()) {
                return ServerResponse.createByErrorMessage("商品已下架");
            }
            //检验库存
            if (cartItem.getQuantity() > product.getStock()) {
                return ServerResponse.createByErrorMessage("库存不足，请修改数量");
            }

            orderItem.setProductId(cartItem.getProductId());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUserId(cartItem.getUserId());
            orderItem.setProductImage(product.getMainImage());
            orderItem.setProductName(product.getName());
            orderItem.setCurrentUnitPrice(product.getPrice());
            orderItem.setTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(), cartItem.getQuantity()));

            orderItemList.add(orderItem);
        }
        return ServerResponse.createBySuccess(orderItemList);
    }

    private Long generateOrderNo() {
        Long nowTime = System.currentTimeMillis();
        return nowTime + new Random().nextInt(100);
    }


    public ServerResponse<String> pay(Integer userId, Long orderNo) {
        Order order = orderMapper.selectByOrderNoAndUserId(orderNo, userId);
        if (null == order) {
            return ServerResponse.createByErrorMessage("该用户下没此订单");
        }
        List<OrderItem> orderItemList = orderItemMapper.selectByOrderNoAndUserId(orderNo, userId);
        StringBuilder subject = new StringBuilder("");
        BigDecimal totalAmount = new BigDecimal(0);
        for (OrderItem orderItem : orderItemList) {
            String productName = orderItem.getProductName();
            Integer quantity = orderItem.getQuantity();
            BigDecimal totalPrice = orderItem.getTotalPrice();
            log.info(productName + ":" + totalPrice);
            subject.append(productName).append("X").append(quantity).append(" , ");
            totalAmount = BigDecimalUtil.add(totalAmount.doubleValue(), totalPrice.doubleValue());
        }
        log.info("共有商品,{}", subject);
        log.info("总计价格{},", totalAmount);
        AlipayClient alipayClient = new DefaultAlipayClient(
                PropertiesUtil.getProperty("alipay.url"),
                PropertiesUtil.getProperty("alipay.app_id"),
                PropertiesUtil.getProperty("alipay.app_private_key"),
                PropertiesUtil.getProperty("alipay.format"),
                PropertiesUtil.getProperty("alipay.charset"),
                PropertiesUtil.getProperty("alipay.alipay_public_key"),
                PropertiesUtil.getProperty("alipay.sign_type"));
        //创建API对应的request
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        //回跳地址
        alipayRequest.setReturnUrl(PropertiesUtil.getProperty("alipay.return_url"));
        //通知地址
        alipayRequest.setNotifyUrl(PropertiesUtil.getProperty("alipay.notify_url"));
        //业务参数
        alipayRequest.setBizContent("{" +
                "    \"out_trade_no\":\"" + orderNo + "\"," +
                "    \"product_code\":\"FAST_INSTANT_TRADE_PAY\"," +
                "    \"total_amount\":" + totalAmount + "," +
                "    \"subject\":\"" + subject + "\"" +
                "  }");//填充业务参数
        String form = "";
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody();
            log.info(form);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        return ServerResponse.createBySuccess(form);
    }

    public ServerResponse aliCallback(Map<String, String> params) {
        Long orderNo = Long.parseLong(params.get("out_trade_no"));
        String tradeNo = params.get("trade_no");
        String tradeStatus = params.get("trade_status");
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            return ServerResponse.createByErrorMessage("agmmall的订单,回调忽略");
        }
        if (order.getStatus() >= Const.OrderStatusEnum.PAID.getCode()) {
            return ServerResponse.createBySuccess("支付宝重复调用");
        }
        if (Const.AlipayCallback.TRADE_STATUS_TRADE_SUCCESS.equals(tradeStatus)) {
            order.setPaymentTime(DateTimeUtil.strToDate(params.get("gmt_payment")));
            order.setStatus(Const.OrderStatusEnum.PAID.getCode());
            orderMapper.updateByPrimaryKeySelective(order);
        }

        PayInfo payInfo = new PayInfo();
        payInfo.setUserId(order.getUserId());
        payInfo.setOrderNo(order.getOrderNo());
        payInfo.setPayPlatform(Const.PayPlatformEnum.ALIPAY.getCode());
        payInfo.setPlatformNumber(tradeNo);
        payInfo.setPlatformStatus(tradeStatus);

        payInfoMapper.insert(payInfo);

        return ServerResponse.createBySuccess();
    }

    @Override
    public ServerResponse<Boolean> getOrderStatusByOrderNoAndUserId(Integer userId, Long orderNo) {
        Order order = orderMapper.selectByOrderNoAndUserId(orderNo, userId);
        if (order == null) {
            return ServerResponse.createByErrorMessage("没有该订单");
        }
        log.info(order.getStatus().toString());
        if (order.getStatus() >= Const.OrderStatusEnum.PAID.getCode()) {
            return ServerResponse.createBySuccess(true);
        }

        return ServerResponse.createBySuccess(false);
    }
}
