-- 填充 medicine 表 indication 字段（适应症）
-- 执行: mysql -u root -p medical_system < update_indication.sql

-- 已有适应症的保留，仅填充缺失的
UPDATE medicine SET indication = '2型糖尿病' WHERE medicine_id = 14 AND (indication IS NULL OR indication = '');
UPDATE medicine SET indication = '胃酸过多、胃溃疡' WHERE medicine_id = 15 AND (indication IS NULL OR indication = '');
UPDATE medicine SET indication = '抑郁症、焦虑症' WHERE medicine_id = 16 AND (indication IS NULL OR indication = '');
UPDATE medicine SET indication = '轻中度抑郁、焦虑' WHERE medicine_id = 17 AND (indication IS NULL OR indication = '');
UPDATE medicine SET indication = '轻中度抑郁、焦虑' WHERE medicine_id = 18 AND (indication IS NULL OR indication = '');
UPDATE medicine SET indication = '焦虑症' WHERE medicine_id = 19 AND (indication IS NULL OR indication = '');
UPDATE medicine SET indication = '肌肉痉挛' WHERE medicine_id = 20 AND (indication IS NULL OR indication = '');
UPDATE medicine SET indication = '失眠' WHERE medicine_id = 21 AND (indication IS NULL OR indication = '');
UPDATE medicine SET indication = '焦虑症' WHERE medicine_id = 22 AND (indication IS NULL OR indication = '');
UPDATE medicine SET indication = '肌肉痉挛' WHERE medicine_id = 23 AND (indication IS NULL OR indication = '');
UPDATE medicine SET indication = '失眠' WHERE medicine_id = 24 AND (indication IS NULL OR indication = '');
UPDATE medicine SET indication = '饮食控制、辅助降压降糖降脂' WHERE medicine_id = 25 AND (indication IS NULL OR indication = '');
UPDATE medicine SET indication = '高血压、2型糖尿病伴微量白蛋白尿' WHERE medicine_id = 26 AND (indication IS NULL OR indication = '');
UPDATE medicine SET indication = '轻中度抑郁、焦虑' WHERE medicine_id = 27 AND (indication IS NULL OR indication = '');
UPDATE medicine SET indication = '焦虑症' WHERE medicine_id = 28 AND (indication IS NULL OR indication = '');
UPDATE medicine SET indication = '肌肉痉挛' WHERE medicine_id = 29 AND (indication IS NULL OR indication = '');
UPDATE medicine SET indication = '抑郁症、焦虑症' WHERE medicine_id = 34 AND (indication IS NULL OR indication = '');
UPDATE medicine SET indication = '失眠' WHERE medicine_id = 35 AND (indication IS NULL OR indication = '');
UPDATE medicine SET indication = '轻中度抑郁、焦虑' WHERE medicine_id = 36 AND (indication IS NULL OR indication = '');
UPDATE medicine SET indication = '焦虑症' WHERE medicine_id = 37 AND (indication IS NULL OR indication = '');
UPDATE medicine SET indication = '肌肉痉挛' WHERE medicine_id = 38 AND (indication IS NULL OR indication = '');
UPDATE medicine SET indication = '失眠' WHERE medicine_id = 39 AND (indication IS NULL OR indication = '');
UPDATE medicine SET indication = '2型糖尿病' WHERE medicine_id = 40 AND (indication IS NULL OR indication = '');

-- 清理乱码数据（id 30-33 为乱码，跳过）
