package com.alag.mmall.controller.backend;

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

    private ServerResponse checkAdmin(HttpSession session) {
        User sessionUser = (User) session.getAttribute(Const.CURRENT_USER);
        if (sessionUser == null) {
            return ServerResponse.createByErrorMessage("请登录后再修改商品");
        }
        return sessionUser.getRole().equals(Const.Role.ROLE_ADMIN) ? ServerResponse.createBySuccessMessage("isAdmin") : ServerResponse.createByErrorMessage("该账号不是管理员");
    }

    @PostMapping("add_category")
    public ServerResponse addCategory(HttpSession session, String categoryName,
                                      @RequestParam(value = "parentId", defaultValue = "0") Integer parentId) {
        ServerResponse isAdmin = this.checkAdmin(session);
        if (!isAdmin.isSuccess()) {
            return isAdmin;
        }
        return categoryService.addCategory(categoryName, parentId);
    }

    @PostMapping("modify_category_name")
    public ServerResponse modifyCategoryName(HttpSession session, Integer categoryId, String categoryName) {
        ServerResponse isAdmin = this.checkAdmin(session);
        if (!isAdmin.isSuccess()) {
            return isAdmin;
        }
        return categoryService.modifyCategoryName(categoryId, categoryName);
    }

    @GetMapping("get_parallel_category")
    public ServerResponse getParallelCategory(HttpSession session,
                                              @RequestParam(value = "categoryId", defaultValue = "0") Integer categoryId) {
        ServerResponse isAdmin = this.checkAdmin(session);
        if (!isAdmin.isSuccess()) {
            return isAdmin;
        }
        return categoryService.getParallelCategoryByParentId(categoryId);
    }

    @GetMapping("get_deep_child_id")
    public ServerResponse<List<Integer>> getDeepChildId(HttpSession session,Integer categoryId){
        ServerResponse isAdmin = this.checkAdmin(session);
        if (!isAdmin.isSuccess()) {
            return isAdmin;
        }
        return categoryService.getAllDeepChildId(categoryId);
    }

}
