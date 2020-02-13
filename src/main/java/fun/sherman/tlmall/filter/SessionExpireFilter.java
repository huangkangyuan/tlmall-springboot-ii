package fun.sherman.tlmall.filter;

import fun.sherman.tlmall.common.Const;
import fun.sherman.tlmall.domain.User;
import fun.sherman.tlmall.util.CookieUtil;
import fun.sherman.tlmall.util.JacksonUtil;
import fun.sherman.tlmall.util.ShardedRedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * 获取用户登录的token值，如果请求页面有效，将对应的token值有效期设置为默认值30分钟
 *
 * @author sherman
 */
@Component
@WebFilter(urlPatterns = "/*.do", filterName = "sessionExpireFilter")
public class SessionExpireFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String userLoginToken = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isNotEmpty(userLoginToken)) {
            String userJsonStr = ShardedRedisUtil.get(userLoginToken);
            User user = JacksonUtil.stringToBean(userJsonStr, User.class);
            if (user != null) {
                // 用户存在，重置userLoginToken的时间为30分钟
                ShardedRedisUtil.expire(userLoginToken, Const.RedisKeyExpires.USER_LOGIN_TOKEN);
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
