package org.redis.service;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.redis.entity.User;
import org.redis.util.KafkaUtil;
import org.redis.util.RedisUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * 模仿用户
 */
@Service
public class UserServiceImpl {

    @Resource
    private RedisUtil redisUtil;
    @Resource
    private KafkaUtil kafkaUtil;

    public String getUserById(String id) {
        return "用户：" + id;
    }

    /**
     * 获取redis String数据
     * @param key key
     * @return String
     */
    public String getStringByKey(String key) {
        return "redis String: " + redisUtil.get(key);
    }

    /**
     * 设置redis String
     * @return String
     */
    public String setString() {
        int age = 18;
        for (int i = 0; i < 10; i++) {
            User user = new User();
            user.setName("张三"+i);
            user.setAge(age++);
            user.setBirthday(new Date());
           if (!redisUtil.set("user"+i, user)) {
               return "error";
           }
        }
        return "success";
    }

    /**
     * kafka发送数据
     * @return String
     */
    public String kafkaSend() throws ExecutionException, InterruptedException {
        for (int i=0; i<3; i++) {
            kafkaUtil.send("testHello", null, "hello"+i, "WORLD"+i);
        }
        return "kafkaSend";
    }

    /**
     * 创建topic
     * @return String
     */
    public String createTopic() {
        kafkaUtil.createTopic("testHello", 1,(short)0);
        return "createTopic";
    }

    /**
     * 获取所有topic名称
     * @return List<String>
     * @throws ExecutionException e
     * @throws InterruptedException i
     */
    public List<String> listTopic() throws ExecutionException, InterruptedException {
        return kafkaUtil.listTopic();
    }

    /**
     * 消费者指定topic拉取数据
     * @param topic 主题
     * @return List<ConsumerRecords<String, Object>>
     */
    public List<ConsumerRecords<String, Object>> poll(String topic) {
        return kafkaUtil.poll(topic);
    }
}
