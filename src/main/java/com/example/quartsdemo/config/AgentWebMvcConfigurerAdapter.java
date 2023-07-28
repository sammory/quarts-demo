package com.example.quartsdemo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AgentWebMvcConfigurerAdapter implements WebMvcConfigurer {

	/**
	   *  정적 자원 액세스 구성 (예: 이미지, CSS, JavaScript 파일 등)
     * @param registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        registry.addResourceHandler("/css/**", "/image/**", "/js/**").addResourceLocations("classpath:/static/");
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
    }

}

