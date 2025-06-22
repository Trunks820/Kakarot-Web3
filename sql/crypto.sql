
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


