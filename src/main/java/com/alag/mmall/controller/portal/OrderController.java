package com.alag.mmall.controller.portal;

import com.alag.mmall.common.Const;
import com.alag.mmall.common.PropertiesUtil;
import com.alag.mmall.common.ResponseCode;
import com.alag.mmall.common.ServerResponse;
import com.alag.mmall.model.User;
import com.alag.mmall.service.OrderService;
import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Iterator;
import java.util.Map;

@Controller
@RequestMapping("order")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("create")
    @ResponseBody
    public ServerResponse create(HttpSession session,
                                 @RequestParam(value = "shippingId",required = true) Integer shippingId) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "请先登录后在操作");
        }
        return orderService.createOrder(user.getId(),shippingId);
    }

    @PutMapping("canncel")
    @ResponseBody
    public ServerResponse canncel(HttpSession session, Long orderNo) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "请先登录后在操作");
        }
        return orderService.canncelOrder(user.getId(),orderNo);
    }

    /**
     * 没有下单之前的购物车查询
     *
     * @param session
     * @return
     */
    @GetMapping("get_cart_product")
    @ResponseBody
    public ServerResponse getCartProduct(HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "请先登录后在操作");
        }
        return orderService.getCartProduct(user.getId());

    }

    @GetMapping("detail")
    @ResponseBody
    public ServerResponse getDetail(HttpSession session,Long orderNo) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "请先登录后在操作");
        }
        return orderService.getDetail(user.getId(),orderNo);
    }

    @GetMapping("list")
    @ResponseBody
    public ServerResponse list(HttpSession session,
                               @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                               @RequestParam(value = "pageSize", defaultValue = "1") Integer pageSize) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "请先登录后在操作");
        }
        return orderService.list(user.getId(),pageNum,pageSize);
    }













    @RequestMapping("alipay")
    public String pay(Model model, HttpSession session, @RequestParam(value = "orderNo",required = true) Long orderNo) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            model.addAttribute("errCode", ResponseCode.NEED_LOGIN.getCode());
            model.addAttribute("errMsg", "请先登陆上再操作");
        }else {
            ServerResponse<String> formRet = orderService.pay(user.getId(), orderNo);
            model.addAttribute("form", formRet.getData());
        }
        return "toAlipay";
    }

    @RequestMapping("alipay_callback")
    @ResponseBody
    public ServerResponse<Map<String,String>> alipayCallback(HttpServletRequest request) {
        log.info("========================== callbackRequest ===========================");
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, String> retMap = Maps.newHashMap();
        for (String key : parameterMap.keySet()) {
            String[] values = parameterMap.get(key);
            for (int i = 0; i <values.length ; i++) {
                log.info("key:{}---value:{}",key,values[i]);
                retMap.put(key, values[i]);
            }
        }
        return ServerResponse.createBySuccess(retMap);
    }

    @RequestMapping("alipay_notify")
    @ResponseBody
    public Object alipayNotify(HttpServletRequest request) throws AlipayApiException {
        log.info("========================== notifyRequest ===========================");
        Map<String,String> params = Maps.newHashMap();
        Map requestParams = request.getParameterMap();
        for(Iterator iter = requestParams.keySet().iterator(); iter.hasNext();){
            String name = (String)iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for(int i = 0 ; i <values.length;i++){
                valueStr = (i == values.length -1)?valueStr + values[i]:valueStr + values[i]+",";
            }

            log.info("key:{}-----value:{}",name,valueStr);
            params.put(name,valueStr);

        }
        params.remove("sign_type");
        boolean signVerified = AlipaySignature.rsaCheckV2(params, PropertiesUtil.getProperty("alipay.alipay_public_key"), PropertiesUtil.getProperty("alipay.charset"), PropertiesUtil.getProperty("alipay.sign_type")); //调用SDK验证签名
        if(signVerified){
            log.info("验签成功,正在处理业务...");
            ServerResponse response = orderService.aliCallback(params);
            if (response.isSuccess()) {
                log.info("业务处理成功，返回支付宝成功");
                return Const.AlipayCallback.RESPONSE_SUCCESS;
            } else {
                log.info("业务处理失败返回支付宝失败");
                return Const.AlipayCallback.RESPONSE_FAILED;
            }
        }else{
            log.info("验签失败");
            return ServerResponse.createByErrorMessage("非法请求,验证不通过!");
        }
    }

    @RequestMapping("query_order_pay_status")
    @ResponseBody
    public ServerResponse<Boolean> queryOrderPayStatus(HttpSession session, Long orderNo) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }

        ServerResponse response = orderService.getOrderStatusByOrderNoAndUserId(user.getId(), orderNo);
        if (response.isSuccess()) {
            return response;
        }
        return ServerResponse.createBySuccess(false);
    }

}
