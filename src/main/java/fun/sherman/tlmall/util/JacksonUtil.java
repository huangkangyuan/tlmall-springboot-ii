package fun.sherman.tlmall.util;

import com.google.common.collect.Lists;
import fun.sherman.tlmall.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * @author sherman
 */
@Slf4j
public class JacksonUtil {
    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        // 对象的所有字段都进行序列化
        objectMapper.setSerializationInclusion(JsonSerialize.Inclusion.ALWAYS);

        /**
         * 序列化配置
         */
        // 取消默认转换成timestamp形式
        objectMapper.configure(SerializationConfig.Feature.WRITE_DATE_KEYS_AS_TIMESTAMPS, false);
        // 忽略空Bean转换成Json错误
        objectMapper.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);
        // 统一所有日期格式
        objectMapper.setDateFormat(new SimpleDateFormat(DateTimeUtil.STANDARD_FORMAT));

        /**
         * 反序列化配置
         */
        // 或许Json字符串存在，但是Bean对象不存在时的错误
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Bean对象序列化成String
     */
    public static <T> String beanToString(T bean) {
        if (bean == null) {
            return null;
        }
        try {
            return bean instanceof String ? (String) bean : objectMapper.writeValueAsString(bean);
        } catch (Exception e) {
            log.error("Parse bean to String error", e);
            return null;
        }
    }

    public static <T> String beanToStringPretty(T bean) {
        if (bean == null) {
            return null;
        }
        try {
            return bean instanceof String ? (String) bean : objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(bean);
        } catch (Exception e) {
            log.error("Parse bean to String error", e);
            return null;
        }
    }

    /**
     * String反序列化成Bean对象
     */
    public static <T> T stringToBean(String str, Class<T> clazz) {
        if (StringUtils.isEmpty(str) || clazz == null) {
            return null;
        }
        try {
            return clazz.equals(String.class) ? (T) str : objectMapper.readValue(str, clazz);
        } catch (IOException e) {
            log.error("Parse String to bean error", e);
            return null;
        }
    }

    /**
     * 复杂集合（List、Map、Set等）的反序列化方式1
     */
    public static <T> T stringToBean(String str, TypeReference<T> typeReference) {
        if (StringUtils.isEmpty(str) || typeReference == null) {
            return null;
        }
        try {
            return typeReference.getType().equals(String.class) ? (T) str : objectMapper.readValue(str, typeReference);
        } catch (IOException e) {
            log.error("Parse String to bean error", e);
            return null;
        }
    }

    /**
     * 复杂集合（List、Map、Set等）的反序列化方式2
     */
    public static <T> T stringToBean(String str, Class<?> collectionsClass, Class<?>... elementClasses) {
        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(collectionsClass, elementClasses);
        try {
            return objectMapper.readValue(str, javaType);
        } catch (IOException e) {
            log.error("Parse String to bean error", e);
            return null;
        }
    }
}
