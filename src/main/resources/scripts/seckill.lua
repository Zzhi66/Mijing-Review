-- 秒杀资格校验 Lua 脚本
-- 参数：KEYS[1]=库存Key KEYS[2]=已购用户集合Key ARGV[1]=userId ARGV[2]=orderId
-- 返回：0成功 1库存不足 2已购买

-- 判断库存是否充足
local stock = tonumber(redis.call('get', KEYS[1]))
if stock == nil or stock <= 0 then
    return 1
end

-- 判断用户是否已下单（一人一单）
local hasBought = redis.call('sismember', KEYS[2], ARGV[1])         
if tonumber(hasBought) == 1 then
    return 2
end

-- 扣减库存
redis.call('incrby', KEYS[1], -1)

-- 记录已购用户
redis.call('sadd', KEYS[2], ARGV[1])

return 0
    