package com.cloud.provider;

import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;

/**
 * @author lpx .
 * @create 2020-04-22-11:11 .
 * @description 同步发送.
 */
public class SyncProvider {

    public static void main(String[] args) throws MQClientException, RemotingException, InterruptedException, MQBrokerException {

        DefaultMQProducer producer = new DefaultMQProducer("SyncProvider");

        producer.setNamesrvAddr("192.168.88.134:9876");
        producer.start();

        // topic 消息将要发送到的地址
        // body  消息中的具体数据
        Message message = new Message("SyncTopic", "第一条同向消息！！".getBytes());

        SendResult result = producer.send(message);

        producer.shutdown();

        System.out.println(result);
    }
}
