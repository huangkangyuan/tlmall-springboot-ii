package fun.sherman.tlmall.util;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;

/**
 * joda-time 时间转换工具类
 *
 * @author sherman
 */
public class DateTimeUtil {

    public static final String STANDARD_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 字符串时间转换成Date类型
     *
     * @param dateTimeStr   字符串时间表现形式
     * @param formatPattern 需要转换的格式
     * @return 最终的时间
     */
    public static Date stringToDate(String dateTimeStr, String formatPattern) {
        DateTimeFormatter dtf = DateTimeFormat.forPattern(formatPattern);
        DateTime dateTime = dtf.parseDateTime(dateTimeStr);
        return dateTime.toDate();
    }

    public static Date stringToDate(String dateTimeStr) {
        return stringToDate(dateTimeStr, STANDARD_FORMAT);
    }

    /**
     * 将Date类型转换成字符串时间类型
     *
     * @param date          Date时间
     * @param formatPattern 需要转换的时间
     * @return 最终字符串时间形式
     */
    public static String dateToString(Date date, String formatPattern) {
        if (date == null) {
            return StringUtils.EMPTY;
        }
        DateTime dateTime = new DateTime(date);
        return dateTime.toString(formatPattern);
    }

    public static String dateToString(Date date) {
        return dateToString(date, STANDARD_FORMAT);
    }
}
