package com.alag.mmall.service;

import com.alag.mmall.common.ServerResponse;
import com.alag.mmall.vo.CartVo;

public interface CartService {
    ServerResponse<CartVo> add(Integer id, Integer productId, Integer count);

    ServerResponse<CartVo> update(Integer id, Integer productId, Integer count);

    ServerResponse<CartVo> deleteProduct(Integer id, String productIds);

    ServerResponse<CartVo> list(Integer id);

    ServerResponse<CartVo> selectOrUnSelect(Integer id,Integer productId, int checked);

    ServerResponse<Integer> getProductCountInCart(Integer id);
}
