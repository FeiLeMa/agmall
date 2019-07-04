package com.alag.mmall.service;

import com.alag.mmall.common.ServerResponse;
import com.alag.mmall.model.Category;

import java.util.List;

public interface CategoryService {
    ServerResponse addCategory(String categoryName, Integer parentId);

    ServerResponse modifyCategoryName(Integer categoryId, String categoryName);

    ServerResponse<List<Category>> getParallelCategoryByParentId(Integer categoryId);
}
