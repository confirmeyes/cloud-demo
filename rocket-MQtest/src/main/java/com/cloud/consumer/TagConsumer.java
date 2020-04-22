package com.cloud.consumer;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.MessageSelector;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;

import java.util.List;

/**
 * @author lpx .
 * @create 2020-04-22-11:02 .
 * @description .
 */
public class TagConsumer {


    public static void main(String[] args) throws Exception {

        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("consumer-tag");

        consumer.setNamesrvAddr("192.168.88.134:9876");

        // 每个consumer 关注一个topic
        // topic 关注的消息的地址
        // 过滤器 * 表示不过滤

        MessageSelector selector = MessageSelector.bySql("age >= 18 and age <= 28");
        consumer.subscribe("tagTopic-test", selector);

        // 设置监听机制，并回调
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
                for (MessageExt msg : msgs) {
                    System.out.println(new String(msg.getBody()));
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });

        //consumer.setMessageModel(MessageModel.CLUSTERING);
        consumer.start();
    }
}
