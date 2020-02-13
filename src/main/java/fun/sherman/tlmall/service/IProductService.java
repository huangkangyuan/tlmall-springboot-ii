package fun.sherman.tlmall.service;

import com.github.pagehelper.PageInfo;
import fun.sherman.tlmall.common.ServerResponse;
import fun.sherman.tlmall.domain.Product;
import fun.sherman.tlmall.vo.ProductDetailVo;

/**
 * @author sherman
 */
public interface IProductService {
    ServerResponse saveOrUpdateProduct(Product product);

    ServerResponse<String> setProductStatus(Integer productId, Integer status);

    ServerResponse<ProductDetailVo> manageProductDetail(Integer productId);

    ServerResponse getProductList(int pageNUm, int pageSize);

    ServerResponse searchByProductOrId(String productName, Integer productId, int pageNum, int pageSize);

    ServerResponse<ProductDetailVo> getProductDetail(Integer productId);

    ServerResponse<PageInfo> getProductByKeywordAndCategoryId(String keyword, Integer categoryId, Integer pageNum, Integer pageSize, String orderBy);
}
