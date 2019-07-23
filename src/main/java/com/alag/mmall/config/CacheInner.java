package com.alag.mmall.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public final class CacheInner {

	@Autowired
	private RedisService redisService;

	private static CacheInner cacheInner;

	/**
	 * 防止spring注入的数据被清空
	 */
	@PostConstruct
	public void init() {
		cacheInner = this;
		cacheInner.redisService = this.redisService;
	}

	public static boolean set(String cacheType, String key, Object value) {
		return cacheInner.redisService.set((cacheType + key), value);
	}
	
	public static boolean set(String cacheType, String key, Object value, Long expireTime) {
		return cacheInner.redisService.set((cacheType + key), value);
	}

	public static Object get(String cacheType, String key) {
		return cacheInner.redisService.get((cacheType + key));
	}

	public static boolean exists(String cacheType, String key) {
		return cacheInner.redisService.exists((cacheType + key));
	}

	public static boolean remove(String cacheType, String key) {
		return cacheInner.redisService.remove((cacheType + key));
	}

}
