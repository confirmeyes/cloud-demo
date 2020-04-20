package com.cloud.consumer;

import com.cloud.api.Student;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author lpx .
 * @create 2020-04-19-10:09 .
 * @description .
 */

@Component
public class AliveBack implements UserFeignService {
    @Override
    public String isAlive() {
        return "出错了！！！";
    }

    @Override
    public Map getMap(Integer id) {
        return null;
    }

    @Override
    public Student getStudent(Map map) {
        return null;
    }
}
