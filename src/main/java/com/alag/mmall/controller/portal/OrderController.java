package com.alag.mmall.controller.portal;

import com.alag.mmall.common.Const;
import com.alag.mmall.common.ResponseCode;
import com.alag.mmall.common.ServerResponse;
import com.alag.mmall.model.User;
import com.alag.mmall.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("order")
public class OrderController {

    @Autowired
    private OrderService orderService;


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
}
