package com.cloud.consumer;

import com.cloud.api.UserApi;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * @author lpx .
 * @create 2020-04-18-11:36 .
 * @description .
 */

@FeignClient(name = "User-Provider")
public interface UserFeignService extends UserApi {
}
