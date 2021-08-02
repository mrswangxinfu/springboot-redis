package org.redis.entity;

import lombok.Data;

@Data
public class ConsumerData {
    private  String topic;
    private Long offset;
    private String key;
    private Object value;
}
