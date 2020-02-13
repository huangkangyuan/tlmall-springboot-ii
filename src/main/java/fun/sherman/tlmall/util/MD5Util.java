package fun.sherman.tlmall.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.rmi.runtime.Log;

import java.security.MessageDigest;

/**
 * MD5加密工具类封装
 *
 * @author sherman
 */
public class MD5Util {
    private static Logger logger = LoggerFactory.getLogger(MD5Util.class);

    private static String byteArrayToHexString(byte[] b) {
        StringBuilder resultSb = new StringBuilder();
        for (byte value : b) {
            resultSb.append(byteToHexString(value));
        }
        return resultSb.toString();
    }

    private static String byteToHexString(byte b) {
        int n = b;
        if (n < 0)
            n += 256;
        int d1 = n / 16;
        int d2 = n % 16;
        return hexDigits[d1] + hexDigits[d2];
    }

    /**
     * 返回大写MD5
     *
     * @param origin  源字符串
     * @param charset 字符集名称
     * @return MD5加密后密码
     */
    private static String MD5Encode(String origin, String charset) {
        String resultString = null;
        try {
            resultString = origin;
            MessageDigest md = MessageDigest.getInstance("MD5");
            if (charset == null || "".equals(charset)) {
                resultString = byteArrayToHexString(md.digest(resultString.getBytes()));
            } else {
                resultString = byteArrayToHexString(md.digest(resultString.getBytes(charset)));
            }
        } catch (Exception exception) {
            logger.error(exception.getMessage(), exception);
        }
        return resultString.toUpperCase();
    }

    public static String encodeWithUtf8(String origin) {
        //origin = origin + PropertiesUtil.getProperty("password.salt", "");
        return MD5Encode(origin, "utf-8");
    }


    private static final String[] hexDigits = {"0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};
}
