package com.cloud.provider;

import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.exception.RemotingException;


/**
 * @author lpx .
 * @create 2020-04-24-9:10 .
 * @description .
 */
public class TransactionMq {

    public static void main(String[] args) throws Exception {

        TransactionMQProducer producer = new TransactionMQProducer("TransactionProvider");

        producer.setNamesrvAddr("192.168.88.134:9876");
        producer.start();

        Message message = new Message("TransactionTopic", "事务消息！！".getBytes());
        producer.send(message);
        producer.setTransactionListener(new TransactionListener() {
            @Override
            public LocalTransactionState executeLocalTransaction(Message message, Object o) {

                try {
                    System.out.println("------执行其他业务操作------");
                    int x = 1 / 0;
                } catch (Exception e) {
                    return LocalTransactionState.ROLLBACK_MESSAGE;
                }
                return LocalTransactionState.COMMIT_MESSAGE;
            }

            @Override
            public LocalTransactionState checkLocalTransaction(MessageExt messageExt) {
                System.out.println("------回调方法-------");
                return LocalTransactionState.COMMIT_MESSAGE;
            }
        });

    }

}
