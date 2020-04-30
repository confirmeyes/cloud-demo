package com.cloud.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * @author WIN10 .
 * @create 2020-04-30-11:02 .
 * @description .
 */



@SpringBootApplication
public class ConfigConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigConsumerApplication.class, args);
    }
}
