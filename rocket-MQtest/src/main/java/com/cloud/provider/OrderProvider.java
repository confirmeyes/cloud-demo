package com.cloud.provider;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;

import java.util.List;

/**
 * @author WIN10 .
 * @create 2020-04-29-17:10 .
 * @description .
 */
public class OrderProvider {


    public static void main(String[] args) throws Exception {


        DefaultMQProducer producer = new DefaultMQProducer("SyncProvider");

        producer.setNamesrvAddr("192.168.88.134:9876");
        producer.start();


        for (int i = 0; i < 20; i++) {

            Message message = new Message("xxoo007", ("hi!" + i).getBytes());

            producer.send(
                    // 要发的那条消息
                    message,
                    // queue 选择器 ，向 topic中的哪个queue去写消息
                    new MessageQueueSelector() {
                        // 手动 选择一个queue
                        @Override
                        public MessageQueue select(
                                // 当前topic 里面包含的所有queue
                                List<MessageQueue> mqs
                                ,
                                // 具体要发的那条消息
                                Message msg

                                // 对应到 send（） 里的 args
                                , Object arg) {
                            // TODO Auto-generated method stub

                            // 向固定的一个queue里写消息
                            MessageQueue queue = mqs.get(0);
                            // 选好的queue
                            return queue;
                        }
                    },
                    // 自定义参数
                    0, 2000);
        }

    }
}
