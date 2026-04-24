package com.feature.neighbourHood_backend.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class GlobalCorsConfig {

    @Value("${app.cors.allowed-origins:http://localhost:3000}")
    private String allowedOrigins;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NonNull CorsRegistry registry) {
                String[] origins = Arrays.stream(allowedOrigins.split(","))
                        .map(String::trim)
                        .filter(origin -> !origin.isEmpty())
                        .toArray(String[]::new);

                if (origins.length == 0) {
                    origins = new String[] { "http://localhost:3000" };
                }

                // 映射路徑
                registry.addMapping("/**")
                        // 允許跨網域請求的來源
                        .allowedOriginPatterns(origins)
                        // 允許跨域攜帶cookie資訊，預設跨網域請求是不攜帶cookie資訊的。
                        .allowCredentials(true)
                        // 允許使用那些請求方式
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        // 允許哪些Header
                        .allowedHeaders("*")
                        // 可獲取哪些Header（因為跨網域預設不能取得全部Header資訊）
                        .exposedHeaders("*");
            }
        };
    }
}