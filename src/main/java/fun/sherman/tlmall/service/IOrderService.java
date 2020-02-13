package fun.sherman.tlmall.service;

import com.github.pagehelper.PageInfo;
import fun.sherman.tlmall.common.ServerResponse;
import fun.sherman.tlmall.vo.OrderVo;

import java.util.Map;

/**
 * @author sherman
 */
public interface IOrderService {
    ServerResponse pay(Integer userId, long orderNo, String filepath);

    ServerResponse alipayCallback(Map<String, String> params);

    ServerResponse queryOrderPayStatus(Integer userId, Long orderNo);

    ServerResponse createOrderNo(Integer userId, Long shippingId);

    ServerResponse<String> cancel(Integer userId, long orderNo);

    ServerResponse getOrderCartProduct(Integer userId);

    ServerResponse getDetails(Integer userId, Long orderNo);

    ServerResponse getList(Integer userId, Integer pageNum, Integer pageSize);

    ServerResponse<PageInfo> manageOrderList(Integer pageNum, Integer pageSize);

    ServerResponse<OrderVo> manageOrderDetails(Long orderNo);

    ServerResponse<PageInfo> mangeOrderSearch(Long orderNo, Integer pageNum, Integer pageSize);

    ServerResponse manageSendGoods(Long orderNo);

    void closeOrder(int hour);
}
