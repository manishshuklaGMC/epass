package curfew.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;

@Service
public class RedisService {
  private final Jedis jedis;

  public RedisService(@Value("${redisHost}") String host, @Value("${redisPort}") int port) {
    jedis = new Jedis(new HostAndPort(host, port));
  }

  public void setKey(String key, String value) {
    jedis.set(key, value);
  }

  public String getKey(String key) {
    return jedis.get(key);
  }
}
