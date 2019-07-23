package com.alag.mmall.controller.backend;

import com.alag.mmall.common.Const;
import com.alag.mmall.common.ServerResponse;
import com.alag.mmall.model.User;
import com.alag.mmall.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/manage/user")
@Slf4j
public class UserManageController {
    @Autowired
    private UserService userService;

    @PostMapping("login")
    public ServerResponse<User> login(
            @RequestParam(required = true, value = "username") String username,
            @RequestParam(required = true, value = "password") String password,
            HttpSession session) {
        if (StringUtils.isEmpty(username) && StringUtils.isEmpty(password)) {
            return ServerResponse.createByErrorMessage("用户名或密码为空");
        }
        ServerResponse<User> ret = userService.verifyAcc(username, password);
        if (!ret.isSuccess()) {
            return ret;
        }else {
            User user = ret.getData();
            if (user.getRole() != Const.Role.ROLE_ADMIN) {
                return ServerResponse.createByErrorMessage("用户无权限登录后台管理");
            }
        }
        session.setAttribute(Const.CURRENT_USER, ret.getData());

        log.info("==================LOGIN SUCCESS=====================");
        return ServerResponse.createBySuccessMessage("登录成功！");
    }
}
