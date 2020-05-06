package com.cloud.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author WIN10 .
 * @create 2020-04-30-11:57 .
 * @description .
 */


@RestController
@RefreshScope
public class ConfigController {

    @Value("${myconfig}")
    String myconfig;

    @GetMapping("/getConfig")
    public String getConfig() {
        return myconfig;
    }
}
