package fun.sherman.tlmall.service.impl;

import com.google.common.collect.Lists;
import fun.sherman.tlmall.common.Const;
import fun.sherman.tlmall.common.ResponseCode;
import fun.sherman.tlmall.common.ServerResponse;
import fun.sherman.tlmall.dao.CartDao;
import fun.sherman.tlmall.dao.ProductDao;
import fun.sherman.tlmall.domain.Cart;
import fun.sherman.tlmall.domain.Product;
import fun.sherman.tlmall.service.ICartService;
import fun.sherman.tlmall.util.BigDecimalUtil;
import fun.sherman.tlmall.util.PropertiesUtil;
import fun.sherman.tlmall.vo.CartProductVo;
import fun.sherman.tlmall.vo.CartVo;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * @author sherman
 */
@Service("iCartService")
public class CartServiceImpl implements ICartService {
    @Autowired
    private CartDao cartDao;

    @Autowired
    private ProductDao productDao;

    @Override
    public ServerResponse<CartVo> add(Integer userId, Integer productId, Integer count) {
        if (productId == null || count == null) {
            return ServerResponse.buildErrorByCode(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Cart cart = cartDao.selectCartByUserIdAndProductId(userId, productId);
        // cart不存在，需要创建新的cart，并且insert
        if (cart == null) {
            Cart cartItem = new Cart();
            cartItem.setUserId(userId);
            cartItem.setProductId(productId);
            cartItem.setQuantity(count);
            cartItem.setChecked(Const.Cart.CHECKED);
            cartDao.insert(cartItem);
        } else {
            // cart存在，直接count相加即可
            count += cart.getQuantity();
            cart.setQuantity(count);
            cartDao.updateCartSelective(cart);
        }
        return list(userId);
    }

    @Override
    public ServerResponse<CartVo> update(Integer userId, Integer productId, Integer count) {
        if (productId == null || count == null) {
            return ServerResponse.buildErrorByCode(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Cart cart = cartDao.selectCartByUserIdAndProductId(userId, productId);
        if (cart != null) {
            cart.setQuantity(count);
        }
        cartDao.updateCartSelective(cart);
        return list(userId);
    }

    @Override
    public ServerResponse<CartVo> delete(Integer userId, String productIds) {
        String[] split = productIds.split(",");
        if (split.length == 0 || split.length == 1 && split[0].equals("")) {
            return ServerResponse.buildErrorByCode(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        List<String> productIdList = Lists.newArrayList();
        Collections.addAll(productIdList, split);
        cartDao.deleteByUserIdAndProductIds(userId, productIdList);
        return list(userId);
    }

    @Override
    public ServerResponse<CartVo> list(Integer userId) {
        CartVo cartVo = getCartVoLimit(userId);
        return ServerResponse.buildSuccessByData(cartVo);
    }

    /**
     * 根据productId是否为null，进行全（不）选或者部分（不）选
     * productId==null：全选或者全不选
     * productId!=null: 选择对应商品或者不选择对应商品
     *
     * @param userId    user id
     * @param productId product id，可以为null
     * @param checked   选择或者不选
     * @return 封装CartVo的ServerResponse对象
     */
    @Override
    public ServerResponse<CartVo> selectOrUnSelect(Integer userId, Integer productId, int checked) {
        cartDao.checkOrUnCheck(userId, productId, checked);
        return list(userId);
    }

    @Override
    public ServerResponse<Integer> getCartProductCount(Integer userId) {
        return ServerResponse.buildSuccessByData(cartDao.selectCartProductCount(userId));
    }

    /**
     * 1. 根据userId（已经建立索引）查询到Cart列表
     * 2. 遍历每个cart并结合productDao封装cartProductVo对象，并产生CartProductVo的列表
     * 3. 遍历期间还需要判断当前cart中商品的数量和库存的数量匹配情况，以及计算总价格
     */
    private CartVo getCartVoLimit(Integer userId) {
        CartVo cartVo = new CartVo();
        List<Cart> cartList = cartDao.selectCartsByUserId(userId);
        List<CartProductVo> cartProductVoList = Lists.newArrayList();
        BigDecimal cartTotalPrice = new BigDecimal("0");

        if (CollectionUtils.isNotEmpty(cartList)) {
            for (Cart cart : cartList) {
                CartProductVo cartProductVo = new CartProductVo();
                cartProductVo.setId(cart.getId());
                cartProductVo.setUserId(cart.getUserId());
                cartProductVo.setProductId(cart.getProductId());

                Product product = productDao.selectProductById(cart.getProductId());
                if (product != null) {
                    cartProductVo.setProductMainImage(product.getMainImage());
                    cartProductVo.setProductSubtitle(product.getSubtitle());
                    cartProductVo.setProductName(product.getName());
                    cartProductVo.setProductStatus(product.getStatus());
                    cartProductVo.setProductPrice(product.getPrice());
                    cartProductVo.setProductStock(product.getStock());
                    // 判断库存
                    int buyLimitCount = 0;
                    // 库存充足
                    if (product.getStock() >= cart.getQuantity()) {
                        buyLimitCount = cart.getQuantity();
                        cartProductVo.setQuantity(cart.getQuantity());
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_SUCCESS);
                    } else {
                        // 库存不足，购物车中商品数量设置为最大库存数量
                        buyLimitCount = product.getStock();
                        // 购物车中更新有效库存
                        Cart cartForQuantity = new Cart();
                        cartForQuantity.setId(cart.getId());
                        cartForQuantity.setQuantity(buyLimitCount);
                        cartDao.updateCartSelective(cartForQuantity);
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_FAIL);
                    }
                    cartProductVo.setQuantity(buyLimitCount);
                    // 计算当前购物车总价格
                    cartProductVo.setProductTotalPrice(BigDecimalUtil.multiply(
                            product.getPrice().doubleValue(),
                            cartProductVo.getQuantity().doubleValue()));
                    cartProductVo.setProductChecked(cart.getChecked());
                }
                // 判断当前购物车是否被勾选，如果被勾选，需要将其该购物车中商品价格增加到总价格中
                if (cart.getChecked() == Const.Cart.CHECKED) {
                    cartTotalPrice = BigDecimalUtil.add(cartTotalPrice.doubleValue(),
                            cartProductVo.getProductTotalPrice().doubleValue());
                }
                cartProductVoList.add(cartProductVo);
            }
        }
        cartVo.setCartTotalPrice(cartTotalPrice);
        cartVo.setCartProductVoList(cartProductVoList);
        // 是否全选商品
        cartVo.setAllChecked(getAllCheckedStatus(userId));
        cartVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        return cartVo;
    }

    private boolean getAllCheckedStatus(Integer userId) {
        if (userId == null) {
            return false;
        }
        return cartDao.selectCartProductCheckedStatusByUserId(userId) == 0;
    }
}
