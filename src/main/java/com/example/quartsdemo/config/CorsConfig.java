package com.example.quartsdemo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;


@Configuration
public class CorsConfig {
	
    private CorsConfiguration buildConfig() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        // 모든 도메인 허용
        corsConfiguration.addAllowedOrigin("*");
        // 모든 헤더 허용
        corsConfiguration.addAllowedHeader("*");
        // 모든 메서드 허용 (POST, GET 등)
        corsConfiguration.addAllowedMethod("*");
        return corsConfiguration;
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 인터페이스에 대해 Cross-Origin 설정 구성
        source.registerCorsConfiguration("/**", buildConfig());
        return new CorsFilter(source);
    }

}
