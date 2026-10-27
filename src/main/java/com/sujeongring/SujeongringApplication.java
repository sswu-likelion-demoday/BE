package com.sujeongring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class SujeongringApplication {

    public static void main(String[] args) {
        SpringApplication.run(SujeongringApplication.class, args);
    }

}
