-- ✅ 正确写法（移除了所有字段名/表名的单引号）
DROP TABLE IF EXISTS member;
CREATE TABLE member (
                        id BIGINT NOT NULL COMMENT 'id',
                        mobile VARCHAR(11) COMMENT '手机号',
                        PRIMARY KEY (id),                  -- 去除了单引号
                        UNIQUE KEY mobile_unique (mobile)  -- 去除了单引号
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员';
