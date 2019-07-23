package com.alag.mmall.service.impl;

import com.alag.mmall.common.Const;
import com.alag.mmall.common.MD5Util;
import com.alag.mmall.common.ServerResponse;
import com.alag.mmall.config.RedisService;
import com.alag.mmall.mapper.UserMapper;
import com.alag.mmall.model.User;
import com.alag.mmall.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RedisService redisService;

    @Override
    public ServerResponse verifyAcc(String username, String password) {
        int result = userMapper.checkUsername(username);
        if (result != 1) {
            return ServerResponse.createByErrorMessage("用户名有误！");
        }
        String md5Passwd = MD5Util.MD5EncodeUtf8(password);
        User user = userMapper.getUserByUsernameAndPassword(username, md5Passwd);
        if (user == null) {
            return ServerResponse.createByErrorMessage("密码不正确！");
        }
        return ServerResponse.createBySuccess(user);
    }

    @Override
    public ServerResponse register(User user) {
        ServerResponse usernameValid = this.checkValid(user.getUsername(), Const.USERNAME);
        if (!usernameValid.isSuccess()) {
            return usernameValid;
        }
        ServerResponse emailValid = this.checkValid(user.getEmail(), Const.EMAIL);
        if (!emailValid.isSuccess()) {
            return emailValid;
        }

        user.setRole(Const.Role.ROLE_CUSTOMER);
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
        user.setCreateTime(new Date(new java.util.Date().getTime()));
        user.setUpdateTime(new Date(new java.util.Date().getTime()));
        int insertUserRet = userMapper.insertSelective(user);
        if (insertUserRet == 0) {
            return ServerResponse.createByErrorMessage("注册失败");
        }

        return ServerResponse.createBySuccessMessage("注册成功");
    }

    @Override
    public ServerResponse checkValid(String str, String type) {
        if (StringUtils.isNotBlank(type)&&StringUtils.isNotBlank(str)) {
            if (Const.USERNAME.equals(type)) {
                int result = userMapper.checkUsername(str);
                if (result > 0) {
                    return ServerResponse.createByErrorMessage("用户名已存在");
                }
            }
            if (Const.EMAIL.equals(type)) {
                int result = userMapper.checkEmail(str);
                if (result > 0) {
                    return ServerResponse.createByErrorMessage("Email已存在");
                }
            }
            if (Const.PHONE.equals(type)) {
                int result = userMapper.checkPhone(str);
                if (result > 0){
                    return ServerResponse.createByErrorMessage("Phone已存在");
                }
            }

        } else {
            return ServerResponse.createByErrorMessage("参数错误");
        }

        return ServerResponse.createBySuccessMessage("验证无误");
    }

    @Override
    public ServerResponse<String> selectQuestion(String username) {
        if (StringUtils.isBlank(username)) {
            return ServerResponse.createByErrorMessage("参数错误");
        }
        ServerResponse usernameValid = this.checkValid(username, Const.USERNAME);
        if (usernameValid.isSuccess()) {
            return ServerResponse.createByErrorMessage("该账户不存在");
        }
        String question = userMapper.getQuestionByUsername(username);
        if (StringUtils.isBlank(question)) {
            return ServerResponse.createByErrorMessage("找回密码的问题未设置过");
        }
        return ServerResponse.createBySuccess(question);
    }

    @Override
    public ServerResponse checkQuestion(String username, String question, String answer) {
        int checkAnswerValid = userMapper.checkAnswer(username, question, answer);
        if (checkAnswerValid == 0){
            return ServerResponse.createByErrorMessage("问题回答错误");
        }
        String forgetToken = UUID.randomUUID().toString();
        redisService.set(Const.TOKENP_RREFIX+username, forgetToken, 60*60*24L);
        return ServerResponse.createBySuccess("问题回答正确",redisService.get(Const.TOKENP_RREFIX+username));
    }

    @Override
    public ServerResponse resetPassword(String username, String newPasswd, String token) {
        if (this.checkValid(username, Const.USERNAME).isSuccess()) {
            return ServerResponse.createByErrorMessage("用户不存在");
        }
        if (StringUtils.isBlank(token)) {
            return ServerResponse.createByErrorMessage("token不可为空");
        }

        if (!StringUtils.equals(token,String.valueOf(redisService.get(Const.TOKENP_RREFIX+username)))){
            return ServerResponse.createByErrorMessage("token不匹配");
        }

        String md5NewPasswd = MD5Util.MD5EncodeUtf8(newPasswd);
        int retPasswd = userMapper.retPasswdByUsername(username,md5NewPasswd);
        if (retPasswd <=0){
            return ServerResponse.createByErrorMessage("修改密码失败");
        }

        return ServerResponse.createBySuccess("修改密码成功");
    }

    @Override
    public ServerResponse resetPasswdBySession(String oldPasswd, String newPasswd, Integer id) {
        if (StringUtils.isAnyBlank(oldPasswd,newPasswd)) {
            return ServerResponse.createByErrorMessage("密码不可为空");
        }
        oldPasswd = MD5Util.MD5EncodeUtf8(oldPasswd);
        newPasswd = MD5Util.MD5EncodeUtf8(newPasswd);
        int passwdValid = userMapper.checkPasswd(id,oldPasswd);
        if (passwdValid !=1){
            return ServerResponse.createByErrorMessage("密码不对");
        }
        int retPasswd = userMapper.updatePasswdById(id, newPasswd);
        if (retPasswd <= 0){
            return ServerResponse.createByErrorMessage("修改密码失败");
        }
        return ServerResponse.createBySuccess("修改密码成功");
    }

    @Override
    public ServerResponse modifyUserInfo(User user) {
        if (!this.checkValid(user.getEmail(), Const.EMAIL).isSuccess()){
            return ServerResponse.createByErrorMessage("email重复");
        }
        if (!this.checkValid(user.getPhone(), Const.PHONE).isSuccess()){
            return ServerResponse.createByErrorMessage("phone重复");
        }

        User userInfo = new User();
        userInfo.setId(user.getId());
        userInfo.setEmail(user.getEmail());
        userInfo.setPhone(user.getPhone());
        userInfo.setQuestion(user.getQuestion());
        userInfo.setAnswer(user.getAnswer());

        int updateUserInfoCount = userMapper.updateByPrimaryKeySelective(userInfo);
        if (updateUserInfoCount <= 0){
            return ServerResponse.createByErrorMessage("更新用户信息失败");
        }
        return ServerResponse.createBySuccess("更新用户信息成功",user);
    }

    @Override
    public ServerResponse<User> getInfomationByCurrentUserId(Integer id) {
        User user = userMapper.getUserById(id);
        if (null == user) {
            return ServerResponse.createByErrorMessage("该Id无对应账户");
        }
        user.setPassword("");
        return ServerResponse.createBySuccess(user);
    }


}
