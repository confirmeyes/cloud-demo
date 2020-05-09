### 长轮询 （RocketMQ使用）

Consumer -> Broker RocketMQ采用的长轮询建立连接

- consumer的处理能力Broker不知道
- 直接推送消息 broker端压力较大
- 采用长连接有可能consumer不能及时处理推送过来的数据
- pull主动权在consumer手里

#### 短轮询

client不断发送请求到server，每次都需要重新连接

#### 长轮询

client发送请求到server，server有数据返回，没有数据请求挂起不断开连接

#### 长连接

连接一旦建立，永远不断开，push方式推送

### **消费端**
#### 主要从以下 5步操作 进行源码跟踪

1. **new出 DefaultMQPushConsumer**
```java
DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("consumer");
```
2. **设置Namesrv地址**
```java
consumer.setNamesrvAddr("192.168.88.134:9876");
```
3. **订阅topic，并进行过滤。（ pullMessageService 启动后 ，会看到内部如何操作）**
- **DefaultMQPushConsumer的方法由 defaultMQPushConsumerImpl类进行真正实现**
- **返回subscriptionData ，subExpression要么指定，要么为***
- **mQClientFactory会在 消费客户端启动后，向broker发送心跳包**
```java
consumer.subscribe("tagTopic", "TAG-A");

-----------------------------------------------------------------------------------------

public void subscribe(String topic, String subExpression) throws MQClientException {
	this.defaultMQPushConsumerImpl.subscribe(this.withNamespace(topic), subExpression);
    }
    
-----------------------------------------------------------------------------------------
    
this.rebalanceImpl.getSubscriptionInner().put(topic, subscriptionData);
    
-----------------------------------------------------------------------------------------

if (this.mQClientFactory != null) {
                this.mQClientFactory.sendHeartbeatToAllBrokerWithLock();
            }
```

4. **设置消息监听，并回调**
   
```java
consumer.registerMessageListener

默认情况下 这条消息只会被 一个consumer 消费到 点对点
message 状态修改 （ 由broker进行维护 ）

ACK (重新投递) 返回消费状态--->CONSUME_SUCCESS 消费成功 || RECONSUME_LATER 消费失败，重新消费
```
- **返回 Broker RECONSUME_LATER状态时** 
- **RocketMQ会把这批消息重发回Broker。（topic不是原topic而是这个消费租的RETRY topic 重发topic）**
- **在延迟的某个时间点（默认是10秒，业务可设置）后，再次投递到这个ConsumerGroup的另一个消费者。**
- **如果一直这样重复消费都持续失败到一定次数（默认16次），就会投递到DLQ死信队列。应用可以监控死信队列来做人工干预。**
5. **启动 消费客户端 （ 开启 traceDispatcher 追踪调度 ）**
```java
consumer.start();
this.defaultMQPushConsumerImpl.start();
```

- **针对 ServiceState 状态进行操作** 

```java
    刚刚创建	CREATE_JUST,
    正在运行	RUNNING,
    已经关闭	SHUTDOWN_ALREADY,
    开启失败	START_FAILED;
```

- **检查配置，获取订阅列表 SubscriptionData**
```java
this.checkConfig();
this.copySubscription();
```
- **获取MQClient实例**
```java
this.mQClientFactory = MQClientManager.getInstance().getOrCreateMQClientInstance(this.defaultMQPushConsumer, this.rpcHook);
```
- **注册消费者，并开启客户端**
```java
boolean registerOK = this.mQClientFactory.registerConsumer(this.defaultMQPushConsumer.getConsumerGroup(), this);
------------------------------------------------------------------------
this.mQClientFactory.start();
```


#### **consumeMessageService启动**

```java
this.consumeMessageService.start();
```


#### **MQClientInstance启动流程**
```java
this.mQClientAPIImpl.start();
this.startScheduledTask();
this.pullMessageService.start();
this.rebalanceService.start();
this.defaultMQProducer.getDefaultMQProducerImpl().start(false);
```
#### **NettyRemotingClient启动**
- **启动Netty远程调用Client (4个工作线程)，创建事件执行组并放入Netty管道中**
- **NettyRemotingClient定时扫描 ResponseTable**
- **遍历responseTable ，ConcurrentHashMap类型 ，初始容量256**
- **对于超时请求 进行删除操作**

```java
				this.mQClientAPIImpl.start();
				private int clientWorkerThreads = 4;
				
----------------------------------------------------------------------------------------
 pipeline.addFirst(NettyRemotingClient.this.defaultEventExecutorGroup, "sslHandler", 					NettyRemotingClient.this.sslContext.newHandler(ch.alloc()));
                NettyRemotingClient.log.info("Prepend SSL handler");
-----------------------------------------------------------------------------------------

this.timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                try {
                    NettyRemotingClient.this.scanResponseTable();
                } catch (Throwable var2) {
                    NettyRemotingClient.log.error("scanResponseTable exception", var2);
                }

            }
        }, 3000L, 1000L);
        
        protected final ConcurrentMap<Integer, ResponseFuture> responseTable = new ConcurrentHashMap(256);
        
        if (rf.getBeginTimestamp() + rf.getTimeoutMillis() + 1000L <= System.currentTimeMillis()) {
                rf.release();
                it.remove();
                rfList.add(rf);
                log.warn("remove timeout request, " + rf);
            }
```
- **channelEventListener 不为空， 开启nettyEventExecutor 事件执行器（ 启动ServiceThread线程 ）**

- ```java
  org.apache.rocketmq.remoting.common 属于Netty的ServiceThread
  ```

```java
if (this.channelEventListener != null) {
            this.nettyEventExecutor.start();
        }
```

#### **startScheduledTask启动**

- **每120s 判断 NamesrvAddr地址，若为空，便去获取新的地址**

```java
 private void startScheduledTask() {
        if (null == this.clientConfig.getNamesrvAddr()) {
            this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
                public void run() {
                    try {
                        MQClientInstance.this.mQClientAPIImpl.fetchNameServerAddr();
                    } catch (Exception var2) {
                        MQClientInstance.this.log.error("ScheduledTask fetchNameServerAddr exception", var2);
                    }

                }
            }, 10000L, 120000L, TimeUnit.MILLISECONDS);
        }
```

#### **pullMessageService启动 、 实现消息消费  ( 重点 )**

- ```java
  org.apache.rocketmq.common 属于Rocketmq的ServiceThread
  ```

```java
public void start() {
        log.info("Try to start service thread:{} started:{} lastThread:{}", new Object[]{this.getServiceName(), this.started.get(), this.thread});
        if (this.started.compareAndSet(false, true)) {
            this.stopped = false;
            this.thread = new Thread(this, this.getServiceName());
            this.thread.setDaemon(this.isDaemon);
            this.thread.start();
        }
    }
```

```java
final LinkedBlockingQueue<PullRequest> pullRequestQueue = new LinkedBlockingQueue();
```
- **拉取消息服务**
```java
public void run() {
        this.log.info(this.getServiceName() + " service started");

        while(!this.isStopped()) {
            try {
                PullRequest pullRequest = (PullRequest)this.pullRequestQueue.take();
                
-----------------------------------------------------------------------------------------
                this.pullMessage(pullRequest);
-----------------------------------------------------------------------------------------

            } catch (InterruptedException var2) {
                ;
            } catch (Exception var3) {
                this.log.error("Pull Message Service Run Method exception", var3);
            }
        }

        this.log.info(this.getServiceName() + " service end");
    }
```

- **LinkedBlockingQueue<PullRequest> pullRequestQueue 拉取队列中取出一个拉取请求**
- **获取AtomicInteger、可中断的ReentrantLock重入锁**
- **lockInterruptibly(); 可中断重入锁 (一旦检测到中断请求，方法返回不再参与锁竞争，直接抛出中断异常)**

```java
public E take() throws InterruptedException {
        E x;
        int c = -1;
        final AtomicInteger count = this.count;
        final ReentrantLock takeLock = this.takeLock;
        takeLock.lockInterruptibly();
        try {
            while (count.get() == 0) {
                notEmpty.await();
            }
            x = dequeue();
            c = count.getAndDecrement();
            if (c > 1)
                notEmpty.signal();
        } finally {
            takeLock.unlock();
        }
        if (c == capacity)
            signalNotFull();
        return x;
    }
```

- **dequeue 出队列，出一个PullRequest拉取请求**

- **PullRequest拉取请求包括：消费组，messageQueue（元消息队列包括：topic、brokerName、queueId ）**

- **processQueue 处理队列主要包括：（TreeMap<Long, MessageExt> 存放消息）**

- ```java
  private String consumerGroup;
  private MessageQueue messageQueue;
  private ProcessQueue processQueue;
  private long nextOffset;
  ```

```java
private E dequeue() {
        // assert takeLock.isHeldByCurrentThread();
        // assert head.item == null;
        Node<E> h = head;
        Node<E> first = h.next;
        h.next = h; // help GC
        head = first;
        E x = first.item;
        first.item = null;
        return x;
    }
```

- **拉取消息服务中 this.pullMessage(pullRequest);**
- **获取消费者，准备进行processQueue 消费**

```java
 private void pullMessage(PullRequest pullRequest) {
 
-----------------------------------------------------------------------------------------
        MQConsumerInner consumer = this.mQClientFactory.selectConsumer(pullRequest.getConsumerGroup());
-----------------------------------------------------------------------------------------

        if (consumer != null) {
            DefaultMQPushConsumerImpl impl = (DefaultMQPushConsumerImpl)consumer;
            
-----------------------------------------------------------------------------------------
            impl.pullMessage(pullRequest);
-----------------------------------------------------------------------------------------
        } else {
            this.log.warn("No matched consumer for the PullRequest {}, drop it", pullRequest);
        }
    }
```

- **拉取采用异步回调方法，onSuccess( PullResult pullResult )**

- ```java
  pullResult.getMsgFoundList() 结果为 List<MessageExt> msgFoundList
  ```

- **submitConsumeRequest 两个实现 ConcurrentlyService 和 OrderlyService 多线程消费和顺序消费**

- **executePullRequestImmediately ，将pullRequest put () pullRequestQueue 中**
```java
PullCallback pullCallback = new PullCallback()
public void onSuccess(PullResult pullResult) {
switch(pullResult.getPullStatus()) {
	case FOUND:

pullRequest.setNextOffset(pullResult.getNextBeginOffset());

DefaultMQPushConsumerImpl.this.consumeMessageService.submitConsumeRequest(pullResult.getMsgFoundList(), processQueue, pullRequest.getMessageQueue(), dispatchToConsume);

-----------------------------------------------------------------------------------------
if (DefaultMQPushConsumerImpl.this.defaultMQPushConsumer.getPullInterval() > 0L) {        DefaultMQPushConsumerImpl.this.executePullRequestLater(pullRequest, DefaultMQPushConsumerImpl.this.defaultMQPushConsumer.getPullInterval());
                                            } else {
 DefaultMQPushConsumerImpl.this.executePullRequestImmediately(pullRequest);
                                            }
-----------------------------------------------------------------------------------------                               
 this.pullAPIWrapper.pullKernelImpl(pullRequest.getMessageQueue(), subExpression, subscriptionData.getExpressionType(), subscriptionData.getSubVersion(), pullRequest.getNextOffset(), this.defaultMQPushConsumer.getPullBatchSize(), sysFlag, commitOffsetValue, 15000L, 30000L, CommunicationMode.ASYNC, pullCallback);
 	}
 }
```

- **this.pullAPIWrapper.pullKernelImpl   +  pullCallback 回调方法 处理拉取到消息PullResult**

```java
public class PullResult {
    private final PullStatus pullStatus;
    private final long nextBeginOffset;
    private final long minOffset;
    private final long maxOffset;
    private List<MessageExt> msgFoundList;
```

- **ConcurrentlyService 并发消费服务**  

- **并发消费 和 顺序消费 run执行体 大体相同，** 

- **主要区别在于：生产者向指定queue队列发送消息，跟普通消息相比，顺序消息的使用需要在producer的send()方法中添加MessageQueueSelector接口的实现类，并重写select选择使用的队列，因为顺序消息局部顺序，需要将所有消息指定发送到同一队列中。**

- **消费者 设置最大最小线程数为1，并实现MessageListenerOrderly 接口进行消息消费**

---

- **msgs 小于等于 consumeMessageBatchMaxSize ，new出 consumeRequest ， 在线程池消费**

- ```java
  if (msgs.size() <= consumeBatchSize) 
  ```

- **若大于consumeMessageBatchMaxSize ，每次只能消费consumeMessageBatchMaxSize 数量的消息**

- ```java
  private String consumerGroup;
  private List&lt;MessageExt&gt; msgList;
  private MessageQueue mq;
  private boolean success;
  private String status;
  private Object mqTraceContext;
  private Map&lt;String, String&gt; props;
  private String namespace;
  ```
  
- **并发消费线程池**
```java
this.consumeExecutor = new ThreadPoolExecutor(this.defaultMQPushConsumer.getConsumeThreadMin(), this.defaultMQPushConsumer.getConsumeThreadMax(), 60000L, TimeUnit.MILLISECONDS, this.consumeRequestQueue, new ThreadFactoryImpl("ConsumeMessageThread_"));
```
- **MessageListenerConcurrently 并发消费监听接口**
- ```java
  public interface MessageListenerConcurrently extends MessageListener {
      ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> var1, ConsumeConcurrentlyContext var2);
  }
  ```
```java
MessageListenerConcurrently listener = ConsumeMessageConcurrentlyService.this.messageListener;
                ConsumeConcurrentlyContext context = new  
                ConsumeConcurrentlyContext(this.messageQueue);
                ConsumeConcurrentlyStatus status = null;
                ConsumeMessageConcurrentlyService.this.defaultMQPushConsumerImpl.resetRetryAndNamespace(this.msgs,ConsumeMessageConcurrentlyService.this.defaultMQPushConsumer.getConsumerGroup());
                ConsumeMessageContext consumeMessageContext = null;
     if (ConsumeMessageConcurrentlyService.this.defaultMQPushConsumerImpl.hasHook()) {
                    consumeMessageContext = new ConsumeMessageContext();
                    consumeMessageContext.setNamespace(ConsumeMessageConcurrentlyService.this.defaultMQPushConsumer.getNamespace());
                    consumeMessageContext.setConsumerGroup(ConsumeMessageConcurrentlyService.this.defaultMQPushConsumer.getConsumerGroup());
                    consumeMessageContext.setProps(new HashMap());
                    consumeMessageContext.setMq(this.messageQueue);
                    consumeMessageContext.setMsgList(this.msgs);
                    consumeMessageContext.setSuccess(false);
                    ConsumeMessageConcurrentlyService.this.defaultMQPushConsumerImpl.executeHookBefore(consumeMessageContext);
                }
                long beginTimestamp = System.currentTimeMillis();
                boolean hasException = false;
                ConsumeReturnType returnType = ConsumeReturnType.SUCCESS;
```

- **OrderlyService 顺序消费服务**

- **new出 consumeRequest ， 在线程池消费**

- ```java
  ConsumeMessageOrderlyService.ConsumeRequest consumeRequest = new ConsumeMessageOrderlyService.ConsumeRequest(processQueue, messageQueue);
  ```
  
- **consumeExecutor 顺序消费线程池 执行 consumeRequest**

- ```java
  this.consumeExecutor.submit(consumeRequest);
  ```

- ```java
  this.consumeExecutor = new ThreadPoolExecutor(this.defaultMQPushConsumer.getConsumeThreadMin(), this.defaultMQPushConsumer.getConsumeThreadMax(), 60000L, TimeUnit.MILLISECONDS, this.consumeRequestQueue, new ThreadFactoryImpl("ConsumeMessageThread_"));
  ```
  
- **consumeRequest实现Runnable接口，下面是它的run()方法**

- ```java
  class ConsumeRequest implements Runnable {
      private final ProcessQueue processQueue;
      private final MessageQueue messageQueue;
  ```
  
-  **取出List<MessageExt> 消息集合**
```java
List<MessageExt> msgs = this.processQueue.takeMessags(consumeBatchSize);
```
- **resetRetryAndNamespace  过滤重投消息**

```java
ConsumeMessageOrderlyService.this.defaultMQPushConsumerImpl.resetRetryAndNamespace(msgs, ConsumeMessageOrderlyService.this.defaultMQPushConsumer.getConsumerGroup());
```
- **遍历 消息集合， 找出属性为RETRY_TOPIC 重投的消息 ， 设置该消息的topic** 

- ```java
  String retryTopic = msg.getProperty("RETRY_TOPIC");
  if (retryTopic != null && groupTopic.equals(msg.getTopic())) {
      msg.setTopic(retryTopic);
  }
  
  if (StringUtils.isNotEmpty(this.defaultMQPushConsumer.getNamespace())) {
      msg.setTopic(NamespaceUtil.withoutNamespace(msg.getTopic(), this.defaultMQPushConsumer.getNamespace()));
  }
  ```
```java
if (!msgs.isEmpty()) {
          ConsumeOrderlyContext context = new ConsumeOrderlyContext(this.messageQueue);
          ConsumeOrderlyStatus status = null;
          ConsumeMessageContext consumeMessageContext = null;
       if (ConsumeMessageOrderlyService.this.defaultMQPushConsumerImpl.hasHook()) {
                consumeMessageContext = new ConsumeMessageContext();
                                        consumeMessageContext.setConsumerGroup(ConsumeMessageOrderlyService.this.defaultMQPushConsumer.getConsumerGroup());
                                        consumeMessageContext.setNamespace(ConsumeMessageOrderlyService.this.defaultMQPushConsumer.getNamespace());
                consumeMessageContext.setMq(this.messageQueue);
                consumeMessageContext.setMsgList(msgs);
                consumeMessageContext.setSuccess(false);
                consumeMessageContext.setProps(new HashMap());
                                        ConsumeMessageOrderlyService.this.defaultMQPushConsumerImpl.executeHookBefore(consumeMessageContext);
                         }

              long beginTimestamp = System.currentTimeMillis();
              ConsumeReturnType returnType = ConsumeReturnType.SUCCESS;
```
- **MessageListenerOrderly 顺序消费监听接口 继承了 messageListener 接口**  

- **MessageListenerOrderly  就是 我们在设置 监听订阅时 回调用的接口，重写此方法进行消息消费**

- ```java
  public interface MessageListenerOrderly extends MessageListener {
      ConsumeOrderlyStatus consumeMessage(List<MessageExt> var1, ConsumeOrderlyContext var2);
  }
  ```
- **就是这一行，如果重写乐监听接口，就能消费消息**
```java
-----------------------------------------------------------------------------------------
status = ConsumeMessageOrderlyService.this.messageListener.consumeMessage(
    Collections.unmodifiableList(msgs), context  );
-----------------------------------------------------------------------------------------
```
- **消息在消费前后  executeHookBefore，executeHookAfter  （ Hook进行调用 ）**
```java
    if (ConsumeMessageOrderlyService.this.defaultMQPushConsumerImpl.hasHook()) {
                                        consumeMessageContext.setStatus(status.toString());
                                        consumeMessageContext.setSuccess(ConsumeOrderlyStatus.SUCCESS == status || ConsumeOrderlyStatus.COMMIT == status);
                                        ConsumeMessageOrderlyService.this.defaultMQPushConsumerImpl.executeHookAfter(consumeMessageContext);
                                    }
```



#### **rebalanceService启动**

- **和pullMessageService一样启动 Rocketmq的ServiceThread**

- **同一个抽象类 rebalanceService 和 pullMessageService 为具体实现**

- **public abstract class ServiceThread implements Runnable**

- **等待间隔**

```java
private static long waitInterval = Long.parseLong(System.getProperty("rocketmq.client.rebalance.waitInterval", "20000"));
```

```java
  public void run() {
        this.log.info(this.getServiceName() + " service started");

        while(!this.isStopped()) {
            this.waitForRunning(waitInterval);
            this.mqClientFactory.doRebalance();
        }
    
        this.log.info(this.getServiceName() + " service end");
    }
```
- **进行负载**

```java
public void doRebalance() {
        Iterator var1 = this.consumerTable.entrySet().iterator();

        while(var1.hasNext()) {
            Entry<String, MQConsumerInner> entry = (Entry)var1.next();
            MQConsumerInner impl = (MQConsumerInner)entry.getValue();
            if (impl != null) {
                try {
                    impl.doRebalance();
                } catch (Throwable var5) {
                    this.log.error("doRebalance exception", var5);
                }
            }
        }
```

```java
 public void doRebalance() {
        if (!this.pause) {
            this.rebalanceImpl.doRebalance(this.isConsumeOrderly());
        }

    }
```
- **获取订阅列表**
- **广播和集群两种模式    BROADCASTING:      CLUSTERING:**

```java
private void rebalanceByTopic(String topic, boolean isOrder) {
        Set mqSet;

-----------------------------------------------------------------------------------------
广播模式：：：：
        switch(this.messageModel) {
        case BROADCASTING:
            mqSet = (Set)this.topicSubscribeInfoTable.get(topic);
            if (mqSet != null) {
-----------------------------------------------------------------------------------------

清理不重要的消息 （ 同一个topic下，清理没有在topicSubscribeInfoTable订阅列表中的MessageQueue ）  
boolean changed = this.updateProcessQueueTableInRebalance(topic, mqSet, isOrder);

-----------------------------------------------------------------------------------------
   if (changed) {
     this.messageQueueChanged(topic, mqSet, mqSet);
     log.info("messageQueueChanged {} {} {} {}", new Object[]{this.consumerGroup, topic, mqSet, mqSet});
            }
            } else {
     log.warn("doRebalance, {}, but the topic[{}] not exist.", this.consumerGroup, topic);
            }
            break;
            
```

- **集群模式下：：：**
- **获取同一个 消费组中 ，订阅同一个topic的 消费者列表**

```java
集群模式：：：：
      case CLUSTERING:
      mqSet = (Set)this.topicSubscribeInfoTable.get(topic);
  List<String> cidAll = this.mQClientFactory.findConsumerIdList(topic, this.consumerGroup);
            if (null == mqSet && !topic.startsWith("%RETRY%")) {
      log.warn("doRebalance, {}, but the topic[{}] not exist.", this.consumerGroup, topic);
            }

            if (null == cidAll) {
     log.warn("doRebalance, {} {}, get consumer id list failed", this.consumerGroup, topic);
            }

            if (mqSet != null && cidAll != null) {
                List<MessageQueue> mqAll = new ArrayList();
                mqAll.addAll(mqSet);
                Collections.sort(mqAll);
                Collections.sort(cidAll);
  AllocateMessageQueueStrategy strategy = this.allocateMessageQueueStrategy;
                List allocateResult = null;
```

- **allocateMessageQueueStrategy 分配消息队列策略**
- **为当前消费端 分配消息队列MessageQueue**
```java
                try {
         allocateResult = strategy.allocate(this.consumerGroup, this.mQClientFactory.getClientId(), mqAll, cidAll);
                } catch (Throwable var10) {
                    log.error("AllocateMessageQueueStrategy.allocate Exception. allocateMessageQueueStrategyName={}", strategy.getName(), var10);
                    return;
                }

                Set<MessageQueue> allocateResultSet = new HashSet();
                if (allocateResult != null) {
                    allocateResultSet.addAll(allocateResult);
                }

           boolean changed = this.updateProcessQueueTableInRebalance(topic, allocateResultSet, isOrder);
                if (changed) {
                    log.info("rebalanced result changed. allocateMessageQueueStrategyName={}, group={}, topic={}, clientId={}, mqAllSize={}, cidAllSize={}, rebalanceResultSize={}, rebalanceResultSet={}", new Object[]{strategy.getName(), this.consumerGroup, topic, this.mQClientFactory.getClientId(), mqSet.size(), cidAll.size(), allocateResultSet.size(), allocateResultSet});
                    this.messageQueueChanged(topic, mqSet, allocateResultSet);
                }
            }
        }

    }
```



#### **DefaultMQProducerImpl （消费端默认false，不启动）**

- **true的话，传入生产者相关配置 class DefaultMQProducer extends ClientConfig 创建生产者实例**
- **在producerTable生产者列表中 ， 进行生产者客户端注册（ 本质ConcurrentMap ）**

```java
  this.mQClientFactory = MQClientManager.getInstance().getOrCreateMQClientInstance(this.defaultMQProducer, this.rpcHook);
  boolean registerOK = this.mQClientFactory.registerProducer(this.defaultMQProducer.getProducerGroup(), this);


ConcurrentMap<String, MQProducerInner> producerTable;
```