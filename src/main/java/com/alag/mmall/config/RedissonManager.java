package com.alag.mmall.config;

import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@Slf4j
public class RedissonManager {
    private Config config = new Config();
    private Redisson redisson = null;

    public Redisson getRedisson() {
        return redisson;
    }

    @Value("${spring.redis.host}")
    private String host;
    @Value("${spring.redis.port}")
    private String port;
    @Value("${spring.redis.password}")
    private String password;

    //在构造方法执行完成之后执行
    @PostConstruct
    private void init() {
        try {
            config.useSingleServer().setAddress(new StringBuilder("redis://").append(host).append(":").append(port).toString()).setPassword(password);
            redisson = (Redisson) Redisson.create(config);
            log.info("init Redisson successful");
        } catch (Exception e) {
            log.info("init Redisson fail");
            e.printStackTrace();
        }
    }


}
