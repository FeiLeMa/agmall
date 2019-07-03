package com.alag.mmall.controller.portal;

import com.alag.mmall.common.Const;
import com.alag.mmall.common.ResponseCode;
import com.alag.mmall.common.ServerResponse;
import com.alag.mmall.model.User;
import com.alag.mmall.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

import org.apache.log4j.LogManager;

@RestController
@RequestMapping("/user/")
public class UserController {
    @Autowired
    private UserService userService;
    private Logger logger = LogManager.getLogger(UserController.class);

    /**
     * 用户登录
     *
     * @param username
     * @param password
     * @param session
     * @return
     */
    @PostMapping("login")
    public ServerResponse login(
            @RequestParam(required = true, value = "username") String username,
            @RequestParam(required = true, value = "password") String password,
            HttpSession session) {
        if (StringUtils.isEmpty(username) && StringUtils.isEmpty(password)) {
            return ServerResponse.createByErrorMessage("用户名或密码为空");
        }
        ServerResponse ret = userService.verifyAcc(username, password);
        if (ret.getStatus() == 1) {
            return ret;
        }
        session.setAttribute(Const.CURRENT_USER, ret.getData());
        return ServerResponse.createBySuccessMessage("登录成功！");
    }

    @GetMapping("logout")
    public ServerResponse logout(HttpSession session) {
        session.removeAttribute(Const.CURRENT_USER);
        return ServerResponse.createBySuccess();
    }

    @PostMapping("register")
    public ServerResponse register(User user) {
        logger.info(user);
        return userService.register(user);
    }

    @PostMapping("check_valid")
    public ServerResponse checkValid(String str, String type) {
        return userService.checkValid(str, type);
    }

    @GetMapping("get_user_info")
    public ServerResponse<User> getUserInfo(HttpSession session) {
        User sessionUser = (User) session.getAttribute(Const.CURRENT_USER);
        if (sessionUser == null) {
            return ServerResponse.createByErrorMessage("用户未登陆");
        }
        return ServerResponse.createBySuccess(sessionUser);
    }

    @GetMapping("get_question")
    public ServerResponse<String> getQuestion(String username) {
        return userService.selectQuestion(username);
    }

    @PostMapping("check_answer")
    public ServerResponse checkAnswer(String username, String question, String answer) {
        logger.info(username + "," + question + "," + answer);
        return userService.checkQuestion(username, question, answer);
    }

    @PostMapping("reset_password")
    public ServerResponse resetPassword(String username, String newPasswd, String token) {
        return userService.resetPassword(username, newPasswd, token);
    }

    @PostMapping("online_reset_password")
    public ServerResponse resetPasswordBySession(String oldPasswd, String newPasswd, HttpSession session) {
        logger.info(oldPasswd + "," + newPasswd);
        User sessionUser = (User) session.getAttribute(Const.CURRENT_USER);
        if (sessionUser == null) {
            return ServerResponse.createByErrorMessage("用户未登录无法在线修改密码");
        }
        return userService.resetPasswdBySession(oldPasswd, newPasswd, sessionUser.getId());
    }

    @PostMapping("update_user_info")
    public ServerResponse updateUserInfo(HttpSession session, User user) {
        User sessionUser = (User) session.getAttribute(Const.CURRENT_USER);
        if (sessionUser == null) {
            return ServerResponse.createByErrorMessage("用户未登录无法在线修改密码");
        }
        user.setId(sessionUser.getId());
        user.setUsername(sessionUser.getUsername());
        ServerResponse response = userService.modifyUserInfo(user);
        if (response.isSuccess()) {
            session.setAttribute(Const.CURRENT_USER, response.getData());
        }
        return response;
    }

    @GetMapping("get_information")
    public ServerResponse<User> getInfomation(HttpSession session) {
        User sessionUser = (User) session.getAttribute(Const.CURRENT_USER);
        if (sessionUser == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "未登录");
        }
        return userService.getInfomationByCurrentUserId(sessionUser.getId());
    }

}
