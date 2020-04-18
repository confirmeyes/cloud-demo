package com.cloud.eureka;

import com.cloud.Student;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author lpx .
 * @create 2020-04-09-17:05 .
 * @description .
 */

@RestController
public class ProvideController {

    @GetMapping("/hello")
    public String hello() {
        return "SpringCloud Learning";
    }

    @GetMapping("/student")
    public Student student() {
        return new Student(1, "李", 23);
    }

    @GetMapping("/student2")
    public Student student2(@RequestParam(name = "id") int id) {
        return new Student(id, "李", 23);
    }

    @PostMapping("/student3")
    public Student student3(@RequestBody String name) {
        System.out.println("name ::: " + name);
        return new Student(1, name, 23);
    }

    @PostMapping("/student4")
    public Student student4(@RequestBody Map map) {
        System.out.println("id ::: " + map);
        return new Student(Integer.parseInt(map.get("id").toString()), "eeeeeeeee", 23);
    }


}
