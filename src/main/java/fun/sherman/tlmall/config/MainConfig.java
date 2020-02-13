package fun.sherman.tlmall.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

/**
 * SpringBoot的主配置类
 *
 * @author sherman
 */
@Configuration
public class MainConfig {
    @Bean(name = "multipartResolver")
    MultipartResolver multipartResolver() {
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
        // 最大上传文件大小10m
        multipartResolver.setMaxUploadSize(10485760);
        multipartResolver.setMaxInMemorySize(4096);
        multipartResolver.setDefaultEncoding("UTF-8");
        return multipartResolver;
    }
}
