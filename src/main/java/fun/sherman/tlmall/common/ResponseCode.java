package fun.sherman.tlmall.common;

/**
 * 返回值和返回信息枚举类封装
 *
 * @author sherman
 */
public enum ResponseCode {
    SUCCESS(0, "success"),

    ERROR(1, "error"),

    ILLEGAL_ARGUMENT(2, "illegal argument"),

    NEED_LOGIN(10, "need login");

    private final int code;
    private final String desc;

    ResponseCode(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
