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
                             chain_type   VARCHAR(20)  DEFAULT NULL            COMMENT '链类型',
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
                                  first_query_group_id  VARCHAR(64)  NOT NULL                COMMENT '首次查询群组ID',
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
                                  PRIMARY KEY (id),
                                  INDEX idx_ca_id (ca_id),
                                  INDEX idx_max_multiple (max_multiple)
) ENGINE=InnoDB AUTO_INCREMENT=200 COMMENT='代币记录表';

-- ----------------------------
-- 3、CA查询记录表
-- ----------------------------
DROP TABLE IF EXISTS crypto_ca_query_record;
CREATE TABLE crypto_ca_query_record (
                                        id                    BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
                                        ca_id                 BIGINT        NOT NULL                COMMENT '关联代币id',
                                        user_id               VARCHAR(64)   NOT NULL                COMMENT '查询用户ID',
                                        group_id              VARCHAR(64)   NOT NULL                COMMENT '查询群组ID',
                                        query_time            DATETIME                              COMMENT '查询时间',
                                        market_cap_at_query   DECIMAL(30,8) DEFAULT NULL            COMMENT '查询时市值',
                                        price_at_query        DECIMAL(30,8) DEFAULT NULL            COMMENT '查询时价格',
                                        multiple_from_first   DECIMAL(15,6) DEFAULT NULL            COMMENT '与首次查询的倍数',
                                        del_flag              CHAR(1)       DEFAULT '0'             COMMENT '删除标志（0代表存在 2代表删除）',
                                        create_by             VARCHAR(64)   DEFAULT ''              COMMENT '创建者',
                                        create_time           DATETIME                              COMMENT '创建时间',
                                        update_by             VARCHAR(64)   DEFAULT ''              COMMENT '更新者',
                                        update_time           DATETIME                              COMMENT '更新时间',
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
                                         UNIQUE INDEX idx_group_id (group_id),
                                         UNIQUE idx_win_rate (win_rate)
) ENGINE=InnoDB AUTO_INCREMENT=200 COMMENT='群组CA统计表';

-- ----------------------------
-- 5、监控配置表
-- ----------------------------
DROP TABLE IF EXISTS crypto_monitor_config;
CREATE TABLE crypto_monitor_config (
                                       id                     BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
                                       coin_id                BIGINT        NOT NULL                COMMENT '代币ID',
                                       user_id                VARCHAR(64)   NOT NULL                COMMENT '用户ID',
                                       min_price              DECIMAL(30,8) DEFAULT 0               COMMENT '最低价格监控',
                                       max_price              DECIMAL(30,8) DEFAULT 0               COMMENT '最高价格监控',
                                       percent_change         DECIMAL(5,2)  DEFAULT 0               COMMENT '波动百分比',
                                       notification_type      TINYINT       DEFAULT 0               COMMENT '通知类型',
                                       is_active              TINYINT(1)    DEFAULT 0               COMMENT '是否激活',
                                       notification_target    VARCHAR(64)   DEFAULT ''              COMMENT '通知目标(群ID或用户ID)',
                                       last_notification_time DATETIME                              COMMENT '上次通知时间',
                                       del_flag               CHAR(1)       DEFAULT '0'             COMMENT '删除标志（0代表存在 2代表删除）',
                                       create_by              VARCHAR(64)   DEFAULT ''              COMMENT '创建者',
                                       create_time            DATETIME                              COMMENT '创建时间',
                                       update_by              VARCHAR(64)   DEFAULT ''              COMMENT '更新者',
                                       update_time            DATETIME                              COMMENT '更新时间',
                                       PRIMARY KEY (id),
                                       INDEX idx_coin_id (coin_id),
                                       INDEX idx_user_id (user_id),
                                       INDEX idx_is_active (is_active)
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