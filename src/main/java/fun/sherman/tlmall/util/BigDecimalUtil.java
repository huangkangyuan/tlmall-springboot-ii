package fun.sherman.tlmall.util;

import java.math.BigDecimal;

/**
 * BigDecimal工具类封装
 *
 * @author sherman
 */
public class BigDecimalUtil {
    public static BigDecimal add(double v1, double v2) {
        BigDecimal bd1 = new BigDecimal(Double.toString(v1));
        BigDecimal bd2 = new BigDecimal(Double.toString(v2));
        return bd1.add(bd2);
    }

    public static BigDecimal subtract(double v1, double v2) {
        BigDecimal bd1 = new BigDecimal(Double.toString(v1));
        BigDecimal bd2 = new BigDecimal(Double.toString(v2));
        return bd1.subtract(bd2);
    }

    public static BigDecimal multiply(double v1, double v2) {
        BigDecimal bd1 = new BigDecimal(Double.toString(v1));
        BigDecimal bd2 = new BigDecimal(Double.toString(v2));
        return bd1.multiply(bd2);
    }

    public static BigDecimal divide(double v1, double v2) {
        return divide(v1, v2, 2);
    }

    public static BigDecimal divide(double v1, double v2, int scale) {
        BigDecimal bd1 = new BigDecimal(Double.toString(v1));
        BigDecimal bd2 = new BigDecimal(Double.toString(v2));
        return bd1.divide(bd2, scale, BigDecimal.ROUND_HALF_UP);
    }
}
