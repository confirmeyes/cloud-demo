package com.cloud;

import com.cloud.api.Student;
import com.cloud.api.UserApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author lpx .
 * @create 2020-04-18-10:38 .
 * @description .
 */

@RestController
public class ProviderController implements UserApi {

    @Value("${server.port}")
    String port;

    private AtomicInteger count = new AtomicInteger();

    @Override
    public String isAlive() {

       /* try {
            System.out.println("sleep!!!");

            Thread.sleep(5000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }*/
       int x = 1/0;
        int i = count.getAndIncrement();
        System.out.println(port+ "====的第：" + i + "次调用");

        return "port:" + port;
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
