package com.alag.mmall.service.impl;

import com.alag.mmall.common.Const;
import com.alag.mmall.common.PropertiesUtil;
import com.alag.mmall.common.ResponseCode;
import com.alag.mmall.common.ServerResponse;
import com.alag.mmall.mapper.CategoryMapper;
import com.alag.mmall.mapper.ProductMapper;
import com.alag.mmall.model.Category;
import com.alag.mmall.model.Product;
import com.alag.mmall.service.CategoryService;
import com.alag.mmall.service.ProductService;
import com.alag.mmall.vo.ProductDetailVo;
import com.alag.mmall.vo.ProductListVo;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.List;

@Service
@Slf4j
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private CategoryService categoryService;


    private ProductListVo assembleProductListVo(Product product) {
        ProductListVo productListVo = new ProductListVo();
        productListVo.setId(product.getId());
        productListVo.setName(product.getName());
        productListVo.setCategoryId(product.getCategoryId());
        productListVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        productListVo.setMainImage(product.getMainImage());
        productListVo.setPrice(product.getPrice());
        productListVo.setSubtitle(product.getSubtitle());
        productListVo.setStatus(product.getStatus());
        return productListVo;
    }

    private ProductDetailVo assembleProductDetailVo(Product product) {
        ProductDetailVo productDetailVo = new ProductDetailVo();
        productDetailVo.setId(product.getId());
        productDetailVo.setSubtitle(product.getSubtitle());
        productDetailVo.setPrice(product.getPrice());
        productDetailVo.setMainImage(product.getMainImage());
        productDetailVo.setSubImages(product.getSubImages());
        productDetailVo.setCategoryId(product.getCategoryId());
        productDetailVo.setDetail(product.getDetail());
        productDetailVo.setName(product.getName());
        productDetailVo.setStatus(product.getStatus());
        productDetailVo.setStock(product.getStock());

        productDetailVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));

        Category category = categoryMapper.selectByPrimaryKey(product.getCategoryId());
        if (category == null) {
            productDetailVo.setParentCategoryId(0);//默认根节点
        } else {
            productDetailVo.setParentCategoryId(category.getParentId());
        }
        return productDetailVo;
    }

    @Override
    public ServerResponse saveProduct(Product product) {
        if (null == product) {
            return ServerResponse.createByErrorMessage("参数有误");
        }
        if (StringUtils.isNotBlank(product.getSubImages())) {
            String[] subImagesArray = product.getSubImages().split(",");
            if (subImagesArray.length > 0) {
                product.setMainImage(subImagesArray[0]);
            }
        }
        if (product.getId() != null) {
            int rowCount = productMapper.updateByPrimaryKeySelective(product);
            if (rowCount > 0) {
                return ServerResponse.createBySuccessMessage("更新产品成功");
            } else {
                return ServerResponse.createByErrorMessage("更新产品失败");
            }
        } else {
            product.setCreateTime(new Date(new java.util.Date().getTime()));
            int rowCount = productMapper.insertSelective(product);
            if (rowCount > 0) {
                return ServerResponse.createBySuccessMessage("保存产品成功");
            } else {
                return ServerResponse.createByErrorMessage("保存产品失败");
            }
        }
    }

    @Override
    public ServerResponse<String> setSaleStatus(Integer productId, Integer status) {
        if (productId == null || status == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), "参数不可为空");
        }
        Product product = new Product();
        product.setStatus(status);
        product.setId(productId);
        int rowCount = productMapper.updateByPrimaryKeySelective(product);
        if (rowCount <= 0) {
            return ServerResponse.createByErrorMessage("更新产品状态失败");
        }
        return ServerResponse.createBySuccessMessage("更新产品状态成功");
    }

    @Override
    public ServerResponse<ProductDetailVo> getDetail(Integer productId) {
        if (productId == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), "参数不合法");
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product == null) {
            return ServerResponse.createByErrorMessage("产品不存在");
        }

        ProductDetailVo productDetailVo = this.assembleProductDetailVo(product);
        return ServerResponse.createBySuccess(productDetailVo);
    }

    @Override
    public ServerResponse<PageInfo> list(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Product> productList = productMapper.selectList();

        List<ProductListVo> productListVos = Lists.newArrayList();
        for (Product productItem : productList) {
            ProductListVo productListVo = this.assembleProductListVo(productItem);
            productListVos.add(productListVo);
        }
        PageInfo pageInfo = new PageInfo(productList);
        pageInfo.setList(productListVos);
        return ServerResponse.createBySuccess(pageInfo);
    }

    @Override
    public ServerResponse<PageInfo> getProductByIdAndName(int pageNum, int pageSize, String productName, Integer productId) {
        if (StringUtils.isNotBlank(productName)) {
            productName = new StringBuilder("%").append(productName).append("%").toString();
        }
        PageHelper.startPage(pageNum, pageSize);
        List<Product> productList = productMapper.selectByIdAndName(productId, productName);

        List<ProductListVo> productListVos = Lists.newArrayList();
        for (Product productItem : productList) {
            ProductListVo productListVo = assembleProductListVo(productItem);
            productListVos.add(productListVo);
        }
        PageInfo pageInfo = new PageInfo(productList);
        pageInfo.setList(productListVos);

        return ServerResponse.createBySuccess(pageInfo);
    }

    @Override
    public ServerResponse<ProductDetailVo> getProductDetail(Integer productId) {
        if (productId == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), "参数不合法");
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product == null) {
            return ServerResponse.createByErrorMessage("产品不存在");
        }
        if (product.getStatus() != Const.ProductStatusEnum.ON_SALE.getCode()) {
            return ServerResponse.createByErrorMessage("产品已下架");
        }

        ProductDetailVo productDetailVo = this.assembleProductDetailVo(product);
        return ServerResponse.createBySuccess(productDetailVo);
    }

    @Override
    public ServerResponse<PageInfo> getListByKeyword(String keyword, Integer categoryId, Integer pageNum, Integer pageSize) {
        log.info("keyword:{},categoryId:{},pageNum:{},pageSize:{}",keyword, categoryId, pageNum, pageSize);
        if (StringUtils.isBlank(keyword) && null == categoryId) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), "参数不可为空");
        }

        Category category = categoryMapper.selectByPrimaryKey(categoryId);
        log.info("category:",category);
        if (category == null && StringUtils.isBlank(keyword)) {
            PageHelper.startPage(pageNum, pageSize);
            List<ProductListVo> productList = Lists.newArrayList();
            PageInfo pageInfo = new PageInfo(productList);
            log.info("没有查到数据");
            return ServerResponse.createBySuccess(pageInfo);
        }
        if (StringUtils.isNotBlank(keyword)) {
            keyword = new StringBuilder("%").append(keyword).append("%").toString();
        }

        List<ProductListVo> productListVoList = Lists.newArrayList();
        List<Integer> categoryIdList = categoryService.getAllDeepChildId(categoryId).getData();
        PageHelper.startPage(pageNum, pageSize);
        List<Product> productList = productMapper.getProductListByKeywordAndcategoryIdList(keyword, categoryIdList.size() == 0 ? null : categoryIdList);
        for (Product product : productList) {
            ProductListVo productListVo = assembleProductListVo(product);
            productListVoList.add(productListVo);
        }
        PageInfo pageInfo = new PageInfo(productList);
        pageInfo.setList(productListVoList);
        return ServerResponse.createBySuccess(pageInfo);
    }
}
