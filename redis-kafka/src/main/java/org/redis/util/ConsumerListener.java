package org.redis.util;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 设计思路：可以将一个类作为一个消费者组由spring管理，一个组里的每个方法监听消费一个topic中的一个分区
 * 即：一个消费者只能消费一个topic中的一个分区
 */
@Component
public class ConsumerListener {

    /**
     * 每次监听返回的数据
     * 这里是批量取出
     * @param record
     */
    @KafkaListener(topics = "testHello",topicPartitions = {},containerFactory = "containerFactory",groupId = "groupTopic")
    public List<String>  listener(List<ConsumerRecord<?,?>> record) {
        List<String> messages = new ArrayList<>();
        record.forEach(records -> {
            Optional<?> kafkaMessage = Optional.ofNullable(records);
            kafkaMessage.ifPresent(o -> messages.add(o.toString()));
        });
        System.out.println("监听消费---messages:==" + messages);
        return messages;
    }
}
