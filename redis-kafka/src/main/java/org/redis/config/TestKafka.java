package org.redis.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.SendResult;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class TestKafka {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        producer();
        consumer();
    }

    public static void producer() throws ExecutionException, InterruptedException {
        Map<String, Object> props = new HashMap<>();
        // kafka的服务器地址
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        // 当生产者生产数据到kafka中，kafka会以什么样的策略返回，all或者-1表示等待leader和副本完成数据同步
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        // key和value进行序列化
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        ProducerFactory<String, Object> producerFactory = new DefaultKafkaProducerFactory<String, Object>(props);
        KafkaTemplate<String,Object> kafkaTemplate = new KafkaTemplate<String,Object>(producerFactory);
        for (int i = 0; i < 20; i++) {
            ProducerRecord<String,Object> record = new ProducerRecord<String, Object>("testhello",null, i+"zs");
            Future<SendResult<String, Object>> future = kafkaTemplate.send(record);
            Object callback = future.get();
            System.out.println("设置：" + callback);
        }
    }
    public static void consumer() {
        Map<String, Object> props = new HashMap<>();
        // kafka服务地址
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        // 设置是否自动提交
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");

        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "1000");
        // 反序列化
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "groupTopic");
        KafkaConsumer<String, Object> kafkaConsumer = new KafkaConsumer<String, Object>(props);
        kafkaConsumer.subscribe(Collections.singletonList("testHello"));
        int i = 20;
        while (i > 0) {
            // 2秒拉取一次
            ConsumerRecords<String, Object> consumerRecords = kafkaConsumer.poll(Duration.ofSeconds(2));
            for (ConsumerRecord<String, Object> record : consumerRecords) {
                String topicName = record.topic();
                long offset = record.offset();
                String key = record.key();
                Object value = record.value();
                System.out.println("---topic: " + topicName + "--offset: " + offset + "--key: " + key + "--value: " + value);
            }
            --i;
        }
    }
}
