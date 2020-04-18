package com.cloud.consumer;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * @author lpx .
 * @create 2020-04-18-13:21 .
 * @description .
 */


public class MyBasicAuthRequestInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        requestTemplate.header("Authorization", "Basic cm9vdDpyb290");
    }
}
