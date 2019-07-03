package com.alag.mmall.service;

import com.alag.mmall.common.ServerResponse;
import com.alag.mmall.model.User;

public interface UserService {
    ServerResponse verifyAcc(String username, String password);

    ServerResponse register(User user);

    ServerResponse checkValid(String str, String type);

    ServerResponse<String> selectQuestion(String username);

    ServerResponse checkQuestion(String username, String question, String answer);

    ServerResponse resetPassword(String username, String newPasswd, String token);

    ServerResponse resetPasswdBySession(String oldPasswd, String newPasswd, Integer id);

    ServerResponse modifyUserInfo(User user);

    ServerResponse<User> getInfomationByCurrentUserId(Integer id);
}
