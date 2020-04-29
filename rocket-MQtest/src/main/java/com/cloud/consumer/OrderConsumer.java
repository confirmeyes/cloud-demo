package com.cloud.consumer;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.List;

/**
 * @author WIN10 .
 * @create 2020-04-29-17:04 .
 * @description .
 */
public class OrderConsumer {

    public static void main(String[] args) throws Exception {

        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("TransactionConsumer");

        consumer.setNamesrvAddr("192.168.88.134:9876");
        // 设置监听机制，并回调
        consumer.subscribe("OrderTopic", "*");

        // 设置 ---- 顺序消费
        // 最大开启消费线程数
        consumer.setConsumeThreadMax(1);
        //  最小线程数
        consumer.setConsumeThreadMin(1);
        consumer.registerMessageListener(new MessageListenerOrderly() {

            @Override
            public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs, ConsumeOrderlyContext context) {
                for (MessageExt msg : msgs) {

                    System.out.println(new String(msg.getBody()) + " Thread:" + Thread.currentThread().getName() + " queueid:" + msg.getQueueId());
                    ;
                }
                return ConsumeOrderlyStatus.SUCCESS;
            }
        });

        consumer.start();
        System.out.println("Consumer g02 start...");
    }
}

