package com.alag.mmall.service;

import com.alag.mmall.common.ServerResponse;
import com.alag.mmall.model.Product;
import com.alag.mmall.vo.ProductDetailVo;
import com.github.pagehelper.PageInfo;

import java.util.List;

public interface ProductService {
    ServerResponse saveProduct(Product product);

    ServerResponse<String> setSaleStatus(Integer productId, Integer status);

    ServerResponse<ProductDetailVo> getDetail(Integer productId);

    ServerResponse<PageInfo> list(int pageNum, int pageSize);

    ServerResponse<PageInfo> getProductByIdAndName(int pageNum, int pageSize, String productName, Integer productId);

    ServerResponse<ProductDetailVo> getProductDetail(Integer productId);

    ServerResponse<PageInfo> getListByKeyword(String keyword, Integer categoryId, Integer pageNum, Integer pageSize);
}
