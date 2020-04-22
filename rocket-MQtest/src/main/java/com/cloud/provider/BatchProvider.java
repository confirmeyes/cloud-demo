package com.cloud.provider;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;

import java.util.ArrayList;

/**
 * @author lpx .
 * @create 2020-04-22-11:02 .
 * @description 批量发送.
 */
public class BatchProvider {
    public static void main(String[] args) throws Exception {

        DefaultMQProducer producer = new DefaultMQProducer("BatchProvider");

        producer.setNamesrvAddr("192.168.88.134:9876");
        producer.start();

        // topic 消息将要发送到的地址
        // body  消息中的具体数据
        Message msg1 = new Message("BatchTopic", "批量消息！！".getBytes());
        Message msg2 = new Message("BatchTopic", "批量消息！！".getBytes());
        Message msg3 = new Message("BatchTopic", "批量消息！！".getBytes());

        ArrayList<Message> list = new ArrayList<Message>();
        list.add(msg1);
        list.add(msg2);
        list.add(msg3);
        SendResult result = producer.send(list);

        producer.shutdown();

        System.out.println(result);
    }


}
