package com.cloud.consumer;

import com.cloud.api.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author lpx .
 * @create 2020-04-18-11:33 .
 * @description 用于接收外面的请求 ------> FeignClient 接口 -------> provider生产者
 */

@RestController
public class ConsumerController {

    @Resource
    UserFeignService userFeignApi;

    @GetMapping("/alive")
    public String alive() {

        return userFeignApi.isAlive();
    }

    /**
     * Feign默认所有带参数的请求都是Post，想要使用指定的提交方式需引入依赖
     */

    @GetMapping("/map")
    public Map map(@RequestParam("id") Integer id) {

        return userFeignApi.getMap(id);
    }


    @PostMapping("/student")
    public Student user(@RequestBody Map map) {

        return userFeignApi.getStudent(map);
    }
}
