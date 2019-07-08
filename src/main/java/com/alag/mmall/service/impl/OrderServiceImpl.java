package com.alag.mmall.service.impl;

import com.alag.mmall.common.BigDecimalUtil;
import com.alag.mmall.common.PropertiesUtil;
import com.alag.mmall.common.ServerResponse;
import com.alag.mmall.mapper.OrderItemMapper;
import com.alag.mmall.mapper.OrderMapper;
import com.alag.mmall.model.Order;
import com.alag.mmall.model.OrderItem;
import com.alag.mmall.service.OrderService;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    private static Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;

    public ServerResponse<String> pay(Integer userId,Long orderNo) {
        Order order = orderMapper.selectByOrderNoAndUserId(orderNo,userId);
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
            logger.info(productName+":"+totalPrice);
            subject.append(productName).append("X").append(quantity).append(" , ");
            totalAmount = BigDecimalUtil.add(totalAmount.doubleValue(),totalPrice.doubleValue());
        }
        logger.info("共有商品,{}",subject);
        logger.info("总计价格{},",totalAmount);
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
        alipayRequest.setReturnUrl("http://localhost:8080/CallBack/return_url.jsp");
        //通知地址
        alipayRequest.setNotifyUrl("http://localhost:8080/CallBack/notify_url.jsp");
        //业务参数
        alipayRequest.setBizContent("{" +
                "    \"out_trade_no\":\""+orderNo+"\"," +
                "    \"product_code\":\"FAST_INSTANT_TRADE_PAY\"," +
                "    \"total_amount\":"+totalAmount+"," +
                "    \"subject\":\""+subject+"\"" +
                "  }");//填充业务参数
        String form = "";
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody();
            logger.info(form);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        return ServerResponse.createBySuccess(form);
    }
}
