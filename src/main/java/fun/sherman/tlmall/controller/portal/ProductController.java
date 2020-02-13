package fun.sherman.tlmall.controller.portal;

import com.github.pagehelper.PageInfo;
import fun.sherman.tlmall.common.ServerResponse;
import fun.sherman.tlmall.service.IProductService;
import fun.sherman.tlmall.vo.ProductDetailVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * @author sherman
 */
@RequestMapping("/product/")
@Controller
@ResponseBody
public class ProductController {

    @Autowired
    IProductService iProductService;

//    @RequestMapping(value = "detail.do", method = RequestMethod.POST)
//    public ServerResponse<ProductDetailVo> detail(@RequestParam("product_id") Integer productId) {
//        return iProductService.getProductDetail(productId);
//    }

    /**
     * Restful风格改造
     */
    @RequestMapping(value = "/{product_id}", method = RequestMethod.GET)
    public ServerResponse<ProductDetailVo> detail(@PathVariable("product_id") Integer productId) {
        return iProductService.getProductDetail(productId);
    }

//    @RequestMapping(value = "list.do", method = RequestMethod.POST)
//    public ServerResponse<PageInfo> list(@RequestParam(value = "keyword", required = false) String keyword,
//                                         @RequestParam(value = "category_id", required = false) Integer categoryId,
//                                         @RequestParam(value = "page_num", defaultValue = "1") Integer PageNum,
//                                         @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize,
//                                         @RequestParam(value = "order_by", defaultValue = "") String orderBy) {
//        return iProductService.getProductByKeywordAndCategoryId(keyword, categoryId, PageNum, pageSize, orderBy);
//    }

    /**
     * Restful风格改造：只传category_id，url前面加上category常量
     */
    @RequestMapping(value = "/category/{category_id}/{page_num}/{page_size}/{order_by}", method = RequestMethod.GET)
    public ServerResponse<PageInfo> list(@PathVariable("category_id") Integer categoryId,
                                         @PathVariable("page_num") Integer pageNum,
                                         @PathVariable("page_size") Integer pageSize,
                                         @PathVariable("order_by") String orderBy) {
        if (pageNum == null) {
            pageNum = 1;
        }
        if (pageSize == null) {
            pageSize = 1;
        }
        if (StringUtils.isBlank(orderBy)) {
            orderBy = "price_asc";
        }
        return iProductService.getProductByKeywordAndCategoryId(null, categoryId, pageNum, pageSize, orderBy);
    }


    /**
     * Restful风格改造：只传keyword，url前面加上keyword常量
     */
    @RequestMapping(value = "/keyword/{keyword}/{page_num}/{page_size}/{order_by}", method = RequestMethod.GET)
    public ServerResponse<PageInfo> list(@PathVariable("keyword") String keyword,
                                         @PathVariable("page_num") Integer pageNum,
                                         @PathVariable("page_size") Integer pageSize,
                                         @PathVariable("order_by") String orderBy) {
        if (pageNum == null) {
            pageNum = 1;
        }
        if (pageSize == null) {
            pageSize = 1;
        }
        if (StringUtils.isBlank(orderBy)) {
            orderBy = "price_asc";
        }
        return iProductService.getProductByKeywordAndCategoryId(keyword, null, pageNum, pageSize, orderBy);
    }

    /**
     * Restful风格改造：同时传入keyword和category_id
     */
    @RequestMapping(value = "/{keyword}/{category_id}/{page_num}/{page_size}/{order_by}", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<PageInfo> list(@PathVariable(value = "keyword") String keyword,
                                         @PathVariable(value = "category_id") Integer categoryId,
                                         @PathVariable(value = "page_num") Integer pageNum,
                                         @PathVariable(value = "page_size") Integer pageSize,
                                         @PathVariable(value = "order_by") String orderBy) {
        if (pageNum == null) {
            pageNum = 1;
        }
        if (pageSize == null) {
            pageSize = 1;
        }
        if (StringUtils.isBlank(orderBy)) {
            orderBy = "price_asc";
        }
        return iProductService.getProductByKeywordAndCategoryId(keyword, categoryId, pageNum, pageSize, orderBy);
    }
}
