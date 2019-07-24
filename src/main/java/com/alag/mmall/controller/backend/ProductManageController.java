package com.alag.mmall.controller.backend;

import com.alag.mmall.Interceptor.LoginRequired;
import com.alag.mmall.common.Const;
import com.alag.mmall.common.PropertiesUtil;
import com.alag.mmall.common.ResponseCode;
import com.alag.mmall.common.ServerResponse;
import com.alag.mmall.model.Product;
import com.alag.mmall.model.User;
import com.alag.mmall.service.FileService;
import com.alag.mmall.service.ProductService;
import com.alag.mmall.vo.ProductDetailVo;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/manage/product")
@Slf4j
public class ProductManageController {

    @Autowired
    private ProductService productService;

    @Autowired
    private FileService fileService;


    @PostMapping("save_product")
    @LoginRequired
    public ServerResponse saveProduct(HttpSession session,Product product) {
        log.info(product.toString());
        return productService.saveProduct(product);
    }
    @PostMapping("set_sale_status")
    @LoginRequired
    public ServerResponse<String> setSaleStatus(HttpSession session,Integer productId, Integer status) {
        return productService.setSaleStatus(productId, status);
    }

    @GetMapping("get_detail")
    @LoginRequired
    public ServerResponse<ProductDetailVo> getDetail(HttpSession session, Integer productId) {
        return productService.getDetail(productId);
    }

    @GetMapping("list")
    @LoginRequired
    public ServerResponse<PageInfo> list(HttpSession session,
                                         @RequestParam(value = "pageNum",defaultValue = "1") int pageNum,
                                         @RequestParam(value = "pageSize",defaultValue = "10")int pageSize) {
        return productService.list(pageNum,pageSize);
    }

    @GetMapping("search")
    @LoginRequired
    public ServerResponse<PageInfo> search(HttpSession session,
                                           String productName,Integer productId,
                                           @RequestParam(value = "pageNum",defaultValue = "1") int pageNum,
                                           @RequestParam(value = "pageSize",defaultValue = "10")int pageSize) {
        return productService.getProductByIdAndName(pageNum, pageSize, productName, productId);
    }
    @PostMapping("upload")
    @LoginRequired
    public ServerResponse<Map> upload(HttpSession session, @RequestParam(value = "upload_file",required = true)MultipartFile file) {
        String path = ClassUtils.getDefaultClassLoader().getResource("").getPath() + "upload/";
        String targetFileName = fileService.upload(file, path);
        String url = PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFileName;

        Map fileMap = Maps.newHashMap();
        fileMap.put("uri",targetFileName);
        fileMap.put("url",url);
        return ServerResponse.createBySuccess(fileMap);
    }

    @PostMapping("richtext_img_upload")
    @LoginRequired
    public Map richtextImgUpload(HttpSession session, HttpServletResponse response,
                                                 @RequestParam(value = "upload_file",required = true)MultipartFile file) {
        Map resultMap = Maps.newHashMap();
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
