package com.alag.mmall.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@Slf4j
public class RedisConfig {

	@Autowired
	private RedisTemplate<?, ?> redisTemplate;

	/*
	 * 解决redis插入中文乱码
	 */
	@Bean
	@Scope("prototype")
	public RedisTemplate<?, ?> redisTemplate() {

		// 设置序列化Key的实例化对象
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		// 设置序列化Value的实例化对象
		redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());

		redisTemplate.setHashKeySerializer(new StringRedisSerializer());
		redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
		log.info("解决了redis中文插入乱码问题");

		return redisTemplate;

	}

}
