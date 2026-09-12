-- 滑动窗口限流（原子执行）
-- KEYS[1] 限流 key
-- ARGV[1] 当前时间(ms)  ARGV[2] 窗口大小(ms)  ARGV[3] 阈值  ARGV[4] 本次请求唯一标识
local key = KEYS[1]
local now = tonumber(ARGV[1])
local window = tonumber(ARGV[2])
local limit = tonumber(ARGV[3])
local member = ARGV[4]

-- 先剔除窗口外的历史请求
redis.call('ZREMRANGEBYSCORE', key, 0, now - window)
local count = redis.call('ZCARD', key)
if count < limit then
    redis.call('ZADD', key, now, member)
    redis.call('PEXPIRE', key, window)
    return 1
end
return 0