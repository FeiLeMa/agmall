package com.alag.mmall.controller.portal;

import com.alag.mmall.common.ServerResponse;
import com.alag.mmall.service.ProductService;
import com.alag.mmall.vo.ProductDetailVo;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/product/")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("detail")
    public ServerResponse<ProductDetailVo> getDetail(Integer productId){
        return productService.getProductDetail(productId);
    }

    @GetMapping("list")
    public ServerResponse<PageInfo> list(@RequestParam(value = "keyword",required = false)String keyword,
                                         @RequestParam(value = "categoryId",required = false)Integer categoryId,
                                         @RequestParam(value = "pageNum",defaultValue = "1")Integer pageNum,
                                         @RequestParam(value = "pageSize",defaultValue = "10")Integer pageSize){
        return productService.getListByKeyword(keyword, categoryId, pageNum, pageSize);
    }
}
