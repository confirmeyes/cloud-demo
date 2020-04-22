package com.cloud.provider;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;

/**
 * @author lpx .
 * @create 2020-04-22-13:20 .
 * @description .
 */
public class ProviderTag {



    public static void main(String[] args) throws Exception {

        DefaultMQProducer producer = new DefaultMQProducer("SyncProvider");

        producer.setNamesrvAddr("192.168.88.134:9876");
        producer.start();
        // tag 是用来过滤消息，消息分组
        Message message = new Message("tagTopic", "TAG-A", "KEY-A", "xxooxx".getBytes());

        producer.send(message);

        producer.shutdown();
    }
}
