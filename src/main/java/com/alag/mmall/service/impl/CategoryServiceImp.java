package com.alag.mmall.service.impl;

import com.alag.mmall.common.ServerResponse;
import com.alag.mmall.mapper.CategoryMapper;
import com.alag.mmall.model.Category;
import com.alag.mmall.service.CategoryService;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class CategoryServiceImp implements CategoryService {
    private static Set<Integer> categorySet = Sets.newHashSet();

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
        if (insertCount <= 0) {
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
        if (updateCategoryName <= 0) {
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

    @Override
    public ServerResponse<List<Integer>> getAllDeepChildId(Integer categoryId) {
        this.getIdByReRcursion(categoryId);
        ArrayList<Integer> categoryIds = Lists.newArrayList();
        for (Integer id : categorySet) {
            categoryIds.add(id);
        }
        categorySet.clear();
        return ServerResponse.createBySuccess(categoryIds);
    }

    private void getIdByReRcursion(Integer categoryId) {
        List<Category> categoryList = categoryMapper.selectCategoryByParentId(categoryId);
        if (CollectionUtils.isEmpty(categoryList)) {
            return;
        }
        for (Category category : categoryList) {
            Integer id = category.getId();
            categorySet.add(id);
            logger.info("~~~~   "+id +"刚放入静态Set中");
        }
        for (Category category : categoryList) {
            this.getIdByReRcursion(category.getId());
        }
    }
}
