package com.alag.mmall.service;

import com.alag.mmall.common.ServerResponse;
import com.alag.mmall.model.Shipping;
import com.github.pagehelper.PageInfo;

public interface ShippingService {
    ServerResponse add(Integer userId, Shipping shipping);

    ServerResponse del(Integer userId, Integer id);

    ServerResponse update(Integer userId, Shipping shipping);

    ServerResponse<Shipping> select(Integer id, Integer shippingId);

    ServerResponse<PageInfo> list(Integer id, Integer pageNum, Integer pageSize);
}
