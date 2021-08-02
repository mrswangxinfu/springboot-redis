package org.redis.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.redis.service.UserServiceImpl;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserServiceImpl service;

    @GetMapping("/get/{id}")
    public String getUser(@PathVariable(value = "id") String id) {
        return service.getUserById(id);
    }
    @GetMapping("/getString")
    public String getStringByKey(@RequestParam(value = "key") String key) {
        return service.getStringByKey(key);
    }

    @GetMapping("/setString")
    public String setString() {
        return service.setString();
    }
    @GetMapping("/kafka/send")
    public String sent() throws ExecutionException, InterruptedException {
        return service.kafkaSend();
    }
    @GetMapping("/kafka/createTopic")
    public String createTopic() {
        return service.createTopic();
    }

    @GetMapping("/kafka/listTopic")
    public List<String> listTopic() throws ExecutionException, InterruptedException {
        return service.listTopic();
    }

    @GetMapping("/kafka/poll")
    public List<ConsumerRecords<String, Object>> poll(@RequestParam(value = "topic")String topic) {
        return service.poll(topic);
    }
}
