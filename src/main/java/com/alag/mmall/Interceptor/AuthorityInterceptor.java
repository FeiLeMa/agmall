package com.alag.mmall.Interceptor;

import com.alag.mmall.common.Const;
import com.alag.mmall.common.ResponseCode;
import com.alag.mmall.common.ServerResponse;
import com.alag.mmall.model.User;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Map;

@Slf4j
public class AuthorityInterceptor implements HandlerInterceptor{
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("preHandle");
        Method method = ((HandlerMethod) handler).getMethod();
        String methodName = method.getName();
        LoginRequired loginRequired = method.getAnnotation(LoginRequired.class);
        if (loginRequired == null) {
            log.info("{},不需要拦截，直接进入Controller！",methodName);
            return true;//进入Controller
        }

        User currentUser = (User) request.getSession().getAttribute(Const.CURRENT_USER);
        PrintWriter writer = null;
        String jsonStr = "";
        if (currentUser != null) {
            if (currentUser.getRole() == Const.Role.ROLE_ADMIN) {
                log.info("用户是管理员");
                return true;
            }
            if (StringUtils.equals(methodName, "richtextImgUpload")) {
                Map resultMap = Maps.newHashMap();
                resultMap.put("success", false);
                resultMap.put("msg", "无权限操作");
                jsonStr = JSON.toJSONString(resultMap);
            } else {
                jsonStr = JSON.toJSONString(ServerResponse.createByErrorMessage("用户无权登录"));
            }
        } else {
            log.info("用户未登录");
            if (StringUtils.equals(methodName, "richtextImgUpload")) {
                Map resultMap = Maps.newHashMap();
                resultMap.put("success", false);
                resultMap.put("msg", "用户未登录");
                jsonStr = JSON.toJSONString(resultMap);
            } else {
                jsonStr = JSON.toJSONString(ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录"));
            }
        }
        response.reset();
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        writer = response.getWriter();
        writer.print(jsonStr);
        writer.flush();
        writer.close();
        return false;//不进入Controller
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        log.info("postHandle");
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        log.info("afterCompletion");
    }
}
