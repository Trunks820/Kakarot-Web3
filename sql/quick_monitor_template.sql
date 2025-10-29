-- Token智能监控配置模板表
-- 用于存储用户自定义的分段监控配置

CREATE TABLE IF NOT EXISTS `quick_monitor_template` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户ID（可选，支持多用户）',
  `chain_type` varchar(20) DEFAULT 'sol' COMMENT '链类型：sol/bsc/eth',
  `min_market_cap` decimal(20,2) NOT NULL COMMENT '最低市值门槛（USD）',
  `config_name` varchar(100) DEFAULT NULL COMMENT '配置名称（如：轻度监控、严格监控）',
  `events_config` text COMMENT '监控事件配置JSON',
  `notify_methods` varchar(100) DEFAULT '' COMMENT '通知方式（逗号分隔）：telegram,wechat（Web通知默认启用）',
  `trigger_logic` varchar(20) DEFAULT 'any' COMMENT '触发逻辑：any任一满足/all全部满足',
  `sort_order` int(11) DEFAULT '0' COMMENT '排序序号（数值越大优先级越高）',
  `status` char(1) DEFAULT '1' COMMENT '状态：0停用 1启用',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注说明',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_chain` (`user_id`, `chain_type`),
  KEY `idx_market_cap` (`min_market_cap`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Token智能监控配置模板';

-- 插入默认配置示例
INSERT INTO `quick_monitor_template` 
  (`chain_type`, `min_market_cap`, `config_name`, `events_config`, `notify_methods`, `trigger_logic`, `sort_order`, `status`, `remark`) 
VALUES
  -- 高市值配置：严格监控
  ('sol', 1000000.00, '严格监控', 
   '{"priceChange":{"enabled":true,"risePercent":20,"fallPercent":15},"holders":{"enabled":true,"increasePercent":15,"decreasePercent":10},"volume":{"enabled":true,"threshold":20000}}',
   'telegram,wechat', 'any', 2, '1', '适用于市值≥100万的成熟Token'),
   
  -- 中等市值配置：中度监控
  ('sol', 500000.00, '中度监控',
   '{"priceChange":{"enabled":true,"risePercent":30,"fallPercent":20},"holders":{"enabled":true,"increasePercent":20,"decreasePercent":15},"volume":{"enabled":true,"threshold":10000}}',
   'telegram', 'any', 1, '1', '适用于市值50万-100万的成长期Token'),
   
  -- 基础市值配置：轻度监控
  ('sol', 300000.00, '轻度监控',
   '{"priceChange":{"enabled":true,"risePercent":50,"fallPercent":30},"holders":{"enabled":true,"increasePercent":30,"decreasePercent":20},"volume":{"enabled":true,"threshold":5000}}',
   '', 'any', 0, '1', '适用于市值30万-50万的早期Token');

-- BSC链默认配置
INSERT INTO `quick_monitor_template` 
  (`chain_type`, `min_market_cap`, `config_name`, `events_config`, `notify_methods`, `trigger_logic`, `sort_order`, `status`, `remark`) 
VALUES
  ('bsc', 1000000.00, 'BSC严格监控', 
   '{"priceChange":{"enabled":true,"risePercent":20,"fallPercent":15},"holders":{"enabled":true,"increasePercent":15,"decreasePercent":10},"volume":{"enabled":true,"threshold":20000}}',
   'telegram,wechat', 'any', 2, '1', '适用于BSC链高市值Token'),
   
  ('bsc', 300000.00, 'BSC轻度监控',
   '{"priceChange":{"enabled":true,"risePercent":50,"fallPercent":30},"holders":{"enabled":true,"increasePercent":30,"decreasePercent":20},"volume":{"enabled":true,"threshold":5000}}',
   '', 'any', 0, '1', '适用于BSC链基础市值Token');

