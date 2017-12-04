package org.flylib.boot.starter.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * @author liushaoming
 * @create 2017-12-04 11:03
 **/
public class LoggingFilter implements Filter {
        private final Logger logger = LoggerFactory.getLogger(this.getClass());
        private final String ip;
//        private final String appId;

//    public LoggingFilter(String ip, String appId) {
//        this.ip = ip;
//        this.appId = appId == null ? "" : appId;
//    }
public LoggingFilter(String ip) {
    this.ip = ip;
}

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest)request;
        HttpServletResponse res = (HttpServletResponse)response;
//        req.setAttribute("_accessTime", System.nanoTime());
//        boolean flag = false;
//        String url = req.getRequestURI();
//        String sessionId;
//        if (url.lastIndexOf(".") != -1) {
//            sessionId = url.substring(url.lastIndexOf(".") + 1, url.length());
//            if ("html".equalsIgnoreCase(sessionId) || "htm".equalsIgnoreCase(sessionId) || "jsp".equalsIgnoreCase(sessionId)) {
//                flag = true;
//            }
//        } else {
//            flag = true;
//        }
//
//        if (flag) {
//            RequestIdUtils.setRequestId();
//            MDC.put("requestId", RequestIdUtils.getRequestId());
//            MDC.put("serverIp", this.ip);
//            MDC.put("appId", this.appId);
//            sessionId = CookieManager.getCookie("sessionId", req);
//            if (!StringUtils.hasText(sessionId)) {
//                sessionId = UUID.randomUUID().toString();
//                CookieManager.addCookieInMermory("sessionId", sessionId, "/", "", res);
//            }
//
//            RequestIdUtils.setSessionId(sessionId);
//            MDC.put("sessionId", sessionId);
//            String referer = req.getHeader("referer");
//            String userAgent = req.getHeader("User-Agent");
//            this.logger.info("access info :{} \"{} {}\" {} \"{}\" \"{}\"", new Object[]{IPUtils.getIpAddr(req), req.getMethod(), req.getRequestURL(), req.getProtocol(), referer == null ? "" : referer, userAgent == null ? "" : userAgent});
//        }
        System.out.println("---Filter demo--->");
        chain.doFilter(request, response);
    }

    public void destroy() {
    }
}
