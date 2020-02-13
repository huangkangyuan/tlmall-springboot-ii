package fun.sherman.tlmall.exception;

import fun.sherman.tlmall.common.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 异常处理，防止直接将服务器日志直接显示在页面上
 *
 * @author sherman
 */
@Slf4j
@Component
public class CustomerExceptionHandler implements HandlerExceptionResolver {
    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object o, Exception e) {
        ModelAndView mv = new ModelAndView(new MappingJackson2JsonView());
        log.error("{} Exception", request.getRequestURI(), e);
        mv.addObject("status", ResponseCode.ERROR.getCode());
        mv.addObject("msg", "接口异常，请查看服务器日志");
        mv.addObject("data", e.getMessage());
        return mv;
    }
}