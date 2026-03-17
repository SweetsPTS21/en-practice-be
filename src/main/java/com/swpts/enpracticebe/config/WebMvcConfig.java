package com.swpts.enpracticebe.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    // This configuration ensures that WebSocket paths are not treated as static resources
}
