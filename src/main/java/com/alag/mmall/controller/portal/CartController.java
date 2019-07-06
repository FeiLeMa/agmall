package com.alag.mmall.controller.portal;

import com.alag.mmall.common.Const;
import com.alag.mmall.common.ResponseCode;
import com.alag.mmall.common.ServerResponse;
import com.alag.mmall.model.User;
import com.alag.mmall.service.CartService;
import com.alag.mmall.vo.CartVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/cart/")
public class CartController {
    @Autowired
    private CartService cartService;

    @PostMapping("add")
    public ServerResponse<CartVo> add(HttpSession session, Integer productId, Integer count) {
        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if (currentUser == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "请先登录后再操作");
        }
        return cartService.add(currentUser.getId(), productId, count);
    }

    @PostMapping("update")
    public ServerResponse<CartVo> update(HttpSession session, Integer productId, Integer count) {
        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if (currentUser == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "请先登录后再操作");
        }
        return cartService.update(currentUser.getId(), productId, count);
    }

    @DeleteMapping("delete")
    public ServerResponse<CartVo> delete(HttpSession session, String productIds) {
        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if (currentUser == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "请先登录后再操作");
        }
        return cartService.deleteProduct(currentUser.getId(), productIds);
    }

    @GetMapping("list")
    public ServerResponse<CartVo> list(HttpSession session) {
        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if (currentUser == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "请先登录后再操作");
        }
        return cartService.list(currentUser.getId());
    }

    @PutMapping("select_all")
    public ServerResponse<CartVo> selectAll(HttpSession session) {
        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if (currentUser == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "请先登录后再操作");
        }
        return cartService.selectOrUnSelect(currentUser.getId(),null,Const.Cart.CHECKED);
    }

    @PutMapping("select")
    public ServerResponse<CartVo> select(HttpSession session,Integer productId) {
        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if (currentUser == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "请先登录后再操作");
        }
        return cartService.selectOrUnSelect(currentUser.getId(),productId,Const.Cart.CHECKED);
    }

    @PutMapping("un_select_all")
    public ServerResponse<CartVo> unSelectAll(HttpSession session) {
        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if (currentUser == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "请先登录后再操作");
        }
        return cartService.selectOrUnSelect(currentUser.getId(),null,Const.Cart.UN_CHECKED);
    }

    @PutMapping("un_select")
    public ServerResponse<CartVo> unSelect(HttpSession session,Integer productId) {
        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if (currentUser == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "请先登录后再操作");
        }
        return cartService.selectOrUnSelect(currentUser.getId(),productId,Const.Cart.UN_CHECKED);
    }

    @GetMapping("get_cart_product_count")
    public ServerResponse<Integer> getCartCount(HttpSession session) {
        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if (currentUser == null) {
            return ServerResponse.createBySuccess(0);
        }
        return cartService.getProductCountInCart(currentUser.getId());
    }
}
