package com.cloud.provider;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;

/**
 * @author lpx .
 * @create 2020-04-22-11:20 .
 * @description 异步发送.
 */


//消息生产者的四种发送方式 同步、异步、批量、单向

public class AsynProvider {

    public static void main(String[] args) throws MQClientException, RemotingException, InterruptedException {

        // 设置producerGroup
        DefaultMQProducer producer = new DefaultMQProducer("AsynProvider");

        // 设置nameserver地址
        producer.setNamesrvAddr("192.168.88.134:9876");
        producer.start();

        // topic 消息将要发送到的地址
        // body  消息中的具体数据
        Message message = new Message("AsynTopic", "第一条异步消息！！".getBytes());

        // 异步可靠消息
        // 不会阻塞，等待broker的确认
        // 采用事件监听方式接受broker返回的确认

        producer.send(message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                System.out.println("消息发送成功。。。");
                System.out.println("sendResult ：" + sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                System.out.println(throwable.getLocalizedMessage());
                throwable.printStackTrace();
            }
        });

        //异步不能显式的关闭 provider, 因为不清楚，什么时间点 broker 返回确认。


    }
}
