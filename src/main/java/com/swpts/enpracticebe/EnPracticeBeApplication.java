package com.swpts.enpracticebe;

import com.swpts.enpracticebe.constant.GoogleSttProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@EnableCaching
@EnableConfigurationProperties({GoogleSttProperties.class})
@SpringBootApplication
public class EnPracticeBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(EnPracticeBeApplication.class, args);
    }

}
