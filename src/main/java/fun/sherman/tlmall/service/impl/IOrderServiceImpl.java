package fun.sherman.tlmall.service.impl;

import com.alipay.api.AlipayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.alipay.demo.trade.utils.ZxingUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import fun.sherman.tlmall.common.Const;
import fun.sherman.tlmall.common.ServerResponse;
import fun.sherman.tlmall.dao.*;
import fun.sherman.tlmall.domain.*;
import fun.sherman.tlmall.service.IOrderService;
import fun.sherman.tlmall.util.*;
import fun.sherman.tlmall.vo.OrderItemVo;
import fun.sherman.tlmall.vo.OrderProductVo;
import fun.sherman.tlmall.vo.OrderVo;
import fun.sherman.tlmall.vo.ShippingVo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.math.BigDecimal;
import java.util.*;

/**
 * @author sherman
 */
@Service("iOrderService")
public class IOrderServiceImpl implements IOrderService {
    // 支付宝当面付2.0服务
    private static AlipayTradeService tradeService;

    static {
        /**
         * 一定要在创建AlipayTradeService之前调用Configs.init()设置默认参数
         * Configs会读取classpath下的zfbinfo.properties文件配置信息，如果找不到该文件则确认该文件是否在classpath目录
         */
        Configs.init("zfbinfo.properties");

        tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();
    }

    private static Logger logger = LoggerFactory.getLogger(IOrderServiceImpl.class);

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private OrderItemDao orderItemDao;

    @Autowired
    private PayInfoDao payInfoDao;

    @Autowired
    private CartDao cartDao;

    @Autowired
    private ProductDao productDao;

    @Autowired
    private ShippingDao shippingDao;

    @Override
    public ServerResponse createOrderNo(Integer userId, Long shippingId) {
        // 根据用户id从购物车中获取数据
        List<Cart> cartList = cartDao.selectCheckedCartsByUserId(userId);
        ServerResponse serverResponse = getCartOrderItem(userId, cartList);
        if (!serverResponse.isSuccess()) {
            return serverResponse;
        }
        List<OrderItem> orderItemList = (List<OrderItem>) serverResponse.getData();
        if (CollectionUtils.isEmpty(orderItemList)) {
            return ServerResponse.buildErrorByMsg("购物车为空");
        }
        BigDecimal payment = getOrderTotalPrice(orderItemList);
        Order order = assembleOrder(userId, shippingId, payment);
        if (order == null) {
            return ServerResponse.buildErrorByMsg("生成订单错误");
        }
        for (OrderItem orderItem : orderItemList) {
            orderItem.setOrderNo(order.getOrderNo());
        }
        // 批量插入
        orderItemDao.batchInsert(orderItemList);
        // 减少库存
        reduceProductStock(orderItemList);
        // 清空购物车
        clearCart(cartList);
        // 组装OrderVo对象
        OrderVo orderVo = assembleOrderVo(order, orderItemList);
        return ServerResponse.buildSuccessByData(orderVo);
    }

    @Override
    public ServerResponse<String> cancel(Integer userId, long orderNo) {
        Order order = orderDao.selectOrderByUserIdAndOrderNo(userId, orderNo);
        if (order == null) {
            return ServerResponse.buildSuccessByMsg("该用户订单不存在");
        }
        // 只有订单处于NO_PAY状态才能取消
        if (order.getStatus() != Const.OrderStatusEnum.NO_PAY.getCode()) {
            return ServerResponse.buildErrorByMsg("订单状态异常");
        }
        Order updateOrder = new Order();
        updateOrder.setId(order.getId());
        updateOrder.setStatus(Const.OrderStatusEnum.CANCELED.getCode());
        int rowCount = orderDao.updateSelective(updateOrder);
        if (rowCount > 0) {
            return ServerResponse.buildSuccess();
        }
        return ServerResponse.buildError();
    }

    @Override
    public ServerResponse getOrderCartProduct(Integer userId) {
        OrderProductVo orderProductVo = new OrderProductVo();
        // 从购物车中获取数据
        List<Cart> cartList = cartDao.selectCheckedCartsByUserId(userId);
        ServerResponse serverResponse = getCartOrderItem(userId, cartList);
        if (!serverResponse.isSuccess()) {
            return serverResponse;
        }
        List<OrderItem> orderItemList = (List<OrderItem>) serverResponse.getData();
        List<OrderItemVo> orderItemVoList = Lists.newArrayList();
        BigDecimal payment = new BigDecimal("0");
        for (OrderItem orderItem : orderItemList) {
            payment = BigDecimalUtil.multiply(payment.doubleValue(), orderItem.getTotalPrice().doubleValue());
            orderItemVoList.add(assembleOrderItemVo(orderItem));
        }
        orderProductVo.setProductTotalPrice(payment);
        orderProductVo.setOrderItemVoList(orderItemVoList);
        orderProductVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        return ServerResponse.buildSuccessByData(orderProductVo);
    }

    @Override
    public ServerResponse getDetails(Integer userId, Long orderNo) {
        Order order = orderDao.selectOrderByUserIdAndOrderNo(userId, orderNo);
        if (order == null) {
            return ServerResponse.buildErrorByMsg("该用户订单不存在");
        }
        List<OrderItem> orderItemList = orderItemDao.getOrderItemsByUserIdAndOrderNo(userId, orderNo);
        OrderVo orderVo = assembleOrderVo(order, orderItemList);
        return ServerResponse.buildSuccessByData(orderVo);
    }

    /**
     * 门户获取当前用户的订单
     */
    @Override
    public ServerResponse getList(Integer userId, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Order> orderList = orderDao.selectOrderListByUserId(userId);
        List<OrderVo> orderVoList = assembleOrderVoList(orderList, userId);
        PageInfo pageResult = new PageInfo(orderList);
        pageResult.setList(orderVoList);
        return ServerResponse.buildSuccessByData(pageResult);
    }

    /**
     * 管理员获取所有订单
     */
    @Override
    public ServerResponse<PageInfo> manageOrderList(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Order> orderList = orderDao.selectAllOrder();
        List<OrderVo> orderVoList = assembleOrderVoList(orderList, null);
        PageInfo pageInfo = new PageInfo(orderList);
        pageInfo.setList(orderVoList);
        return ServerResponse.buildSuccessByData(pageInfo);
    }

    @Override
    public ServerResponse<OrderVo> manageOrderDetails(Long orderNo) {
        Order order = orderDao.selectOrderByOrderNo(orderNo);
        if (order == null) {
            return ServerResponse.buildErrorByMsg("订单不存在");
        }
        List<OrderItem> orderItemList = orderItemDao.getOrderItemsByOrderNo(orderNo);
        OrderVo orderVo = assembleOrderVo(order, orderItemList);
        return ServerResponse.buildSuccessByData(orderVo);
    }

    @Override
    public ServerResponse<PageInfo> mangeOrderSearch(Long orderNo, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Order order = orderDao.selectOrderByOrderNo(orderNo);
        if (order == null) {
            return ServerResponse.buildErrorByMsg("订单不存在");
        }
        List<OrderItem> orderItemList = orderItemDao.getOrderItemsByOrderNo(orderNo);
        PageInfo pageInfo = new PageInfo(Lists.newArrayList(order));
        OrderVo orderVo = assembleOrderVo(order, orderItemList);
        pageInfo.setList(Lists.newArrayList(orderVo));
        return ServerResponse.buildSuccessByData(pageInfo);
    }

    @Override
    public ServerResponse manageSendGoods(Long orderNo) {
        Order order = orderDao.selectOrderByOrderNo(orderNo);
        if (order == null) {
            return ServerResponse.buildErrorByMsg("订单不存在");
        }
        if (order.getStatus() == Const.OrderStatusEnum.PAID.getCode()) {
            order.setStatus(Const.OrderStatusEnum.SHIPPED.getCode());
            order.setSendTime(new Date());
            orderDao.updateSelective(order);
            return ServerResponse.buildErrorByMsg("发货成功");
        }
        return ServerResponse.buildErrorByMsg("订单状态异常");
    }

    @Override
    public void closeOrder(int hour) {
        Date closeDate = DateUtils.addHours(new Date(), -hour);
        List<Order> orderList = orderDao.selectOrderStatusByCreateTime(Const.OrderStatusEnum.NO_PAY.getCode(), DateTimeUtil.dateToString(closeDate));
        for (Order order : orderList) {
            List<OrderItem> orderItemList = orderItemDao.getOrderItemsByOrderNo(order.getOrderNo());
            for (OrderItem orderItem : orderItemList) {
                Integer stock = productDao.selectStockByProductId(orderItem.getProductId());
                // 已生成的订单，被删除的情况，实际中一般不会直接删除订单
                if (stock == null) {
                    continue;
                }
                Product product = new Product();
                product.setId(orderItem.getProductId());
                product.setStock(stock + orderItem.getQuantity());
                productDao.updateProduct(product);

            }
            orderDao.closeOrderByOrderId(order.getId());
            logger.info("关闭订单OrderNo:{}", order.getOrderNo());
        }
    }

    private List<OrderVo> assembleOrderVoList(List<Order> orderList, Integer userId) {
        List<OrderVo> orderVoList = Lists.newArrayList();
        for (Order order : orderList) {
            List<OrderItem> orderItemList;
            if (userId == null) {
                orderItemList = orderItemDao.getOrderItemsByUserId(userId);
            } else {
                orderItemList = orderItemDao.getOrderItemsByUserIdAndOrderNo(userId, order.getOrderNo());
            }
            OrderVo orderVo = assembleOrderVo(order, orderItemList);
            orderVoList.add(orderVo);
        }
        return orderVoList;
    }

    private OrderVo assembleOrderVo(Order order, List<OrderItem> orderItemList) {
        OrderVo orderVo = new OrderVo();
        orderVo.setOrderNo(order.getOrderNo());
        orderVo.setPayment(order.getPayment());
        orderVo.setPaymentType(order.getPaymentType());
        orderVo.setPaymentTypeDesc(Const.PaymentTypeEnum.codeOf(order.getPaymentType()).getMsg());
        orderVo.setPostage(order.getPostage());
        orderVo.setStatus(order.getStatus());
        orderVo.setStatusDesc(Const.OrderStatusEnum.codeOf(order.getStatus()).getMsg());
        orderVo.setShippingId(order.getShippingId());
        Shipping shipping = shippingDao.selectShippingByPrimaryKey(order.getShippingId());
        if (shipping != null) {
            orderVo.setReceiverName(shipping.getReceiverName());
            orderVo.setShippingVo(assembleShippingVo(shipping));
        }
        orderVo.setPaymentTime(DateTimeUtil.dateToString(order.getPaymentTime()));
        orderVo.setCreateTime(DateTimeUtil.dateToString(order.getCreateTime()));
        orderVo.setCloseTime(DateTimeUtil.dateToString(order.getCloseTime()));
        orderVo.setSendTime(DateTimeUtil.dateToString(order.getSendTime()));
        orderVo.setEndTime(DateTimeUtil.dateToString(order.getEndTime()));
        orderVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        List<OrderItemVo> orderItemVoList = Lists.newArrayList();
        for (OrderItem orderItem : orderItemList) {
            orderItemVoList.add(assembleOrderItemVo(orderItem));
        }
        orderVo.setOrderItemVoList(orderItemVoList);
        return orderVo;
    }

    private OrderItemVo assembleOrderItemVo(OrderItem orderItem) {
        OrderItemVo orderItemVo = new OrderItemVo();
        orderItemVo.setOrderNo(orderItem.getOrderNo());
        orderItemVo.setTotalPrice(orderItem.getTotalPrice());
        orderItemVo.setCurrentUnitPrice(orderItem.getCurrentUnitPrice());
        orderItemVo.setQuantity(orderItem.getQuantity());
        orderItemVo.setProductImage(orderItem.getProductImage());
        orderItemVo.setProductName(orderItem.getProductName());
        orderItemVo.setProductId(orderItem.getProductId());
        orderItemVo.setCreateTime(DateTimeUtil.dateToString(orderItem.getCreateTime()));
        return orderItemVo;
    }

    private ShippingVo assembleShippingVo(Shipping shipping) {
        ShippingVo shippingVo = new ShippingVo();
        shippingVo.setReceiverName(shipping.getReceiverName());
        shippingVo.setReceiverAddress(shipping.getReceiverAddress());
        shippingVo.setReceiverCity(shipping.getReceiverCity());
        shippingVo.setReceiverDistrict(shipping.getReceiverDistrict());
        shippingVo.setReceiverMobile(shipping.getReceiverMobile());
        shippingVo.setReceiverProvince(shipping.getReceiverProvince());
        shippingVo.setReceiverZip(shipping.getReceiverZip());
        return shippingVo;
    }

    private void clearCart(List<Cart> cartList) {
        for (Cart cart : cartList) {
            cartDao.deleteByPrimaryKey(cart.getId());
        }
    }

    private void reduceProductStock(List<OrderItem> orderItemList) {
        for (OrderItem orderItem : orderItemList) {
            Product product = productDao.selectProductById(orderItem.getProductId());
            product.setStock(product.getStock() - orderItem.getQuantity());
            productDao.updateProduct(product);
        }
    }

    private Order assembleOrder(Integer userId, Long shippingId, BigDecimal payment) {
        Order order = new Order();
        long orderNo = generateOrderNo();
        order.setOrderNo(orderNo);
        order.setStatus(Const.OrderStatusEnum.NO_PAY.getCode());
        order.setPostage(0);
        order.setPayment(payment);
        order.setPaymentType(Const.PaymentTypeEnum.ONLINE_PAY.getCode());
        order.setShippingId(shippingId.intValue());
        order.setUserId(userId);
        int rowCount = orderDao.insert(order);
        if (rowCount > 0) {
            return order;
        }
        return null;
    }

    private long generateOrderNo() {
        long timeMillis = System.currentTimeMillis();
        return timeMillis + new Random().nextInt(100);
    }

    private BigDecimal getOrderTotalPrice(List<OrderItem> orderItemList) {
        BigDecimal totalPrice = new BigDecimal("0");
        for (OrderItem orderItem : orderItemList) {
            totalPrice = BigDecimalUtil.add(totalPrice.doubleValue(), orderItem.getTotalPrice().doubleValue());
        }
        return totalPrice;
    }

    private ServerResponse getCartOrderItem(Integer userId, List<Cart> cartList) {
        List<OrderItem> orderItemList = new ArrayList<>();
        if (CollectionUtils.isEmpty(cartList)) {
            return ServerResponse.buildErrorByMsg("购物车为空");
        }
        for (Cart cart : cartList) {
            OrderItem orderItem = new OrderItem();
            Product product = productDao.selectProductById(cart.getProductId());
            if (Const.ProductStatusEnum.ON_SALE.getCode() != product.getStatus()) {
                return ServerResponse.buildErrorByMsg("产品：" + product.getName() + "不在在售状态");
            }
            if (cart.getQuantity() > product.getStock()) {
                return ServerResponse.buildErrorByMsg("产品：" + product.getName() + "库存不足");
            }
            orderItem.setUserId(userId);
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getMainImage());
            orderItem.setCurrentUnitPrice(product.getPrice());
            orderItem.setQuantity(cart.getQuantity());
            orderItem.setTotalPrice(BigDecimalUtil.multiply(product.getPrice().doubleValue(), cart.getQuantity()));
            orderItemList.add(orderItem);
        }
        return ServerResponse.buildSuccessByData(orderItemList);
    }

    @Override
    public ServerResponse pay(Integer userId, long orderNo, String path) {
        Order order = orderDao.selectOrderByUserIdAndOrderNo(userId, orderNo);
        Map<String, String> resultMap = new HashMap<>();
        if (order == null) {
            return ServerResponse.buildErrorByMsg("未查询到对应订单");
        }
        resultMap.put("orderNo", String.valueOf(order.getOrderNo()));
        // (必填) 商户网站订单系统中唯一订单号，64个字符以内，只能包含字母、数字、下划线，
        // 需保证商户系统端不能重复，建议通过数据库sequence生成，
        String outTradeNo = order.getOrderNo().toString();

        // (必填) 订单标题，粗略描述用户的支付目的。如“xxx品牌xxx门店当面付扫码消费”
        String subject = new StringBuilder().append("tlmall扫码支付，订单号：").append(outTradeNo).toString();

        // (必填) 订单总金额，单位为元，不能超过1亿元
        // 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
        String totalAmount = order.getPayment().toString();

        // (可选) 订单不可打折金额，可以配合商家平台配置折扣活动，如果酒水不参与打折，则将对应金额填写至此字段
        // 如果该值未传入,但传入了【订单总金额】,【打折金额】,则该值默认为【订单总金额】-【打折金额】
        String undiscountableAmount = "0";

        // 卖家支付宝账号ID，用于支持一个签约账号下支持打款到不同的收款账号，(打款到sellerId对应的支付宝账号)
        // 如果该字段为空，则默认为与支付宝签约的商户的PID，也就是appid对应的PID
        String sellerId = "";

        // 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品2件共15.00元"
        String body = new StringBuilder().append("订单号：").append(outTradeNo).append("，共消费：").append(totalAmount).toString();

        // 商户操作员编号，添加此参数可以为商户操作员做销售统计
        String operatorId = "收银员-01";

        // (必填) 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
        String storeId = "门店编号-01";

        // 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
        ExtendParams extendParams = new ExtendParams();
        extendParams.setSysServiceProviderId("2088100200300400500");

        // 支付超时，默认定义为120分钟
        String timeoutExpress = "60m";

        // 商品明细列表，需填写购买商品详细信息，
        // 创建一个商品信息，参数含义分别为商品id（使用国标）、名称、单价（单位为分）、数量，如果需要添加商品类别，详见GoodsDetail
        // 创建好一个商品后添加至商品明细列表
        List<GoodsDetail> goodsDetailList = new ArrayList<>();
        List<OrderItem> orderItemList = orderItemDao.getOrderItemsByUserIdAndOrderNo(userId, orderNo);
        for (OrderItem orderItem : orderItemList) {
            GoodsDetail goodsDetail = GoodsDetail.newInstance(orderItem.getProductId().toString(), orderItem.getProductName(),
                    BigDecimalUtil.multiply(orderItem.getCurrentUnitPrice().doubleValue(), 100.0).longValue(),
                    orderItem.getQuantity());
            goodsDetailList.add(goodsDetail);
        }
        // 创建扫码支付请求builder，设置请求参数
        AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder()
                .setSubject(subject).setTotalAmount(totalAmount).setOutTradeNo(outTradeNo)
                .setUndiscountableAmount(undiscountableAmount).setSellerId(sellerId).setBody(body)
                .setOperatorId(operatorId).setStoreId(storeId).setExtendParams(extendParams)
                .setTimeoutExpress(timeoutExpress)
                //支付宝服务器主动通知商户服务器里指定的页面http路径,根据需要设置
                .setNotifyUrl(PropertiesUtil.getProperty("alipay.callback.url"))
                .setGoodsDetailList(goodsDetailList);

        AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);
        switch (result.getTradeStatus()) {
            case SUCCESS:
                logger.info("支付宝预下单成功: )");

                AlipayTradePrecreateResponse response = result.getResponse();
                dumpResponse(response);

                // 需要修改为运行机器上的路径
                File folder = new File(path);
                if (!folder.exists()) {
                    folder.setWritable(true);
                    folder.mkdirs();
                }
                String qrPath = String.format(path + "\\qr-%s.png", response.getOutTradeNo());
                String qrFileName = String.format("\\qr-%s.png", response.getOutTradeNo());
                ZxingUtils.getQRCodeImge(response.getQrCode(), 256, qrPath);
                File targetFile = new File(path, qrFileName);
                FtpUtil.uploadFile(Lists.newArrayList(targetFile));
                logger.info("filePath:" + qrPath);
                String qrUrl = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFile.getName();
                resultMap.put("qrUrl", qrUrl);
                return ServerResponse.buildSuccessByData(resultMap);

            case FAILED:
                logger.error("支付宝预下单失败!!!");
                return ServerResponse.buildErrorByMsg("支付宝预下单失败");

            case UNKNOWN:
                logger.error("系统异常，预下单状态未知!!!");
                return ServerResponse.buildErrorByMsg("系统异常，预下单状态未知");

            default:
                logger.error("不支持的交易状态，交易返回异常!!!");
                return ServerResponse.buildErrorByMsg("不支持的交易状态，交易返回异常");
        }
    }

    private void dumpResponse(AlipayResponse response) {
        if (response != null) {
            logger.info(String.format("code:%s, msg:%s", response.getCode(), response.getMsg()));
            if (StringUtils.isNotEmpty(response.getSubCode())) {
                logger.info(String.format("subCode:%s, subMsg:%s", response.getSubCode(),
                        response.getSubMsg()));
            }
            logger.info("body:" + response.getBody());
        }
    }

    @Override
    public ServerResponse alipayCallback(Map<String, String> params) {
        long orderNo = Long.parseLong(params.get("out_trade_no"));
        String tradeNo = params.get("trade_no");
        String tradeStatus = params.get("trade_status");
        Order order = orderDao.selectOrderByOrderNo(orderNo);
        if (order == null) {
            return ServerResponse.buildErrorByMsg("非法订单号，请忽略");
        }
        if (order.getStatus() >= Const.OrderStatusEnum.PAID.getCode()) {
            // 注意订单重复调用，但是应该属于success
            return ServerResponse.buildSuccessByMsg("订单重复调用");
        }
        if (Const.AlipayCallback.TRADE_STATUS_TRADE_SUCCESS.equals(tradeStatus)) {
            order.setPaymentTime(DateTimeUtil.stringToDate(params.get("gmt_payment")));
            order.setStatus(Const.OrderStatusEnum.PAID.getCode());
            orderDao.updateSelective(order);
        }
        PayInfo payInfo = new PayInfo();
        payInfo.setUserId(order.getUserId());
        payInfo.setOrderNo(order.getOrderNo());
        payInfo.setPayPlatform(Const.PayPlatform.ALIPAY.getCode());
        payInfo.setPlatformStatus(tradeStatus);
        payInfo.setPlatformNumber(tradeNo);
        payInfoDao.insert(payInfo);
        return ServerResponse.buildSuccess();
    }

    @Override
    public ServerResponse queryOrderPayStatus(Integer userId, Long orderNo) {
        Order order = orderDao.selectOrderByUserIdAndOrderNo(userId, orderNo);
        if (order == null) {
            return ServerResponse.buildErrorByMsg("用户没有该订单");
        }
        if (order.getStatus() >= Const.OrderStatusEnum.PAID.getCode()) {
            return ServerResponse.buildSuccess();
        } else {
            return ServerResponse.buildError();
        }
    }

}
