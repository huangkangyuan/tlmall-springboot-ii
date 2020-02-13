package fun.sherman.tlmall.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

/**
 * Properties文件中属性值获取工具类封装
 *
 * @author sherman
 */
public class PropertiesUtil {
    private static Logger logger = LoggerFactory.getLogger(PropertiesUtil.class);
    private static Properties props;

    static {
        props = new Properties();
        try {
            props.load(PropertiesUtil.class.getClassLoader().getResourceAsStream("application.properties"));
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static String getProperty(String key) {
        String value = props.getProperty(key.trim());
        return value == null ? null : value.trim();
    }

    public static String getProperty(String key, String defaultValue) {
        String value = getProperty(key);
        return value == null ? defaultValue : value.trim();
    }
}