## flylib-boot简介
flylib-boot是针对springboot构建的程序的基础框架，专门用于构建程序里的比如统一
异常处理

## 功能
包含一个Spring Boot的一些常见的基础组件的设置
**1. 针对Handler的全局的异常处理(处理所有Controller里的Handler里抛出的异常)
2. Filter
3. Interceptor**

注意：SpringBoot（SpringMVC)里的Handler特指@Controller注解的类里的每个处理HTTP请求的一个public method.

## 使用方法
- Step 1:   进入目录flylib-boot-starter,执行<code>mvn install</code> 
- Step 2:   在自己的项目中添加flylib-boot-starter的maven依赖. 并留意自己使用的spring-boot版本，去修改自己的pom.xml文件
```xml
<dependency>
    <groupId>org.flylib</groupId>
    <artifactId>flylib-boot-starter</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```
并且要注意这里spring-boot版本是1.5.0.RELEASE.   另外需要添加spring-boot-maven-plugin
实例参考spring-boot-demo项目，它的pom如下：
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>org.flylib</groupId>
    <artifactId>flylib-boot-demo</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>jar</packaging>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>1.5.0.RELEASE</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
        <java.version>1.8</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.flylib</groupId>
            <artifactId>flylib-boot-starter</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>

```

- Step 3:  在自己的程序中new 一个UserException（自定义的异常类）设置捕获异常
```java
/**
 * 用户信息的异常
 */
public class UserException extends RuntimeException{

}


@RequestMapping("")
    public String index() throws RuntimeException {
        UserException userException = new UserException();
        CustomRuntimeException cause = new CustomRuntimeException("001", "User not exists");
        userException.initCause(cause);
        throw userException;
    }
```
- Step 4:  运行自己的Spring Boot项目
输出到浏览器的结果
```json
{
    code:"001",
    message:"User not exists",
    throwable:{...}
}
```
## 实现原理
利用了@ControllerAdvice和@ExceptionHandler
实现代码是
```java
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

```
