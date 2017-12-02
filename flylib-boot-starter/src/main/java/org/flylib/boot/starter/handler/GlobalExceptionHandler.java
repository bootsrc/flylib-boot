package org.flylib.boot.starter.handler;

import org.flylib.boot.starter.exception.CustomRuntimeException;
import org.flylib.boot.starter.exception.UnknownResourceException;
import org.flylib.boot.starter.exception.ValidationRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.ui.Model;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.mvc.multiaction.NoSuchRequestHandlingMethodException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 说明：
 *
 * @ControllerAdvice是controller的一个辅助类，最常用的就是作为全局异常处理的切面类
 * @ControllerAdvice可以指定扫描范围
 * @ControllerAdvice约定了几种可行的返回值，如果是直接返回model类的话，需要使用@ResponseBody进行json转换 返回String，表示跳到某个view
 * 返回modelAndView
 * 返回model + @ResponseBody
 * 全局异常处理
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Autowired
    private Environment env;

    @Autowired(required = false)
    private MessageSource messageSource;

    @Autowired(required = false)
    private LocaleResolver localeResolver;

    private static final String defaultMoreInfoUrl = "";

    private final Map<String,HttpStatus> DEFAULT_EXCEPTION_MAPPING_DEFINITIONS;

    public GlobalExceptionHandler() {
        DEFAULT_EXCEPTION_MAPPING_DEFINITIONS = createDefaultExceptionMappingDefinitions();
    }

    @ExceptionHandler//处理所有异常
    @ResponseBody //在返回自定义相应类的情况下必须有，这是@ControllerAdvice注解的规定
    public Map<String,Object> exceptionHandler(Throwable e, HttpServletRequest request, HttpServletResponse response, Model model) {
        //
        log.error("handle error:",e);

        HttpStatus httpStatus = DEFAULT_EXCEPTION_MAPPING_DEFINITIONS.get(e.getClass().getName());
        if(httpStatus==null){
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        //是否是生产环境
        boolean isProd = "prod".equals(env.getActiveProfiles()[0]);
        Map<String,Object> map = new HashMap<String,Object>();
        if(e.getCause() instanceof CustomRuntimeException){
            CustomRuntimeException exception = (CustomRuntimeException) e.getCause();
            map.put("code",String.valueOf(exception.getCode()));
            map.put("message",exception.getMessage());
        }else if(e.getCause() instanceof ValidationRuntimeException){
            ValidationRuntimeException exception = (ValidationRuntimeException) e.getCause();
            map.put("code",String.valueOf(exception.getCode()));
            map.put("message",exception.getMessage());
            httpStatus = HttpStatus.BAD_REQUEST;
        }else {
            map.put("code",String.valueOf(httpStatus.value()));
            map.put("message",httpStatus.toString());
        }


        //不是生产环境，添加调试信息
        if(!isProd){
            map.put("throwable",e);
        }
        response.setStatus(httpStatus.value());
        return map;
    }

    protected final Map<String,HttpStatus> createDefaultExceptionMappingDefinitions() {

        Map<String,HttpStatus> m = new LinkedHashMap<String, HttpStatus>();

        // 400
        applyDef(m, HttpMessageNotReadableException.class, HttpStatus.BAD_REQUEST);
        applyDef(m, MissingServletRequestParameterException.class, HttpStatus.BAD_REQUEST);
        applyDef(m, TypeMismatchException.class, HttpStatus.BAD_REQUEST);
        applyDef(m, "javax.validation.ValidationException", HttpStatus.BAD_REQUEST);

        // 404
        applyDef(m, NoSuchRequestHandlingMethodException.class, HttpStatus.NOT_FOUND);
        applyDef(m, "org.hibernate.ObjectNotFoundException", HttpStatus.NOT_FOUND);

        // 405
        applyDef(m, HttpRequestMethodNotSupportedException.class, HttpStatus.METHOD_NOT_ALLOWED);

        // 406
        applyDef(m, HttpMediaTypeNotAcceptableException.class, HttpStatus.NOT_ACCEPTABLE);

        // 409
        //can't use the class directly here as it may not be an available dependency:
        applyDef(m, "org.springframework.dao.DataIntegrityViolationException", HttpStatus.CONFLICT);

        // 415
        applyDef(m, HttpMediaTypeNotSupportedException.class, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        applyDef(m, UnknownResourceException.class, HttpStatus.NOT_FOUND);

        return m;
    }
    private void applyDef(Map<String,HttpStatus> m, Class clazz, HttpStatus status) {
        applyDef(m, clazz.getName(), status);
    }

    private void applyDef(Map<String,HttpStatus> m, String key, HttpStatus status) {
        m.put(key, status);
    }



    protected String getMessage(String msg, ServletWebRequest webRequest, Exception ex) {

        if (msg != null) {
            if (msg.equalsIgnoreCase("null") || msg.equalsIgnoreCase("off")) {
                return null;
            }
            msg = ex.getMessage();
            if (messageSource != null) {
                Locale locale = null;
                if (localeResolver != null) {
                    locale = localeResolver.resolveLocale(webRequest.getRequest());
                }
                msg = messageSource.getMessage(msg, null, msg, locale);
            }
        }

        return msg;
    }
}
