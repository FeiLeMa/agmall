package com.alag.mmall.service.impl;

import com.alag.mmall.common.ServerResponse;
import com.alag.mmall.mapper.CategoryMapper;
import com.alag.mmall.model.Category;
import com.alag.mmall.service.CategoryService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class CategoryServiceImp implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;
    private Logger logger = LoggerFactory.getLogger(CategoryServiceImp.class);
    @Override
    public ServerResponse addCategory(String categoryName, Integer parentId) {
        if (StringUtils.isBlank(categoryName) || null == parentId) {
            return ServerResponse.createByErrorMessage("类别不能为空");
        }
        Category category = new Category();
        category.setParentId(parentId);
        category.setName(categoryName);
        category.setStatus(true);

        int insertCount = categoryMapper.insert(category);
        if (insertCount <= 0){
            return ServerResponse.createByErrorMessage("添加商品失败");
        }
        return ServerResponse.createBySuccessMessage("添加商品成功");
    }

    @Override
    public ServerResponse modifyCategoryName(Integer categoryId, String categoryName) {
        if (StringUtils.isBlank(categoryName) || null == categoryId) {
            return ServerResponse.createByErrorMessage("商品ID和商品名称不可为空");
        }

        Category category = new Category();
        category.setId(categoryId);
        category.setName(categoryName);
        int updateCategoryName = categoryMapper.updateByPrimaryKeySelective(category);
        if (updateCategoryName <= 0){
            return ServerResponse.createByErrorMessage("修改商品类别失败");
        }
        return ServerResponse.createBySuccessMessage("修改商品类别成功");
    }

    @Override
    public ServerResponse<List<Category>> getParallelCategoryByParentId(Integer categoryId) {
        List<Category> categoryList = categoryMapper.selectCategoryByParentId(categoryId);
        if (CollectionUtils.isEmpty(categoryList)) {
            logger.error("当前节点下没有子节点");
        }
        return ServerResponse.createBySuccess(categoryList);
    }
}
