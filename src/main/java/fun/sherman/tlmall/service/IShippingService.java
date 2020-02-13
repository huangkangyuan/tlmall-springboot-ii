package fun.sherman.tlmall.service;

import fun.sherman.tlmall.common.ServerResponse;
import fun.sherman.tlmall.domain.Shipping;

/**
 * @author sherman
 */
public interface IShippingService {
    ServerResponse add(Integer userId, Shipping shipping);

    ServerResponse deleteByUserIdAndShippingId(Integer userId, Integer shippingId);

    ServerResponse updateShippingSelective(Integer userId, Shipping shipping);

    ServerResponse<Shipping> selectByShippingIdAndUserId(Integer userId, Integer shippingId);

    ServerResponse listAllShippingInfo(Integer userId, Integer pageNum, Integer pageSize);
}
