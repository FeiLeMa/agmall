package com.alag.mmall.controller.backend;

import com.alag.mmall.common.Const;
import com.alag.mmall.common.PropertiesUtil;
import com.alag.mmall.common.ResponseCode;
import com.alag.mmall.common.ServerResponse;
import com.alag.mmall.model.Product;
import com.alag.mmall.model.User;
import com.alag.mmall.service.FileService;
import com.alag.mmall.service.ProductService;
import com.alag.mmall.vo.ProductDetailVo;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/manage/product")
public class ProductManageController {
    private static Logger logger = LoggerFactory.getLogger(ProductManageController.class);

    @Autowired
    private ProductService productService;

    @Autowired
    private FileService fileService;

    private ServerResponse checkAdmin(HttpSession session) {
        User sessionUser = (User) session.getAttribute(Const.CURRENT_USER);
        if (sessionUser == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "请先登录再操作");
        }
        return sessionUser.getRole().equals(Const.Role.ROLE_ADMIN) ? ServerResponse.createBySuccessMessage("isAdmin") : ServerResponse.createByErrorMessage("该账号不是管理员");
    }


    @PostMapping("save_product")
    public ServerResponse saveProduct(HttpSession session,Product product) {
        logger.info(product.toString());
        ServerResponse isAdmin = this.checkAdmin(session);
        if (!isAdmin.isSuccess()) {
            return isAdmin;
        }
        return productService.saveProduct(product);
    }
    @PostMapping("set_sale_status")
    public ServerResponse<String> setSaleStatus(HttpSession session,Integer productId, Integer status) {
        ServerResponse isAdmin = this.checkAdmin(session);
        if (!isAdmin.isSuccess()) {
            return isAdmin;
        }
        return productService.setSaleStatus(productId, status);
    }

    @GetMapping("get_detail")
    public ServerResponse<ProductDetailVo> getDetail(HttpSession session, Integer productId) {
        ServerResponse isAdmin = this.checkAdmin(session);
        if (!isAdmin.isSuccess()) {
            return isAdmin;
        }
        return productService.getDetail(productId);
    }

    @GetMapping("list")
    public ServerResponse<PageInfo> list(HttpSession session,
                                         @RequestParam(value = "pageNum",defaultValue = "1") int pageNum,
                                         @RequestParam(value = "pageSize",defaultValue = "10")int pageSize) {
        ServerResponse isAdmin = this.checkAdmin(session);
        if (!isAdmin.isSuccess()) {
            return isAdmin;
        }
        return productService.list(pageNum,pageSize);
    }

    @GetMapping("search")
    public ServerResponse<PageInfo> search(HttpSession session,
                                           String productName,Integer productId,
                                           @RequestParam(value = "pageNum",defaultValue = "1") int pageNum,
                                           @RequestParam(value = "pageSize",defaultValue = "10")int pageSize) {
        ServerResponse isAdmin = this.checkAdmin(session);
        if (!isAdmin.isSuccess()) {
            return isAdmin;
        }
        return productService.getProductByIdAndName(pageNum, pageSize, productName, productId);
    }
    @PostMapping("upload")
    public ServerResponse<Map> upload(HttpSession session, @RequestParam(value = "upload_file",required = true)MultipartFile file) {
        ServerResponse isAdmin = this.checkAdmin(session);
        if (!isAdmin.isSuccess()) {
            return isAdmin;
        }
        String path = ClassUtils.getDefaultClassLoader().getResource("").getPath() + "upload/";
        String targetFileName = fileService.upload(file, path);
        String url = PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFileName;

        Map fileMap = Maps.newHashMap();
        fileMap.put("uri",targetFileName);
        fileMap.put("url",url);
        return ServerResponse.createBySuccess(fileMap);
    }
    @PostMapping("richtext_img_upload")
    public Map richtextImgUpload(HttpSession session, HttpServletResponse response,
                                                 @RequestParam(value = "upload_file",required = true)MultipartFile file) {
        Map resultMap = Maps.newHashMap();
        ServerResponse isAdmin = this.checkAdmin(session);
        if (!isAdmin.isSuccess()) {
            resultMap.put("success",false);
            resultMap.put("msg","无权限操作");
            return resultMap;
        }
        String path = ClassUtils.getDefaultClassLoader().getResource("").getPath() + "upload/";
        String targetFileName = fileService.upload(file, path);
        if(StringUtils.isBlank(targetFileName)){
            resultMap.put("success",false);
            resultMap.put("msg","上传失败");
            return resultMap;
        }
        String url = PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFileName;
        resultMap.put("success",true);
        resultMap.put("msg","上传成功");
        resultMap.put("file_path",url);
        response.addHeader("Access-Control-Allow-Headers","X-File-Name");
        return resultMap;
    }


}
