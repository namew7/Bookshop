local stockKey = KEYS[1]
local countStr = ARGV[1]

-- 1. 【新增】Lua 层面的参数校验
-- 如果传进来不是数字，或者传了负数/小数，直接驳回
local count = tonumber(countStr)
if not count or count <= 0 or math.floor(count) ~= count then
    return -2 -- 返回一个特定的错误码，代表参数非法
end

-- 2. 获取库存
local currentStock = tonumber(redis.call('get', stockKey))

-- 3. 业务逻辑
if not currentStock then
    return -1 -- 未上架
end

if currentStock >= count then
    redis.call('decrby', stockKey, count)
    return 1 -- 成功
else
    return 0 -- 库存不足
end