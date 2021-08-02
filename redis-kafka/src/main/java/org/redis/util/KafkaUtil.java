package org.redis.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Component
@Slf4j
public class KafkaUtil {
    @Resource
    private KafkaTemplate<String, Object> kafkaTemplate;
    @Resource
    private Admin admin;
    @Resource
    private KafkaConsumer<String, Object> kafkaConsumer;

    /**
     * 生产者生产消息
     * @param topic 主题
     * @param partition 分区
     * @param key 键
     * @param value 值
     */
    public void send(String topic, Integer partition, String key, Object value) throws ExecutionException, InterruptedException {
        ProducerRecord<String,Object> record = new ProducerRecord<String, Object>(topic, partition, key, value);
        Future<SendResult<String, Object>> future = kafkaTemplate.send(record);
        Object callback = future.get();
        System.out.println("设置完成：" + callback);
    }

    /**
     * 消费者指定topic消费
     * @param topic 主题
     */
    public List<ConsumerRecords<String, Object>> poll(String topic) {
        List<ConsumerRecords<String, Object>> consumerRecordsList = new ArrayList<>();
        kafkaConsumer.subscribe(Collections.singletonList(topic));
        int i = 4;
        while (i > 0) {
            // 2秒拉取一次
            ConsumerRecords<String, Object> consumerRecords = kafkaConsumer.poll(Duration.ofSeconds(2));
            consumerRecordsList.add(consumerRecords);
            for (ConsumerRecord<String, Object> record : consumerRecords) {
                String topicName = record.topic();
                long offset = record.offset();
                String key = record.key();
                Object value = record.value();
                log.info("---topic: " + topicName + "--offset: " + offset + "--key: " + key + "--value: " + value);
            }
            --i;
        }
        kafkaConsumer.close();
        return consumerRecordsList;
    }
    /**
     * 创建主题
     * @param topic 主题名称
     * @param partitionNum 分区数
     * @param replicationNum 副本数
     */
    public void createTopic(String topic, Integer partitionNum, Short replicationNum) {
        admin.createTopics(Collections.singletonList(new NewTopic(topic, partitionNum, replicationNum)));
    }

    /**
     * 获取主题
     * @return list
     */
    public List<String> listTopic() throws ExecutionException, InterruptedException {
        ListTopicsResult result = admin.listTopics();
        return new ArrayList<>(result.names().get());
    }
}
