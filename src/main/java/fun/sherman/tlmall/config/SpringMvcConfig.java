package fun.sherman.tlmall.config;

import fun.sherman.tlmall.interceptor.AdminAuthorityInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC配置文件
 *
 * @author sherman
 */
@Configuration
public class SpringMvcConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        InterceptorRegistration interceptorRegistration = registry.addInterceptor(adminAuthorityInterceptor());
        // 拦截/manage下及其子路径下所有请求，但是不拦截/user/login.do，方式递归
        interceptorRegistration.addPathPatterns("/manage/**");
        //interceptorRegistration.excludePathPatterns(("/manage/user/login.do"));
    }

    @Bean
    public AdminAuthorityInterceptor adminAuthorityInterceptor() {
        return new AdminAuthorityInterceptor();
    }
}
