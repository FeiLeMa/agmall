package com.alag.mmall.service;

import com.alag.mmall.common.ServerResponse;

import java.util.Map;

public interface OrderService {
    ServerResponse<String> pay(Integer id, Long orderNo);

    ServerResponse aliCallback(Map<String, String> params);

    ServerResponse<Boolean> getOrderStatusByOrderNoAndUserId(Integer userId, Long orderNo);
}
