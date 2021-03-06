package com.ynr.utils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisPool {

	public static JedisPool pool = null;
	
	static {
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxTotal(1024);
		config.setMaxIdle(10);
		config.setMaxWaitMillis(10000);
		config.setTestOnBorrow(true);
		config.setTestOnReturn(true);
		pool = new JedisPool(config,"127.0.0.1",6379,10000,"nji90OKM");
	}

	public static void main(String[] args){
		Jedis jedis = RedisPool.pool.getResource();
		System.out.println(jedis.set("test", "111"));
		System.out.println(jedis.get("test"));
		jedis.close();
	}
	
}
