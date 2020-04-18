package com.cloud.eureka;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.DiscoveryClient;
import com.netflix.discovery.EurekaClient;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author lpx .
 * @create 2020-04-09-17:30 .
 * @description .
 */

@RestController
public class ConsumerController {

    /*@Autowired
    DiscoveryClient discoveryClient;*/

    @Autowired
    EurekaClient eurekaClient;

    @Autowired
    LoadBalancerClient loadBalancerClient;


    /**
     * 开启负载均衡
     */
    @Bean
    @LoadBalanced
    RestTemplate restTemplate() {
        return new RestTemplate();
    }


    @GetMapping("/client")
    public String client() {
        List<InstanceInfo> services = eurekaClient.getInstancesByVipAddress("Provider", false);

        for (InstanceInfo ins : services) {
            System.out.println(ToStringBuilder.reflectionToString(ins));
        }

        if (services.size() > 0) {
            InstanceInfo info = services.get(0);
            String url = "http://" + info.getHostName() + ":" + info.getPort() + "/hello";
            System.out.println(url);

            RestTemplate restTemplate = new RestTemplate();
            String res = restTemplate.getForObject(url, String.class);
            System.out.println(res);
        }


        return "Hi";
    }


    @GetMapping("/client2")
    public String client2() {
        // ribbon 完成客户端的负载均衡，过滤掉down了的节点
        ServiceInstance info = loadBalancerClient.choose("Provider");

        String url = "http://" + info.getHost() + ":" + info.getPort() + "/hello";
        System.out.println(url);

        RestTemplate restTemplate = new RestTemplate();
        String res = restTemplate.getForObject(url, String.class);
        System.out.println(res);

        return res;
    }


    @GetMapping("/getStudent")
    public String getStudent() {
        // ribbon 完成客户端的负载均衡，过滤掉down了的节点
        ServiceInstance info = loadBalancerClient.choose("Provider");

        String url = "http://" + info.getHost() + ":" + info.getPort() + "/student";
        System.out.println(url);

        RestTemplate restTemplate = new RestTemplate();
        Student student = restTemplate.getForObject(url, Student.class);
        assert student != null;
        System.out.println(student.toString());

        return student.toString();
    }


    @GetMapping("/getStudent2")
    public String getStudent2() {
        // ribbon 完成客户端的负载均衡，过滤掉down了的节点
        ServiceInstance info = loadBalancerClient.choose("Provider");

        String url = "http://" + info.getHost() + ":" + info.getPort() + "/student2?id={id}";
        System.out.println(url);

        RestTemplate restTemplate = restTemplate();
        Map<String, Integer> map = Collections.singletonMap("id", 1);
        Student student = restTemplate.getForObject(url, Student.class, map);
        assert student != null;
        System.out.println(student.toString());

        return student.toString();
    }


    @PostMapping("/getStudent3")
    public String getStudent3() {
        // ribbon 完成客户端的负载均衡，过滤掉down了的节点
        ServiceInstance info = loadBalancerClient.choose("Provider");

        String url = "http://" + info.getHost() + ":" + info.getPort() + "/student3";
        System.out.println(url);

        RestTemplate restTemplate = restTemplate();
        Map<String, String> map = Collections.singletonMap("name", "man");
        ResponseEntity<Student> entity = restTemplate.postForEntity(url, map, Student.class);
        return Objects.requireNonNull(entity.getBody()).toString();
    }

    @PostMapping("/getStudent4")
    public String getStudent4() {
        // ribbon 完成客户端的负载均衡，过滤掉down了的节点
        ServiceInstance info = loadBalancerClient.choose("Provider");

        String url = "http://" + info.getHost() + ":" + info.getPort() + "/student4";
        System.out.println(url);

        RestTemplate restTemplate = restTemplate();
        Map<String, Object> map = Collections.singletonMap("id", 1);
        ResponseEntity<Student> entity = restTemplate.postForEntity(url, map, Student.class);
        return Objects.requireNonNull(entity.getBody()).toString();
    }


}
