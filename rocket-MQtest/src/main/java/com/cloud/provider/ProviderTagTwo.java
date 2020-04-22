package com.cloud.provider;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;

/**
 * @author lpx .
 * @create 2020-04-22-13:20 .
 * @description .
 */
public class ProviderTagTwo {



    public static void main(String[] args) throws Exception {

        DefaultMQProducer producer = new DefaultMQProducer("SyncProvider");

        producer.setNamesrvAddr("192.168.88.134:9876");
        producer.start();
        for (int i = 1; i <= 100; i++) {

            Message message = new Message("tagTopic-test", "TAG-B","KEY-xx",("xxooxx:" + i ).getBytes());
            message.putUserProperty("age", String.valueOf(i));
            producer.send(message);
        }

        producer.shutdown();
    }
}
