package org.flylib.boot.starter.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.flylib.boot.starter.filter.LoggingFilter;
import org.flylib.boot.starter.interceptor.ResponseHandlerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.HttpMessageConverters;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * mvc 配置类
 * 支持 velocity 和jsp视图解析
 * 加载顺序
 * 首先用ResourceBundleViewResolver 查找资源 如果差不多再根据velocity 视图解析器查找 如何还差不多就用jsp视图解析器
 * 2：ResourceBundleViewResolver
 * 3：VelocityViewResolver
 * 5：InternalResourceViewResolver
 * @author meixinbin
 * @2017-1-20
 */
@Configuration
public class MvcConfig extends WebMvcConfigurerAdapter {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private Environment environment;

	@Bean
	public HttpMessageConverters httpMessageConverters() {
		MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
		jsonConverter.setPrefixJson(false);
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		jsonConverter.setObjectMapper(objectMapper);
		List<MediaType> ls = new ArrayList<MediaType>();
		ls.add(new MediaType("application","json"));
		ls.add(new MediaType("text","json"));
		jsonConverter.setSupportedMediaTypes(ls);
		return new HttpMessageConverters(jsonConverter);
	}

	@Override
	public void configurePathMatch(PathMatchConfigurer configurer) {
	}

	@Override
	public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
		configurer.mediaType(".*", new MediaType("application","json"));
		configurer.mediaType(".*", new MediaType("text","json"));
	}



	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new ResponseHandlerInterceptor());
		super.addInterceptors(registry);
	}

	@Bean
	public FilterRegistrationBean someFilterRegistration() {
		FilterRegistrationBean registration = new FilterRegistrationBean();
		try {
//			registration.setFilter(new LoggingFilter(InetAddress.getLocalHost().getHostAddress(),environment.getProperty("app.id")));
			registration.setFilter(new LoggingFilter(InetAddress.getLocalHost().getHostAddress()));
		} catch (UnknownHostException e) {
			logger.error("get server ip error",e);
		}
		registration.addUrlPatterns("/*");
		registration.setName("loggingFilter");
		registration.setOrder(Integer.MIN_VALUE);
		return registration;
	}

}
