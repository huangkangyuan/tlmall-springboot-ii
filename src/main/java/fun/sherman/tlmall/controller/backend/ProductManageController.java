package fun.sherman.tlmall.controller.backend;

import fun.sherman.tlmall.common.Const;
import fun.sherman.tlmall.common.ResponseCode;
import fun.sherman.tlmall.common.ServerResponse;
import fun.sherman.tlmall.domain.Product;
import fun.sherman.tlmall.domain.User;
import fun.sherman.tlmall.service.IFileService;
import fun.sherman.tlmall.service.IProductService;
import fun.sherman.tlmall.service.IUserService;
import fun.sherman.tlmall.util.CookieUtil;
import fun.sherman.tlmall.util.JacksonUtil;
import fun.sherman.tlmall.util.PropertiesUtil;
import fun.sherman.tlmall.util.ShardedRedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * @author sherman
 */
@Controller
@RequestMapping("/manage/product")
@ResponseBody
public class ProductManageController {

    @Autowired
    private IProductService iProductService;

    @Autowired
    private IFileService iFileService;

    /**
     * 保存或者更新商品：根据传入的product是否有product_id
     *
     * @param product 传入的product对象
     * @return 保存或者更新商品结果信息
     */
    @RequestMapping(value = "save_product.do", method = RequestMethod.POST)
    public ServerResponse saveProduct(@RequestBody Product product) {
        return iProductService.saveOrUpdateProduct(product);
    }

    /**
     * 修改商品的status信息
     *
     * @param session   http session
     * @param productId 商品 id
     * @param status    商品 状态码
     * @return 修改商品status是否成功信息
     */
    @RequestMapping(value = "set_product_status.do", method = RequestMethod.POST)
    public ServerResponse<String> setProductStatus(HttpSession session,
                                                   @RequestParam("product_id") Integer productId,
                                                   @RequestParam("status") Integer status) {
        return iProductService.setProductStatus(productId, status);
    }

    /**
     * 获取商品详细信息
     *
     * @param productId 商品id
     * @return 返回商品详情信息
     */
    @RequestMapping(value = "details.do", method = RequestMethod.POST)
    public ServerResponse getProductDetails(@RequestParam("product_id") Integer productId) {
        return iProductService.manageProductDetail(productId);
    }

    @RequestMapping(value = "list.do", method = RequestMethod.GET)
    public ServerResponse getList(/*HttpServletRequest request,*/
            @RequestParam(value = "page_num", defaultValue = "1") int pageNum,
            @RequestParam(value = "page_size", defaultValue = "10") int pageSize) {
        /**
         * 以下注释部分：对于管理员权限验证工作全部交由拦截器处理
         * 本防止直接填充业务实现即可
         */
//        String loginToken = CookieUtil.readLoginToken(request);
//        if (StringUtils.isEmpty(loginToken)) {
//            return ServerResponse.buildErrorByMsg("用户未登录,无法获取当前用户的信息");
//        }
//        String userJsonStr = ShardedRedisUtil.get(loginToken);
//        User user = JacksonUtil.stringToBean(userJsonStr, User.class);
//
//        if (user == null) {
//            return ServerResponse.buildErrorByCode(ResponseCode.NEED_LOGIN.getCode(), "用户未登录,请登录管理员");
//
//        }
//        if (iUserService.checkAdminRole(user).isSuccess()) {
//            //填充业务
//            return iProductService.getProductList(pageNum, pageSize);
//        } else {
//            return ServerResponse.buildErrorByMsg("无权限操作");
//        }
        return iProductService.getProductList(pageNum, pageSize);
    }

    @RequestMapping(value = "search.do", method = RequestMethod.POST)
    public ServerResponse productSearch(@RequestParam(value = "product_name", required = false) String productName,
                                        @RequestParam(value = "product_id", required = false) Integer productId,
                                        @RequestParam(value = "page_num", defaultValue = "1") int pageNum,
                                        @RequestParam(value = "page_size", defaultValue = "10") int pageSize) {
        return iProductService.searchByProductOrId(productName, productId, pageNum, pageSize);
    }

    @RequestMapping(value = "upload.do", method = RequestMethod.POST)
    public ServerResponse upload(HttpServletRequest request,
                                 @RequestParam(value = "upload_file", required = false) MultipartFile multipartFile) {
        String path = request.getSession().getServletContext().getRealPath(Const.UPLOAD_PATH);
        String targetFilename = iFileService.upload(multipartFile, path);
        String url = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFilename;
        Map<String, String> fileMap = new HashMap<>();
        fileMap.put("uri", targetFilename);
        fileMap.put("url", url);
        return ServerResponse.buildSuccessByData(fileMap);
    }

    @RequestMapping(value = "richtext_upload.do", method = RequestMethod.POST)
    public Map<String, Object> richtextUpload(HttpServletRequest request, HttpServletResponse response,
                                              @RequestParam(value = "upload_file", required = false) MultipartFile multipartFile) {
        Map<String, Object> resultMap = new HashMap<>();
        String path = request.getSession().getServletContext().getRealPath(Const.UPLOAD_PATH);
        String targetFilename = iFileService.upload(multipartFile, path);
        if (StringUtils.isBlank(targetFilename)) {
            resultMap.put("success", false);
            resultMap.put("msg", "上传失败");
            return resultMap;
        }
        String url = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFilename;
        resultMap.put("success", true);
        resultMap.put("msg", "上传成功");
        resultMap.put("file_path", url);
        response.addHeader("Access-Controller-Allow-Headers", "X-File-Name");
        return resultMap;
    }
}