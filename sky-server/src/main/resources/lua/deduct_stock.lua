local key = KEYS[1]
local deductNum = tonumber(ARGV[1])
local initStock = tonumber(ARGV[2])

local stock = redis.call('get', key)
if not stock then
    if not initStock then
        return -1
    end
    redis.call('set', key, initStock)
    stock = tostring(initStock)
end

if tonumber(stock) < deductNum then
    return -2
end

return redis.call('decrby', key, deductNum)
