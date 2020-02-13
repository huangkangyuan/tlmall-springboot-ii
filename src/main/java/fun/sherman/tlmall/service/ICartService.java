package fun.sherman.tlmall.service;

import fun.sherman.tlmall.common.ServerResponse;
import fun.sherman.tlmall.vo.CartVo;

/**
 * @author sherman
 */
public interface ICartService {
    ServerResponse<CartVo> add(Integer id, Integer productId, Integer count);

    ServerResponse<CartVo> update(Integer id, Integer productId, Integer count);

    ServerResponse<CartVo> delete(Integer id, String productIds);

    ServerResponse<CartVo> list(Integer id);

    ServerResponse<CartVo> selectOrUnSelect(Integer userId, Integer productId, int unchecked);

    ServerResponse<Integer> getCartProductCount(Integer id);
}
