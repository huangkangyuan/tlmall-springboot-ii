package fun.sherman.tlmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import fun.sherman.tlmall.common.Const;
import fun.sherman.tlmall.common.ResponseCode;
import fun.sherman.tlmall.common.ServerResponse;
import fun.sherman.tlmall.dao.CategoryDao;
import fun.sherman.tlmall.dao.ProductDao;
import fun.sherman.tlmall.domain.Category;
import fun.sherman.tlmall.domain.Product;
import fun.sherman.tlmall.service.ICategoryService;
import fun.sherman.tlmall.service.IProductService;
import fun.sherman.tlmall.util.DateTimeUtil;
import fun.sherman.tlmall.util.PropertiesUtil;
import fun.sherman.tlmall.vo.ProductDetailVo;
import fun.sherman.tlmall.vo.ProductListVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sherman
 */
@Service("iProductService")
public class ProductServiceImpl implements IProductService {
    @Autowired
    private ProductDao productDao;

    @Autowired
    private CategoryDao categoryDao;

    /**
     * 平级调用
     */
    @Autowired
    private ICategoryService iCategoryService;

    @Override
    public ServerResponse saveOrUpdateProduct(Product product) {
        if (product != null) {
            // 设置主图
            if (StringUtils.isNotBlank(product.getSubImages())) {
                String[] subImages = product.getSubImages().split(",");
                if (subImages.length > 0) {
                    product.setMainImage(subImages[0]);
                }
            }
            if (product.getId() != null) {
                // 更新操作
                int resultCount = productDao.updateProduct(product);
                if (resultCount > 0) {
                    return ServerResponse.buildSuccessByMsg("产品更新成功");
                } else {
                    return ServerResponse.buildErrorByMsg("产品更新失败");
                }
            } else {
                // 添加商品
                int resultCount = productDao.insertProduct(product);
                if (resultCount > 0) {
                    return ServerResponse.buildSuccessByMsg("添加商品成功");
                } else {
                    return ServerResponse.buildErrorByMsg("添加商品失败");
                }
            }
        }
        return ServerResponse.buildErrorByMsg("更新或新增产品参数不正确");
    }

    @Override
    public ServerResponse<String> setProductStatus(Integer productId, Integer status) {
        if (productId == null || status == null) {
            return ServerResponse.buildErrorByCode(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = new Product();
        product.setId(productId);
        product.setStatus(status);
        int resultCounts = productDao.updateProduct(product);
        if (resultCounts > 0) {
            return ServerResponse.buildSuccessByMsg("修改商品状态成功");
        }
        return ServerResponse.buildErrorByMsg("修改商品状态失败");
    }

    @Override
    public ServerResponse<ProductDetailVo> manageProductDetail(Integer productId) {
        if (productId == null) {
            return ServerResponse.buildErrorByCode(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = productDao.selectProductById(productId);
        if (product == null) {
            return ServerResponse.buildErrorByMsg("商品已下架或者删除");
        }
        ProductDetailVo pdv = assembleProductDetailVo(product);
        return ServerResponse.buildSuccessByData(pdv);
    }

    private ProductDetailVo assembleProductDetailVo(Product product) {
        ProductDetailVo pdv = new ProductDetailVo();
        pdv.setId(product.getId());
        pdv.setCategoryId(product.getCategoryId());
        pdv.setDetails(product.getDetail());
        pdv.setMainImage(product.getMainImage());
        pdv.setName(product.getName());
        pdv.setPrice(product.getPrice());
        pdv.setStatus(product.getStatus());
        pdv.setStock(product.getStock());
        pdv.setSubtitle(product.getSubtitle());
        /**
         * imageHost：从配置文件中获取
         * parentCategoryId：
         * createTime
         * updateTime
         */
        pdv.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix", "http://images.sherman.fun"));
        Category category = categoryDao.selectCategoryByPrimaryKey(product.getCategoryId());
        if (category == null) {
            // 认为当前product为根节点
            pdv.setParentCategoryId(0);
        } else {
            pdv.setParentCategoryId(category.getParentId());
        }
        pdv.setCreateTime(DateTimeUtil.dateToString(product.getCreateTime()));
        pdv.setUpdateTime(DateTimeUtil.dateToString(product.getUpdateTime()));
        return pdv;
    }

    @Override
    public ServerResponse<PageInfo> getProductList(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Product> productList = productDao.selectList();
        List<ProductListVo> productListVo = Lists.newArrayList();
        for (Product product : productList) {
            ProductListVo plv = assembleProductListVo(product);
            productListVo.add(plv);
        }
        PageInfo pageInfo = new PageInfo<>(productList);
        pageInfo.setList(productListVo);
        return ServerResponse.buildSuccessByData(pageInfo);
    }


    private ProductListVo assembleProductListVo(Product product) {
        ProductListVo plv = new ProductListVo();
        plv.setId(product.getId());
        plv.setName(product.getName());
        plv.setSubtitle(product.getSubtitle());
        plv.setCategoryId(product.getCategoryId());
        plv.setMainImage(product.getMainImage());
        plv.setPrice(product.getPrice());
        plv.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix", "http://images.sherman.fun"));
        plv.setStatus(product.getStatus());
        return plv;
    }

    @Override
    public ServerResponse searchByProductOrId(String productName, Integer productId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        if (StringUtils.isNotBlank(productName)) {
            productName = "%" + productName + "%";
        }
        List<Product> productList = productDao.searchByProductOrId(productName, productId);
        List<ProductListVo> productListVos = Lists.newArrayList();
        for (Product product : productList) {
            ProductListVo plv = assembleProductListVo(product);
            productListVos.add(plv);
        }
        PageInfo pageInfo = new PageInfo<>(productList);
        pageInfo.setList(productListVos);
        return ServerResponse.buildSuccessByData(pageInfo);
    }

    @Override
    public ServerResponse<ProductDetailVo> getProductDetail(Integer productId) {
        if (productId == null) {
            return ServerResponse.buildErrorByCode(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = productDao.selectProductById(productId);
        if (product == null) {
            return ServerResponse.buildErrorByMsg("产品已下架或者删除");
        }
        if (product.getStatus() != Const.ProductStatusEnum.ON_SALE.getCode()) {
            return ServerResponse.buildErrorByMsg("产品已经下架");
        }
        ProductDetailVo pdv = assembleProductDetailVo(product);
        return ServerResponse.buildSuccessByData(pdv);
    }

    @Override
    public ServerResponse<PageInfo> getProductByKeywordAndCategoryId(String keyword, Integer categoryId, Integer pageNum, Integer pageSize, String orderBy) {
        if (StringUtils.isBlank(keyword) && categoryId == null) {
            return ServerResponse.buildErrorByCode(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        List<Integer> categoryIdList = new ArrayList<>();
        if (categoryId != null) {
            Category category = categoryDao.selectCategoryByPrimaryKey(categoryId);
            if (category == null && StringUtils.isBlank(keyword)) {
                // 没有该分类且没有该关键字，直接返回空集，不报错
                PageHelper.startPage(pageNum, pageSize);
                List<ProductListVo> productListVos = Lists.newArrayList();
                PageInfo<ProductListVo> pageInfo = new PageInfo<>(productListVos);
                return ServerResponse.buildSuccessByData(pageInfo);
            }
            categoryIdList = iCategoryService.getAllChildCategory(categoryId).getData();
        }
        if (StringUtils.isNotBlank(keyword)) {
            keyword = "%" + keyword + "%";
        }
        PageHelper.startPage(pageNum, pageSize);
        // 排序处理
        if (StringUtils.isNotBlank(orderBy)) {
            if (Const.ProductListOrderBy.Price_ASC_DESC.contains(orderBy)) {
                String[] orderByArray = orderBy.split("_");
                // PageHelper排序规则："price asc"或者"price desc"
                PageHelper.orderBy(orderByArray[0] + " " + orderByArray[1]);
            }
        }
        List<Product> productList = productDao.selectByKeywordAndCategoryIds(StringUtils.isBlank(keyword) ? null : keyword,
                categoryIdList.isEmpty() ? null : categoryIdList);
        List<ProductListVo> productListVos = Lists.newArrayList();
        for (Product product : productList) {
            ProductListVo plv = assembleProductListVo(product);
            productListVos.add(plv);
        }
        PageInfo pageInfo = new PageInfo<>(productList);
        pageInfo.setList(productListVos);
        return ServerResponse.buildSuccessByData(pageInfo);
    }
}
