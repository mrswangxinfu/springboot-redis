package org.redis.config;

import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * kafka消费者配置类
 */
@Configuration
@EnableKafka
public class KafkaConfig {
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    //把YML写的引入进来
    @Value("${spring.kafka.properties.sasl.mechanism}")
    private String saslMechanism;
    //把YML写的引入进来
    @Value("${spring.kafka.properties.security.protocol}")
    private String securityProtocol;
    @Value("${spring.kafka.properties.sasl.jaas.config}")
    private String saslJaasConfig;
    @Value("${spring.kafka.consumer.enable-auto-commit}")
    private String autoCommit;
    @Value("${spring.kafka.consumer.auto-commit-interval}")
    private String interval;
    @Value("${spring.kafka.consumer.max-poll-records-config}")
    private String maxPollRecords;
    @Value("${spring.kafka.producer.acks}")
    private String acks;
    @Value("${spring.kafka.consumer.value-deserializer}")
    private String valueDeserializer;
    @Value("${spring.kafka.consumer.key-deserializer}")
    private String keyDeserializer;
    @Value("${spring.kafka.producer.value-serializer}")
    private String valueSerializer;
    @Value("${spring.kafka.producer.key-serializer}")
    private String keySerializer;


    /**
     * KafkaTemplate
     * @return KafkaTemplate<String, Object>
     */
    @Bean
    public KafkaTemplate<String, Object> getKafkaTemplate() {
        ProducerFactory<String, Object> producerFactory = new DefaultKafkaProducerFactory<String, Object>(producerProps());
        return new KafkaTemplate<String,Object>(producerFactory);
    }

    /**
     * 消费者工厂
     * @return
     */
    @Bean("containerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, String> containerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> container = new ConcurrentKafkaListenerContainerFactory<>();
        container.setConsumerFactory(new DefaultKafkaConsumerFactory<>(consumerProps()));
        // 设置并发量，小于或等于Topic的分区数
        container.setConcurrency(1);
        // 设置为批量监听
        container.setBatchListener(true);
        // 设置提交偏移量的方式
        container.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return container;
    }

    /**
     * kafka管理者
     * @return
     */
    @Bean
    public Admin getAdmin() {
        return AdminClient.create(producerProps());
    }

    /**
     * kafka消费者
     * @return
     */
    @Bean
    public KafkaConsumer<String, Object> getConsumer() {
        return new KafkaConsumer<String, Object>(consumerProps());
    }
    /**
     * 生产者配置
     * @return
     */
    private Map<String, Object> producerProps() {
        Map<String, Object> props = new HashMap<>();
        // kafka的服务器地址
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        // 当生产者生产数据到kafka中，kafka会以什么样的策略返回，all或者-1表示等待leader和副本完成数据同步
        props.put(ProducerConfig.ACKS_CONFIG, acks);
        // key和value进行序列化
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializer);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer);
        return props;
    }

    /**
     * 消费者配置
     * @return
     */
    private Map<String, Object> consumerProps() {
        Map<String, Object> props = new HashMap<>();
        // kafka服务地址
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        // 设置是否自动提交
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, autoCommit);
        // 一次拉取消息数量
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);

        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, interval);
        // 反序列化
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyDeserializer);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializer);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "groupTopic");
//        props.put("sasl.mechanism",saslMechanism);
//        props.put("security.protocol",securityProtocol);
//        props.put("sasl.jaas.config",saslJaasConfig);
        return props;
    }

}
