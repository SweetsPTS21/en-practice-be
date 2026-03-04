package com.swpts.enpracticebe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class EnPracticeBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(EnPracticeBeApplication.class, args);
    }

}
