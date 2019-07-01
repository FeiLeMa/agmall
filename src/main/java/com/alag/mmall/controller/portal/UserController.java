package com.alag.mmall.controller.portal;

import com.alag.mmall.common.ServerResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/user/")
public class UserController {

    @PostMapping("login")
    public ServerResponse login(String username, String password, HttpSession session) {
        return ServerResponse.createBySuccessMessage("Hello");
    }
}
