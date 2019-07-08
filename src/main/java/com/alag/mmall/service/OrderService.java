package com.alag.mmall.service;

import com.alag.mmall.common.ServerResponse;

public interface OrderService {
    ServerResponse<String> pay(Integer id, Long orderNo);
}
