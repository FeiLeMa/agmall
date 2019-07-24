package com.alag.mmall.controller.backend;

import com.alag.mmall.Interceptor.LoginRequired;
import com.alag.mmall.common.Const;
import com.alag.mmall.common.ServerResponse;
import com.alag.mmall.model.User;
import com.alag.mmall.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;


@RestController
@RequestMapping("/manage/category")
public class CategoryManageController {
    @Autowired
    private CategoryService categoryService;



    @PostMapping("add_category")
    @LoginRequired
    public ServerResponse addCategory(HttpSession session, String categoryName,
                                      @RequestParam(value = "parentId", defaultValue = "0") Integer parentId) {
        return categoryService.addCategory(categoryName, parentId);
    }

    @PostMapping("modify_category_name")
    @LoginRequired
    public ServerResponse modifyCategoryName(HttpSession session, Integer categoryId, String categoryName) {
        return categoryService.modifyCategoryName(categoryId, categoryName);
    }

    @GetMapping("get_parallel_category")
    @LoginRequired
    public ServerResponse getParallelCategory(HttpSession session,
                                              @RequestParam(value = "categoryId", defaultValue = "0") Integer categoryId) {
        return categoryService.getParallelCategoryByParentId(categoryId);
    }

    @GetMapping("get_deep_child_id")
    @LoginRequired
    public ServerResponse<List<Integer>> getDeepChildId(HttpSession session,Integer categoryId){
        return categoryService.getAllDeepChildId(categoryId);
    }

}
