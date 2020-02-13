package fun.sherman.tlmall.common;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * 定义系统中各种常量
 *
 * @author sherman
 */
public class Const {
    /**
     * 当前Session中的用户
     */
    public static final String CURRENT_USER = "currentUser";

    /**
     * 用户重置密码token的前缀
     */
    public static String TOKEN_PREFIX = "token_";

    /**
     * 商品的状态：在售-1、下架-2、删除-3
     */
    public enum ProductStatusEnum {
        ON_SALE(1, "在售"),
        OFF_SALE(2, "下架"),
        DELETED(3, "删除");
        private int code;
        private String msg;

        ProductStatusEnum(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public int getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }
    }

    /**
     * 角色分类：普通用户、管理员用户
     */
    public interface Role {
        /**
         * 普通用户
         */
        int ROLE_CUSTOMER = 0;

        /**
         * 管理员用户
         */
        int ROLE_ADMIN = 1;
    }

    public static final String USERNAME = "username";

    public static final String EMAIL = "email";

    public static final String PHONE = "phone";

    /**
     * 上传文件的文件夹
     */
    public static final String UPLOAD_PATH = "upload";

    /**
     * Product排序规则：属性_asc|desc，待扩展
     */
    public interface ProductListOrderBy {
        Set<String> Price_ASC_DESC = Sets.newHashSet("price_asc", "price_desc");
    }

    /**
     * 1. 购物车中商品选中状态：CHECKED、UNCHECKED
     * 2. 返回给前端购物车中数量是否超过该商品库存数量
     */
    public interface Cart {
        int CHECKED = 1;
        int UNCHECKED = 0;
        /**
         * 库存数量大于等于购物车中数量
         */
        String LIMIT_NUM_SUCCESS = "LIMIT_NUM_SUCCESS";
        /**
         * 库存数量小于等于购物车中数量
         */
        String LIMIT_NUM_FAIL = "LIMIT_NUM_FAIL";
    }

    /**
     * Alipay的回调常亮
     */
    public interface AlipayCallback {
        String RESPONSE_SUCCESS = "success";
        String RESPONSE_FAILED = "failed";
        /**
         * 等待用户付款
         */
        String TRADE_STATUS_WAIT_BUYER_PAY = "WAIT_BUYER_PAY";
        /**
         * 交易完毕
         */
        String TRADE_STATUS_TRADE_SUCCESS = "TRADE_SUCCESS";
    }

    /**
     * 订单状态枚举类
     */
    public enum OrderStatusEnum {
        CANCELED(0, "已取消"),
        NO_PAY(10, "未支付"),
        PAID(20, "已支付"),
        SHIPPED(40, "已发货"),
        ORDER_SUCCESS(50, "订单完成"),
        ORDER_CLOSE(60, "订单关闭");
        private int code;
        private String msg;

        OrderStatusEnum(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public int getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }

        /**
         * 根据订单状态枚举类的code寻找对应的订单状态枚举类
         *
         * @param code 待查找的订单状态枚举类的code
         * @return 查找成功返回对应订单状态枚举类，查找失败抛出RuntimeException
         */
        public static OrderStatusEnum codeOf(int code) {
            for (OrderStatusEnum orderStatusEnum : values()) {
                if (orderStatusEnum.getCode() == code) {
                    return orderStatusEnum;
                }
            }
            throw new RuntimeException("没有找到对应枚举类");
        }
    }

    /**
     * 支付平台枚举类：ALIPAY-1，待扩展
     */
    public enum PayPlatform {
        ALIPAY(1, "支付宝支付");
        private int code;
        private String msg;

        PayPlatform(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public int getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }

        /**
         * 根据code查找对应的支付平台枚举类
         *
         * @param code 待查找支付平台枚举类的code
         * @return 查找成功返回对应的支付平台枚举类，查找失败抛出RuntimeException
         */
        public static PayPlatform codeOf(int code) {
            for (PayPlatform payPlatform : values()) {
                if (payPlatform.getCode() == code) {
                    return payPlatform;
                }
            }
            throw new RuntimeException("没有找到对应枚举类");
        }
    }

    /**
     * 支付类型枚举类，ONLINE_PAY-1，待扩展
     */
    public enum PaymentTypeEnum {
        ONLINE_PAY(1, "在线支付");

        PaymentTypeEnum(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        private String msg;
        private int code;

        public String getMsg() {
            return msg;
        }

        public int getCode() {
            return code;
        }

        /**
         * 根据code查找支付类型枚举类
         *
         * @param code 待查找的支付类型枚举类的code
         * @return 查找成功返回对应支付类型枚举类，查找失败抛出RuntimeException
         */
        public static PaymentTypeEnum codeOf(int code) {
            for (PaymentTypeEnum paymentTypeEnum : values()) {
                if (paymentTypeEnum.getCode() == code) {
                    return paymentTypeEnum;
                }
            }
            throw new RuntimeException("没有找到对应枚举类");
        }
    }

    public interface RedisKeyExpires {
        /**
         * 用户登录的token有效期为30分钟
         */
        int USER_LOGIN_TOKEN = 60 * 30;
        /**
         * 用户重置密码的token有效期为12小时
         */
        int USER_RESET_PASSWORD_TOKEN = 60 * 60 * 12;
    }

    public interface RedisLock {
        /**
         * 关闭订单的分布式锁
         */
        String CLOSE_ORDER_TASK_LOCK = "CLOSE_ORDER_TASK_LOCK";
    }
}
