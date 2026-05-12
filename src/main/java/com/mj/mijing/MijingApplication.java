package com.mj.mijing;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 觅境点评 主启动类
 */
@EnableAsync
@EnableScheduling
@MapperScan("com.mj.mijing.mapper")
@SpringBootApplication
public class MijingApplication {

    public static void main(String[] args) {
        SpringApplication.run(MijingApplication.class, args);
    }
}
