package com.cloud.consumer;

import com.cloud.api.Student;
import feign.FeignException;
import feign.hystrix.FallbackFactory;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;

import java.util.Map;

/**
 * @author lpx .
 * @create 2020-04-19-10:32 .
 * @description .
 */

@Component
public class WebError implements FallbackFactory<UserFeignService> {
    @Override
    public UserFeignService create(Throwable throwable) {
        return new UserFeignService() {
            @Override
            public String isAlive() {
                System.out.println(throwable);
                if(throwable instanceof HttpServerErrorException.InternalServerError) {
                    System.out.println("InternalServerError");
                    return "远程服务报错"+ throwable.getLocalizedMessage();
                }else if(throwable instanceof RuntimeException) {

                    return "请求时异常：" + throwable;
                }else {
                    return "都算不上";
                }
            }

            @Override
            public Map getMap(Integer id) {
                return null;
            }

            @Override
            public Student getStudent(Map map) {
                return null;
            }
        };
    }
}
