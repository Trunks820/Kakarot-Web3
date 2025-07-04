-- ----------------------------
-- 1、CA基础信息表
-- ----------------------------
DROP TABLE IF EXISTS crypto_coin;
CREATE TABLE crypto_coin (
                             coin_id      BIGINT       NOT NULL AUTO_INCREMENT COMMENT '代币id',
                             address      VARCHAR(100) DEFAULT ''              COMMENT '合约地址',
                             symbol       VARCHAR(30)  DEFAULT ''              COMMENT '代币符号',
                             name         VARCHAR(50)  DEFAULT ''              COMMENT '代币名称',
                             logo_url     VARCHAR(255) DEFAULT ''              COMMENT '代币图标',
                             chain_type   VARCHAR(20)  DEFAULT ''              COMMENT '链类型',
                             description  VARCHAR(500) DEFAULT NULL            COMMENT '描述',
                             del_flag     CHAR(1)      DEFAULT '0'             COMMENT '删除标志（0代表存在 2代表删除）',
                             create_by    VARCHAR(64)  DEFAULT ''              COMMENT '创建者',
                             create_time  DATETIME                             COMMENT '创建时间',
                             update_by    VARCHAR(64)  DEFAULT ''              COMMENT '更新者',
                             update_time  DATETIME                             COMMENT '更新时间',
                             PRIMARY KEY (coin_id),
                             INDEX idx_address (address),
                             INDEX idx_symbol (symbol),
                             INDEX idx_name (name)
) ENGINE=InnoDB AUTO_INCREMENT=200 COMMENT='代币表';

-- ----------------------------
-- 2、CA记录表
-- ----------------------------
DROP TABLE IF EXISTS crypto_ca_record;
CREATE TABLE crypto_ca_record (
                                  id                    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
                                  ca_id                 BIGINT       NOT NULL                COMMENT '关联代币id',
                                  first_query_user_id   VARCHAR(64)  NOT NULL                COMMENT '首次查询用户ID',
                                  first_query_user_name VARCHAR(100) DEFAULT NULL            COMMENT '首次查询用户名称',
                                  first_query_group_id  VARCHAR(64)  NOT NULL                COMMENT '首次查询群组ID',
                                  first_query_group_name VARCHAR(100) DEFAULT NULL           COMMENT '首次查询群组名称',
                                  first_query_time      DATETIME                             COMMENT '首次查询时间',
                                  first_market_cap      DECIMAL(30,8) NOT NULL               COMMENT '首次查询时市值',
                                  first_price           DECIMAL(30,8) NOT NULL               COMMENT '首次查询时价格',
                                  highest_market_cap    DECIMAL(30,8) NOT NULL               COMMENT '历史最高市值',
                                  highest_price         DECIMAL(30,8) DEFAULT 0              COMMENT '历史最高价格',
                                  highest_time          DATETIME                             COMMENT '达到最高价格时间',
                                  max_multiple          DECIMAL(15,6) DEFAULT 0              COMMENT '最大倍数（最高市值/首次查询市值）',
                                  is_successful         TINYINT(1)    DEFAULT 0              COMMENT '是否成功（涨幅是否超过50%）',
                                  query_count           BIGINT        DEFAULT 0              COMMENT '查询次数',
                                  del_flag              CHAR(1)       DEFAULT '0'            COMMENT '删除标志（0代表存在 2代表删除）',
                                  create_by             VARCHAR(64)   DEFAULT ''             COMMENT '创建者',
                                  create_time           DATETIME                             COMMENT '创建时间',
                                  update_by             VARCHAR(64)   DEFAULT ''             COMMENT '更新者',
                                  update_time           DATETIME                             COMMENT '更新时间',
                                  chain_type            VARCHAR(20)   DEFAULT 'SOL'          COMMENT '链类型',
                                  PRIMARY KEY (id),
                                  INDEX idx_ca_id (ca_id)
) ENGINE=InnoDB AUTO_INCREMENT=200 COMMENT='代币记录表';

-- ----------------------------
-- 3、CA查询记录表
-- ----------------------------
DROP TABLE IF EXISTS crypto_ca_query_record;
CREATE TABLE crypto_ca_query_record (
                                        id                    BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
                                        ca_id                 BIGINT        NOT NULL                COMMENT '关联代币id',
                                        user_id               VARCHAR(64)   NOT NULL                COMMENT '查询用户ID',
                                        user_name             VARCHAR(64)   NOT NULL                COMMENT '查询用户名',
                                        group_id              VARCHAR(64)   NOT NULL                COMMENT '查询群组ID',
                                        group_name            VARCHAR(64)   NOT NULL                COMMENT '查询群组名',
                                        query_time            DATETIME                              COMMENT '查询时间',
                                        market_cap_at_query   DECIMAL(30,8) DEFAULT NULL            COMMENT '查询时市值',
                                        price_at_query        DECIMAL(30,8) DEFAULT NULL            COMMENT '查询时价格',
                                        multiple_from_first   DECIMAL(15,6) DEFAULT NULL            COMMENT '与首次查询的倍数',
                                        del_flag              CHAR(1)       DEFAULT '0'             COMMENT '删除标志（0代表存在 2代表删除）',
                                        create_by             VARCHAR(64)   DEFAULT ''              COMMENT '创建者',
                                        create_time           DATETIME                              COMMENT '创建时间',
                                        update_by             VARCHAR(64)   DEFAULT ''              COMMENT '更新者',
                                        update_time           DATETIME                              COMMENT '更新时间',
                                        chain_type            VARCHAR(20)   DEFAULT 'SOL'           COMMENT '链类型',
                                        PRIMARY KEY (id),
                                        INDEX idx_ca_id (ca_id),
                                        INDEX idx_group_id (group_id)
) ENGINE=InnoDB AUTO_INCREMENT=200 COMMENT='代币查询记录表';

-- ----------------------------
-- 4、群组CA统计表
-- ----------------------------
DROP TABLE IF EXISTS crypto_group_statistics;
CREATE TABLE crypto_group_statistics (
                                         id                    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
                                         group_id              VARCHAR(64)  NOT NULL                COMMENT '群组ID',
                                         total_ca_queries      BIGINT       DEFAULT 0               COMMENT '总CA查询次数',
                                         unique_ca_count       BIGINT       DEFAULT 0               COMMENT 'ca数量',
                                         successful_ca_count   BIGINT       DEFAULT 0               COMMENT '成功CA数量（涨幅超过50%）',
                                         win_rate              DECIMAL(5,2) DEFAULT 0               COMMENT '胜率(成功CA数/不同CA数)',
                                         del_flag              CHAR(1)      DEFAULT '0'             COMMENT '删除标志（0代表存在 2代表删除）',
                                         create_by             VARCHAR(64)  DEFAULT ''              COMMENT '创建者',
                                         create_time           DATETIME                             COMMENT '创建时间',
                                         update_by             VARCHAR(64)  DEFAULT ''              COMMENT '更新者',
                                         update_time           DATETIME                             COMMENT '更新时间',
                                         PRIMARY KEY (id),
                                         INDEX idx_group_id (group_id),
                                         INDEX idx_win_rate (win_rate)
) ENGINE=InnoDB AUTO_INCREMENT=200 COMMENT='群组CA统计表';

-- ----------------------------
-- 5、监控配置表
-- ----------------------------
DROP TABLE IF EXISTS crypto_monitor_config;
CREATE TABLE crypto_monitor_config (
                                       id                     BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
                                       coin_address           VARCHAR(255)  NOT NULL                COMMENT '代币地址(SOL/EVM通用,通过地址格式自动识别链类型)',
                                       token_name             VARCHAR(100)  DEFAULT ''              COMMENT '代币名称(保存时通过API查询获取,相对稳定)',
                                       alert_mode             VARCHAR(20)   NOT NULL                COMMENT '提醒模式(timer:定时提醒, condition:价格触发, event:事件监控)',
                                       event_type             VARCHAR(30)   DEFAULT NULL            COMMENT '事件类型(largeTransaction:大额交易监控, holdingChange:持仓异动监控)',
                                       timer_interval         INT           DEFAULT NULL            COMMENT '定时提醒间隔(分钟)',
                                       condition_type         VARCHAR(30)   DEFAULT NULL            COMMENT '条件类型(priceAbove:价格高于, priceBelow:价格低于, marketCapBelow:市值低于, changeExceeds:涨跌幅超过)',
                                       condition_value        DECIMAL(30,8) DEFAULT NULL            COMMENT '条件阈值(价格单位USD,涨跌幅为百分比,市值已转换为实际数值)',
                                       notify_methods         VARCHAR(100)  NOT NULL                COMMENT '通知方式(wechat,telegram)',
                                       wechat_name            VARCHAR(100)  DEFAULT ''              COMMENT '微信名称(支持个人和群聊)',
                                       telegram_name          VARCHAR(100)  DEFAULT ''              COMMENT 'Telegram名称(支持个人和群组)',
                                       remark                 VARCHAR(500)  DEFAULT ''              COMMENT '备注',
                                       last_notification_time DATETIME                              COMMENT '上次通知时间',
                                       status                 CHAR(1)       DEFAULT '1'             COMMENT '状态(0:停用, 1:启用)',
                                       del_flag               CHAR(1)       DEFAULT '0'             COMMENT '删除标志（0代表存在 2代表删除）',
                                       create_by              VARCHAR(64)   DEFAULT ''              COMMENT '创建者',
                                       create_time            DATETIME                              COMMENT '创建时间',
                                       update_by              VARCHAR(64)   DEFAULT ''              COMMENT '更新者',
                                       update_time            DATETIME                              COMMENT '更新时间',
                                       PRIMARY KEY (id),
                                       INDEX idx_coin_address (coin_address),
                                       INDEX idx_create_by (create_by),
                                       INDEX idx_alert_mode (alert_mode),
                                       INDEX idx_event_type (event_type),
                                       INDEX idx_status (status)
) ENGINE=InnoDB AUTO_INCREMENT=200 COMMENT='监控配置表';

-- ----------------------------
-- 6、机器人群组表
-- ----------------------------
DROP TABLE IF EXISTS crypto_bot_group;
CREATE TABLE crypto_bot_group (
                                  id                    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
                                  platform              VARCHAR(20)  NOT NULL                COMMENT '平台(wechat/telegram)',
                                  group_id              VARCHAR(64)  NOT NULL                COMMENT '群组ID',
                                  group_name            VARCHAR(100) NOT NULL                COMMENT '群组名称',
                                  win_threshold         DECIMAL(5,2) DEFAULT 0.5             COMMENT '胜率计算阈值(默认0.5)',
                                  is_active             TINYINT(1)   DEFAULT 1               COMMENT '是否激活',
                                  del_flag              CHAR(1)      DEFAULT '0'             COMMENT '删除标志（0代表存在 2代表删除）',
                                  create_by             VARCHAR(64)  DEFAULT ''              COMMENT '创建者',
                                  create_time           DATETIME                             COMMENT '创建时间',
                                  update_by             VARCHAR(64)  DEFAULT ''              COMMENT '更新者',
                                  update_time           DATETIME                             COMMENT '更新时间',
                                  PRIMARY KEY (id),
                                  UNIQUE INDEX idx_platform_group_id (platform, group_id)
) ENGINE=InnoDB AUTO_INCREMENT=200 COMMENT='机器人群组表';
-- ----------------------------
-- 7、钱包表
-- ----------------------------
DROP TABLE IF EXISTS crypto_wallet;
CREATE TABLE crypto_wallet (
    id                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    wallet_address    VARCHAR(255) NOT NULL                COMMENT '钱包地址',
    wallet_name       VARCHAR(100) DEFAULT NULL            COMMENT '钱包备注',
    last_active_time  DATETIME                             COMMENT '最后活跃时间',
    monitor_state     TINYINT(1)   DEFAULT 1               COMMENT '监控状态(0:禁用,1:启用)',
    del_flag          CHAR(1)      DEFAULT '0'             COMMENT '删除标志（0代表存在 2代表删除）',
    create_by         VARCHAR(64)  DEFAULT ''              COMMENT '创建者',
    create_time       DATETIME                             COMMENT '创建时间',
    update_by         VARCHAR(64)  DEFAULT ''              COMMENT '更新者',
    update_time       DATETIME                             COMMENT '更新时间',
    chain_type        VARCHAR(20)  DEFAULT 'SOL'           COMMENT '链类型',
    PRIMARY KEY (wallet_address)
) ENGINE=InnoDB AUTO_INCREMENT=200 COMMENT='钱包表';

-- ----------------------------
-- 8、钱包交易记录表
-- ----------------------------
DROP TABLE IF EXISTS crypto_wallet_transactions;
CREATE TABLE crypto_wallet_transactions (
    id                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    wallet_address    VARCHAR(255) NOT NULL                COMMENT '钱包地址',
    coin_address      VARCHAR(255) NOT NULL                COMMENT '代币地址',
    operation_type    VARCHAR(20)  NOT NULL                COMMENT '操作类型(买入/卖出/清仓)',
    transaction_amount DECIMAL(30,8) NOT NULL              COMMENT '交易金额',
    chain_type        VARCHAR(20)  DEFAULT ''              COMMENT '货币类型',
    price             DECIMAL(30,8) DEFAULT NULL           COMMENT '价格',
    market            DECIMAL(30,2) DEFAULT NULL           COMMENT '市值',
    safe_info         VARCHAR(500) DEFAULT NULL            COMMENT '安全评估信息',
    transaction_time  DATETIME     NOT NULL                COMMENT '交易时间戳',
    del_flag          CHAR(1)      DEFAULT '0'             COMMENT '删除标志（0代表存在 2代表删除）',
    create_by         VARCHAR(64)  DEFAULT ''              COMMENT '创建者',
    create_time       DATETIME                             COMMENT '创建时间',
    update_by         VARCHAR(64)  DEFAULT ''              COMMENT '更新者',
    update_time       DATETIME                             COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_wallet_address (wallet_address),
    INDEX idx_coin_address (coin_address),
    INDEX idx_transaction_time (transaction_time)
) ENGINE=InnoDB AUTO_INCREMENT=200 COMMENT='钱包交易记录表';

-- ----------------------------
-- 9、价格历史表
-- ----------------------------
DROP TABLE IF EXISTS crypto_price_history;
CREATE TABLE crypto_price_history (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    coin_address    VARCHAR(255) NOT NULL                COMMENT '代币地址',
    price           DECIMAL(30,8) NOT NULL               COMMENT '价格',
    market          DECIMAL(30,2) DEFAULT NULL           COMMENT '市值',
    recording_time  DATETIME     NOT NULL                COMMENT '记录时间',
    data_source     VARCHAR(50)  DEFAULT NULL            COMMENT '数据来源',
    del_flag        CHAR(1)      DEFAULT '0'             COMMENT '删除标志（0代表存在 2代表删除）',
    create_by       VARCHAR(64)  DEFAULT ''              COMMENT '创建者',
    create_time     DATETIME                             COMMENT '创建时间',
    update_by       VARCHAR(64)  DEFAULT ''              COMMENT '更新者',
    update_time     DATETIME                             COMMENT '更新时间',
    chain_type      VARCHAR(20)  DEFAULT 'SOL'           COMMENT '链类型',
    PRIMARY KEY (id),
    INDEX idx_coin_address (coin_address),
    INDEX idx_recording_time (recording_time)
) ENGINE=InnoDB AUTO_INCREMENT=200 COMMENT='价格历史表';

-- ----------------------------
-- 测试数据
-- ----------------------------

-- 1. 插入CA基础信息
INSERT INTO crypto_coin
(address, symbol, name, create_time, chain_type, description)
VALUES
    ('EsDZUf7cDUU7FSPjePkksaJ8TzvC9QY5YRqeuiy5pump', 'Mitsuki', 'Doge\'s Original Name', NOW(), 'SOL', '我们都知道Doge的名字叫Kabosu'),
('So11111111111111111111111111111111111111112', 'SOL', 'Solana', NOW(), 'SOL', ''),
('0x5417994ae69e6ccb283bb6dbdba3006b3d3f9f95', 'ETHCHAN', 'ETH Chan', NOW(), 'ETH', ''),
('0x945cd29a40629ada610c2f6eba3f393756aa4444', 'USD1DOGE', 'USD1DOGE', NOW(), 'BSC', '');

-- 2. 插入CA记录
INSERT INTO crypto_ca_record
(ca_id, first_query_user_id, first_query_user_name, first_query_group_id, first_query_group_name,
first_query_time, first_market_cap, first_price, highest_market_cap, highest_price,
highest_time, max_multiple, is_successful, query_count, create_time, chain_type)
VALUES
(1, '1001', '用户A', '2001', '群组A', DATE_SUB(NOW(), INTERVAL 7 DAY), 1000000.00, 1.00000000,
2000000.00, 2.00000000, DATE_SUB(NOW(), INTERVAL 3 DAY), 2.00, 1, 50, NOW(), 'SOL'),
(2, '1002', '用户B', '2002', '群组B', DATE_SUB(NOW(), INTERVAL 5 DAY), 2000000.00, 100.00000000,
3000000.00, 150.00000000, DATE_SUB(NOW(), INTERVAL 2 DAY), 1.50, 1, 30, NOW(), 'SOL');

-- 5. 插入监控配置
INSERT INTO crypto_monitor_config
(coin_address, token_name, alert_mode, event_type, timer_interval, 
condition_type, condition_value, notify_methods, wechat_name, telegram_name, remark,
status, last_notification_time, create_by, create_time)
VALUES
('EsDZUf7cDUU7FSPjePkksaJ8TzvC9QY5YRqeuiy5pump', 'Doge\'s Original Name', 'timer', NULL, 15, 
NULL, NULL, 'telegram', '', 'user1001', '定时监控Mitsuki代币', '1', DATE_SUB(NOW(), INTERVAL 1 HOUR), 'admin', NOW()),
('So11111111111111111111111111111111111111112', 'Solana', 'condition', NULL, NULL, 
'priceAbove', 120.00000000, 'wechat,telegram', '微信用户1002', 'tg_user1002', 'SOL价格突破120时预警', '1', DATE_SUB(NOW(), INTERVAL 2 HOUR), 'admin', NOW()),
('EsDZUf7cDUU7FSPjePkksaJ8TzvC9QY5YRqeuiy5pump', 'Doge\'s Original Name', 'condition', NULL, NULL, 
'changeExceeds', 10.00000000, 'telegram', '', 'user1003', 'Mitsuki涨跌幅超过10%预警', '1', NULL, 'admin', NOW()),
('EsDZUf7cDUU7FSPjePkksaJ8TzvC9QY5YRqeuiy5pump', 'Doge\'s Original Name', 'event', 'largeTransaction', NULL, 
NULL, 50000.00000000, 'wechat,telegram', '微信用户1004', 'tg_user1004', '大额交易监控(≥5万美元)', '1', NULL, 'admin', NOW());

-- 6. 插入机器人群组
INSERT INTO crypto_bot_group
(platform, group_id, group_name, win_threshold, is_active, create_time)
VALUES
('telegram', '2001', 'TG群组A', 0.50, 1, NOW()),
('wechat', '2002', '微信群组A', 0.50, 1, NOW());


-- 7. 插入钱包数据
INSERT INTO crypto_wallet 
(wallet_address, wallet_name, last_active_time,
monitor_state, create_time, chain_type) 
VALUES
('7xKXtg2CW87d97TXJSDpbD5jBkheTqA83TZRuJosgAsU', 'Smart Money 1',  NOW(), 1, NOW(), 'SOL'),
('9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM', 'Smart Money 2',  NOW(), 1, NOW(), 'SOL'),
('5Q544fKrFoe6tsEbD7S8EmxGTJYAKtTVhAW5Q5pge4j1', 'Smart Money 3',  NOW(), 1, NOW(), 'SOL');

-- 8. 插入钱包交易记录
INSERT INTO crypto_wallet_transactions 
(wallet_address, coin_address, operation_type, transaction_amount, chain_type, 
price, market, safe_info, transaction_time, create_time) 
VALUES
('7xKXtg2CW87d97TXJSDpbD5jBkheTqA83TZRuJosgAsU', 'EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v', '买入', 1000.00000000, 'SOL', 1.00000000, 1000000.00, '安全', DATE_SUB(NOW(), INTERVAL 1 DAY), NOW()),
('7xKXtg2CW87d97TXJSDpbD5jBkheTqA83TZRuJosgAsU', 'EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v', '卖出', 500.00000000, 'SOL', 1.20000000, 1200000.00, '安全', DATE_SUB(NOW(), INTERVAL 12 HOUR), NOW()),
('9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM', 'EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v', '买入', 2000.00000000, 'SOL', 1.10000000, 1100000.00, '安全', DATE_SUB(NOW(), INTERVAL 6 HOUR), NOW());

-- 9. 插入价格历史数据
INSERT INTO crypto_price_history 
(coin_address, price, market, recording_time, data_source, create_time, chain_type) 
VALUES
('EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v', 1.00000000, 1000000.00, DATE_SUB(NOW(), INTERVAL 1 DAY), 'API', NOW(), 'SOL'),
('EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v', 1.10000000, 1100000.00, DATE_SUB(NOW(), INTERVAL 12 HOUR), 'API', NOW(), 'SOL'),
('EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v', 1.20000000, 1200000.00, DATE_SUB(NOW(), INTERVAL 6 HOUR), 'API', NOW(), 'SOL'),
('So11111111111111111111111111111111111111112', 100.00000000, 100000000.00, DATE_SUB(NOW(), INTERVAL 1 DAY), 'API', NOW(), 'SOL'),
('So11111111111111111111111111111111111111112', 105.00000000, 105000000.00, DATE_SUB(NOW(), INTERVAL 12 HOUR), 'API', NOW(), 'SOL'),
('So11111111111111111111111111111111111111112', 110.00000000, 110000000.00, DATE_SUB(NOW(), INTERVAL 6 HOUR), 'API', NOW(), 'SOL');


