package com.cloud.api; /**
 * @author lpx .
 * @create 2020-04-18-10:42 .
 * @description .
 */

import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RequestMapping("/user")
public interface UserApi {


    @GetMapping("/isAlive")
    String isAlive();

    @GetMapping("/map")
    Map getMap(@RequestParam("id") Integer id);


    @PostMapping("/student")
    Student getStudent(@RequestBody Map map);

}
