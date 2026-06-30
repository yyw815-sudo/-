-- 清理用药计划中的重复药品
-- 对于每个病历，每种药品只保留一条记录（保留最早创建的那条）
-- 注意：这个脚本需要手动在MySQL客户端中执行

-- 步骤1：找出需要保留的计划ID（每个record_id + medicine_id 组合只保留一条）
-- 步骤2：将重复的计划状态设置为0（停用）

-- 首先查看当前的重复情况
SELECT 
    record_id,
    medicine_id,
    COUNT(*) as duplicate_count,
    GROUP_CONCAT(plan_id ORDER BY create_time) as plan_ids
FROM medicationplan 
WHERE status = 1 
GROUP BY record_id, medicine_id 
HAVING COUNT(*) > 1;

-- 实际执行清理（取消注释下面的语句执行）
/*
-- 创建临时表存储需要保留的计划ID
CREATE TEMPORARY TABLE keep_plans AS
SELECT MIN(plan_id) as keep_id
FROM medicationplan 
WHERE status = 1 
GROUP BY record_id, medicine_id;

-- 将重复的计划状态设置为0
UPDATE medicationplan 
SET status = 0 
WHERE status = 1 
AND plan_id NOT IN (SELECT keep_id FROM keep_plans);

-- 删除临时表
DROP TEMPORARY TABLE IF EXISTS keep_plans;
*/
