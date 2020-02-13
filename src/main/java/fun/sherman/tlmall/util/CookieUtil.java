package fun.sherman.tlmall.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author sherman
 */
@Slf4j
public class CookieUtil {
    private static final String DOMAIN_NAME = "sherman.com";
    private static final String COOKIE_NAME = "tlmall_login_token";

    public static String readLoginToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                log.info("read cookieName:{}, cookieValue:{}", cookie.getName(), cookie.getValue());
                if (StringUtils.equals(cookie.getName(), COOKIE_NAME)) {
                    log.info("return cookieName:{}, cookieValue:{}", cookie.getName(), cookie.getValue());
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public static void writeLoginToken(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(COOKIE_NAME, token);
        cookie.setPath("/");
        cookie.setDomain(DOMAIN_NAME);
        // 禁止cookie分享给第三方脚本工具
        cookie.setHttpOnly(true);
        /**
         * 如果不设置maxAge，cookie不会写入磁盘，只存在内存中，只在当前页面有效
         * session maxAge默认24h
         */
        cookie.setMaxAge(60 * 60 * 24);
        log.info("write cookieName:{}, cookieValue:{}", cookie.getName(), cookie.getValue());
        response.addCookie(cookie);
    }

    public static void delLoginToken(HttpServletResponse response, HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (StringUtils.equals(cookie.getName(), COOKIE_NAME)) {
                    cookie.setDomain(DOMAIN_NAME);
                    cookie.setPath("/");
                    /**
                     * 将cookie的maxAge设置为0添加到response中，代表删除cookie
                     */
                    cookie.setMaxAge(0);
                    log.info("delete cookieName:{}, cookieValue:{}", cookie.getName(), cookie.getValue());
                    response.addCookie(cookie);
                    return;
                }
            }
        }
    }


}
