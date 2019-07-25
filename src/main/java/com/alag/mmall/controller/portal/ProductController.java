package com.alag.mmall.controller.portal;

import com.alag.mmall.common.ServerResponse;
import com.alag.mmall.service.ProductService;
import com.alag.mmall.vo.ProductDetailVo;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/detail/{productId}")
    public ServerResponse<ProductDetailVo> getDetail(@PathVariable Integer productId){
        return productService.getProductDetail(productId);
    }

    @GetMapping("/list/{keyword}/{categoryId}/{pageNum}/{pageSize}")
    public ServerResponse<PageInfo> list(@PathVariable(value = "keyword")String keyword,
                                         @PathVariable(value = "categoryId")Integer categoryId,
                                         @PathVariable(value = "pageNum")Integer pageNum,
                                         @PathVariable(value = "pageSize")Integer pageSize){
        if (pageNum == null) {
            pageNum = 1;
        }
        if (pageSize == null) {
            pageSize = 10;
        }
        return productService.getListByKeyword(keyword, categoryId, pageNum, pageSize);
    }
}
