package com.ecc.aipao98syc;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.ecc.aipao98syc")
public class Aipao98sycApplication {

    public static void main(String[] args) {
        SpringApplication.run(Aipao98sycApplication.class, args);
    }

}
