-- 分布式锁释放 Lua 脚本（判断+删除原子化，防误删）
-- KEYS[1]=lockKey  ARGV[1]=threadId
if redis.call('get', KEYS[1]) == ARGV[1] then
    return redis.call('del', KEYS[1])
end
return 0
