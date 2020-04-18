package com.cloud;

import com.cloud.api.Student;
import com.cloud.api.UserApi;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

/**
 * @author lpx .
 * @create 2020-04-18-10:38 .
 * @description .
 */

@RestController
public class ProviderController implements UserApi {

    @Override
    public String isAlive() {
        return "Ok!!!";
    }

    @Override
    public Map getMap(Integer id) {
        return Collections.singletonMap(id, "第一个Map");
    }

    @Override
    public Student getStudent(Map map) {
        Student student = new Student();
        student.setId(Integer.parseInt(map.get("id").toString()));
        student.setName(String.valueOf(map.get("name")));
        return student;
    }
}
