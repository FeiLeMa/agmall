package com.alag.mmall.service;

import com.alag.mmall.common.ServerResponse;

import java.util.Map;

public interface OrderService {
    ServerResponse<String> pay(Integer id, Long orderNo);

    ServerResponse aliCallback(Map<String, String> params);

    ServerResponse<Boolean> getOrderStatusByOrderNoAndUserId(Integer userId, Long orderNo);

    ServerResponse createOrder(Integer id, Integer shippingId);

    ServerResponse canncelOrder(Integer userId, Long orderNo);

    ServerResponse getCartProduct(Integer userId);

    ServerResponse getDetail(Integer id, Long orderNo);

    ServerResponse list(Integer id, Integer pageNum, Integer pageSize);

    void closeOrder(int hour);
}
