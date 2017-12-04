package org.flylib.boot.starter.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author liushaoming
 * @create 2017-12-04 10:29
 **/
public class ResponseHandlerInterceptor implements HandlerInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(ResponseHandlerInterceptor.class);

    public ResponseHandlerInterceptor() {
    }

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        request.setAttribute("startTime", System.currentTimeMillis());
        return true;
    }

    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        String contentType = request.getHeader("Accept");
        if (contentType != null && contentType.indexOf("json") != -1) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Class<?> clazz = handlerMethod.getReturnType().getMethod().getReturnType();
            if ("void".equals(clazz.toString())) {
                response.getWriter().write("{\"code\":\"0\",\"message\":\"ok\"}");
            }
        }

    }

    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        long startTime = ((Long) request.getAttribute("startTime")).longValue();
        long endTime = System.currentTimeMillis();
        long executeTime = endTime - startTime;
        logger.info("[" + handler + "] executeTime :{} ms for request {}", executeTime, request.getRequestURI());
    }
}
