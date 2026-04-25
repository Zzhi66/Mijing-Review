package com.mj.mijing.utils;

/**
 * 分布式锁接口
 */
public interface ILock {
    /**
     * 尝试获取锁
     * @param timeoutSec 超时时间（秒）
     */
    boolean tryLock(long timeoutSec);

    /**
     * 释放锁
     */
    void unlock();
}
