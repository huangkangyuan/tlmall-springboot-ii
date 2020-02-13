package fun.sherman.tlmall.interceptor;


import fun.sherman.tlmall.common.Const;
import fun.sherman.tlmall.domain.User;
import fun.sherman.tlmall.util.CookieUtil;
import fun.sherman.tlmall.util.JacksonUtil;
import fun.sherman.tlmall.util.ShardedRedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author sherman
 */
@Slf4j
public class AdminAuthorityInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("enter preHandle()");
        // handler其实是一个HandlerMethod对象，强转并拿到method的name和方法所在类的简单类名
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        String methodName = handlerMethod.getMethod().getName();
        String classSimpleName = handlerMethod.getBean().getClass().getSimpleName();

        // 解析拦截的参数，方便日志查看，注意日志中不能包含用户密码信息
        StringBuilder requestParamBuilder = new StringBuilder();
        Map<String, String[]> parameterMap = request.getParameterMap();
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            String key = entry.getKey();
            String[] value = entry.getValue();
            String valueStr = String.join("-", value);
            requestParamBuilder.append(key).append("=").append(valueStr).append("&");
        }
        // 拦截到UserManageController#login()直接返回放行，但是不打印日志
        if (StringUtils.equals(classSimpleName, "UserManageController") &&
                StringUtils.equals(methodName, "login")) {
            return true;
        }
        log.info("admin权限拦截器拦截到请求,className:{}, methodName:{},param:{}",
                classSimpleName, methodName, requestParamBuilder.toString());

        User user = null;
        String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isNotEmpty(loginToken)) {
            String userJsonStr = ShardedRedisUtil.get(loginToken);
            user = JacksonUtil.stringToBean(userJsonStr, User.class);
        }

        if (user == null || user.getRole() != Const.Role.ROLE_ADMIN) {
            response.reset();
            response.setCharacterEncoding("utf-8");
            response.setContentType("application/json;charset=utf-8");
            PrintWriter pw = response.getWriter();
            // 对富文本文件进行特殊处理，满足simditor要求
            if (StringUtils.equals(classSimpleName, "ProductManageController") &&
                    StringUtils.equals(methodName, "richtextUpload")) {
                Map<String, Object> resultMap = new HashMap<>();
                if (user == null) {
                    resultMap.put("success", false);
                    resultMap.put("msg", "用户未登录，请先登录");
                    pw.write(JacksonUtil.beanToString(resultMap));
                } else {
                    resultMap.put("success", false);
                    resultMap.put("msg", "无权限操作，请登录管理员账号");
                    pw.write(JacksonUtil.beanToString(resultMap));
                }
            }
            pw.flush();
            pw.close();
            return false;
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        log.info("enter postHandle()");
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        log.info("enter afterHandle()");
    }
}
