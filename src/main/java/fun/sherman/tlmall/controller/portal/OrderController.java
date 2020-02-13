package fun.sherman.tlmall.controller.portal;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.demo.trade.config.Configs;
import com.google.common.collect.Maps;
import fun.sherman.tlmall.common.Const;
import fun.sherman.tlmall.common.ResponseCode;
import fun.sherman.tlmall.common.ServerResponse;
import fun.sherman.tlmall.domain.User;
import fun.sherman.tlmall.service.IOrderService;
import fun.sherman.tlmall.util.CookieUtil;
import fun.sherman.tlmall.util.JacksonUtil;
import fun.sherman.tlmall.util.ShardedRedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * @author sherman
 */
@Controller
@RequestMapping("/order")
@ResponseBody
public class OrderController {

    private static Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    IOrderService iOrderService;

    @RequestMapping(value = "create.do", method = RequestMethod.POST)
    public ServerResponse create(HttpServletRequest request, @RequestParam("shipping_id") long shippingId) {
        String userLoginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isEmpty(userLoginToken)) {
            return ServerResponse.buildErrorByCode(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        String userJsonStr = ShardedRedisUtil.get(userLoginToken);
        User user = JacksonUtil.stringToBean(userJsonStr, User.class);
        if (user == null) {
            return ServerResponse.buildErrorByCode(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.createOrderNo(user.getId(), shippingId);
    }

    @RequestMapping(value = "cancel.do", method = RequestMethod.POST)
    public ServerResponse<String> cancel(HttpServletRequest request, @RequestParam("order_no") long orderNo) {
        String userLoginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isEmpty(userLoginToken)) {
            return ServerResponse.buildErrorByCode(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        String userJsonStr = ShardedRedisUtil.get(userLoginToken);
        User user = JacksonUtil.stringToBean(userJsonStr, User.class);
        if (user == null) {
            return ServerResponse.buildErrorByCode(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.cancel(user.getId(), orderNo);
    }

    /**
     * 获取购物车中选中的商品
     * 注意查询出来的OrderProductVo对象中orderNo应该为null，此时商品还在购物车中
     *
     * @return OrderProductVo封装进入ServerResponse
     */
    @RequestMapping(value = "get_order_cart_product.do", method = RequestMethod.POST)
    public ServerResponse getOrderCartProduct(HttpServletRequest request) {
        String userLoginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isEmpty(userLoginToken)) {
            return ServerResponse.buildErrorByCode(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        String userJsonStr = ShardedRedisUtil.get(userLoginToken);
        User user = JacksonUtil.stringToBean(userJsonStr, User.class);
        if (user == null) {
            return ServerResponse.buildErrorByCode(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.getOrderCartProduct(user.getId());
    }

    @RequestMapping(value = "details.do", method = RequestMethod.POST)
    public ServerResponse getDetails(HttpServletRequest request, @RequestParam("order_no") Long orderNo) {
        String userLoginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isEmpty(userLoginToken)) {
            return ServerResponse.buildErrorByCode(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        String userJsonStr = ShardedRedisUtil.get(userLoginToken);
        User user = JacksonUtil.stringToBean(userJsonStr, User.class);
        if (user == null) {
            return ServerResponse.buildErrorByCode(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.getDetails(user.getId(), orderNo);
    }

    @RequestMapping(value = "list.do", method = RequestMethod.POST)
    public ServerResponse getList(HttpServletRequest request,
                                  @RequestParam(value = "page_num", defaultValue = "1") Integer pageNum,
                                  @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize) {
        String userLoginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isEmpty(userLoginToken)) {
            return ServerResponse.buildErrorByCode(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        String userJsonStr = ShardedRedisUtil.get(userLoginToken);
        User user = JacksonUtil.stringToBean(userJsonStr, User.class);
        if (user == null) {
            return ServerResponse.buildErrorByCode(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.getList(user.getId(), pageNum, pageSize);
    }

    @RequestMapping(value = "pay.do", method = RequestMethod.POST)
    public ServerResponse pay(HttpServletRequest request, @RequestParam("order_no") long orderNo) {
        String userLoginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isEmpty(userLoginToken)) {
            return ServerResponse.buildErrorByCode(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        String userJsonStr = ShardedRedisUtil.get(userLoginToken);
        User user = JacksonUtil.stringToBean(userJsonStr, User.class);
        if (user == null) {
            return ServerResponse.buildErrorByCode(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        String filepath = request.getSession().getServletContext().getRealPath(Const.UPLOAD_PATH);
        return iOrderService.pay(user.getId(), orderNo, filepath);
    }

    @RequestMapping(value = "alipay_callback.do", method = RequestMethod.POST)
    public Object alipayCallback(HttpServletRequest request) {
        Map<String, String[]> requestParams = request.getParameterMap();
        Map<String, String> params = Maps.newHashMap();
        for (String key : requestParams.keySet()) {
            String[] values = requestParams.get(key);
            String valueStr = String.join("-", values);
            params.put(key, valueStr);
        }
        logger.info("支付宝回调结果,sing:{},trade_status:{},参数:{}", params.get("sign"), params.get("trade_status"), params.toString());
        /**
         * 验证回调的正确性，是否是支付宝回调的，同时避免重复通知
         */
        // 支付宝文档要求验证回调正确性时需要移除：sign、sign_type两个key，
        // sdk中已经移除了sign字段，需要手动移除sign_type字段
        params.remove("sign_type");
        try {
            boolean rsaV2validate = AlipaySignature.rsaCheckV2(params, Configs.getAlipayPublicKey(), "utf-8", Configs.getSignType());
            if (!rsaV2validate) {
                return ServerResponse.buildErrorByMsg("RSA2校验失败");
            }
        } catch (AlipayApiException e) {
            logger.error("RSA2校验失败", e);
        }
        /**
         * 对回调结果进行一些校验：订单是否合法，避免重复调用
         */
        ServerResponse serverResponse = iOrderService.alipayCallback(params);
        if (serverResponse.isSuccess()) {
            return Const.AlipayCallback.RESPONSE_SUCCESS;
        } else {
            return Const.AlipayCallback.RESPONSE_FAILED;
        }
    }

    @RequestMapping(value = "query_order_pay_status.do", method = RequestMethod.POST)
    public ServerResponse<Boolean> queryOrderPayStatus(HttpServletRequest request, @RequestParam("order_no") Long orderNo) {
        String userLoginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isEmpty(userLoginToken)) {
            return ServerResponse.buildErrorByCode(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        String userJsonStr = ShardedRedisUtil.get(userLoginToken);
        User user = JacksonUtil.stringToBean(userJsonStr, User.class);
        if (user == null) {
            return ServerResponse.buildErrorByCode(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        ServerResponse serverResponse = iOrderService.queryOrderPayStatus(user.getId(), orderNo);
        if (serverResponse.isSuccess()) {
            return ServerResponse.buildSuccessByData(true);
        } else {
            return ServerResponse.buildSuccessByData(false);
        }
    }
}
