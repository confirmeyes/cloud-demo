package com.cloud.consumer;

import com.cloud.api.UserApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author lpx .
 * @create 2020-04-18-11:36 .
 * @description .
 */

@FeignClient(name = "provider", configuration = FeignAuthConfiguration.class, fallbackFactory = WebError.class)
public interface UserFeignService extends UserApi {
}
