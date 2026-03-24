package com.sky.aspect;

import com.sky.annotation.ClearCache;
import com.sky.exception.BaseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class ClearCacheAspect {

    private final RedisTemplate<String, Object> redisTemplate;

    // 拦截所有加了 @ClearCache 的方法
    @AfterReturning("@annotation(clearCache)")
    public void clearCache(ClearCache clearCache) {
        String pattern = clearCache.prefix();

        //scan扫描需删除的key,不阻塞 Redis
        Set<String> keys = scanKeys(pattern);

        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("清理Redis缓存: {} ,共 {} 条", pattern, keys.size());
        } else {
            log.info("未扫描到Redis缓存{}", pattern);
        }
    }

    /**
     * 工具函数 scan非阻塞查找KEY
     *
     * @param pattern
     * @return 匹配的key
     */
    private Set<String> scanKeys(String pattern) {
        return redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            Set<String> keys = new HashSet<>();

            try (Cursor<byte[]> cursor = connection.scan(ScanOptions.scanOptions()
                    .match(pattern)
                    .count(100)  // 每次扫描100条，不阻塞
                    .build())) {
                while (cursor.hasNext()) {
                    keys.add(new String(cursor.next()));
                }
            } catch (Exception e) {
                log.error("扫描Redis缓存异常", e);
                throw new BaseException("扫描Redis缓存异常");
            }
            return keys;
        });
    }
}