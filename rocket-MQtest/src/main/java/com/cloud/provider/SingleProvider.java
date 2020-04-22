package com.cloud.provider;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;

/**
 * @author lpx .
 * @create 2020-04-22-11:21 .
 * @description 单向发送.
 */
public class SingleProvider {
    public static void main(String[] args) throws Exception {

        DefaultMQProducer producer = new DefaultMQProducer("SingleProvider");

        producer.setNamesrvAddr("192.168.88.134:9876");
        producer.start();

        // topic 消息将要发送到的地址
        // body  消息中的具体数据
        Message message = new Message("SingleTopic", "第一条单向消息！！".getBytes());

        //单向发送 （最易丢失消息）
        producer.sendOneway(message);

        producer.shutdown();

    }
}
