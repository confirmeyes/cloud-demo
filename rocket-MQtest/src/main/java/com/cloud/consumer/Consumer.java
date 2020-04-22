package com.cloud.consumer;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
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
public class Consumer {


    public static void main(String[] args) throws Exception {

        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("consumer");

        consumer.setNamesrvAddr("192.168.88.134:9876");

        // 每个consumer 关注一个topic
        // topic 关注的消息的地址
        // 过滤器 * 表示不过滤
        consumer.subscribe("SingleTopic", "*");

        // 设置监听机制，并回调
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
                for (MessageExt msg : msgs) {
                    System.out.println(new String(msg.getBody()));
                }
                // 默认情况下 这条消息只会被 一个consumer 消费到 点对点
                // message 状态修改 （ 由broker进行维护 ）
                // ACK   返回消费状态
                // CONSUME_SUCCESS 消费成功
                //  RECONSUME_LATER 消费失败，需要稍后重新消费
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        // ACK 重新投递
        //RocketMQ会把这批消息重发回Broker（topic不是原topic而是这个消费租的RETRY topic 重发topic），
        //在延迟的某个时间点（默认是10秒，业务可设置）后，再次投递到这个ConsumerGroup。
        //而如果一直这样重复消费都持续失败到一定次数（默认16次），就会投递到DLQ死信队列。应用可以监控死信队列来做人工干预。

        // 集群消费模式 CLUSTERING ：每条消息只被消费一次  重投机制
        // message 状态修改 （ 由broker进行维护 ）

        // 广播消息模式 BROADCASTING ：所有订阅该topic的消费者至少消费一次全部消息
        // message 状态修改 （ 由consumer进行维护 ）, broker 不关心消息状态
        //consumer.setMessageModel(MessageModel.BROADCASTING);

        consumer.start();
    }
}
