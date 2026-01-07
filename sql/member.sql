-- ✅ 正确写法（移除了所有字段名/表名的单引号）
DROP TABLE IF EXISTS member;
CREATE TABLE member (
                        id BIGINT NOT NULL COMMENT 'id',
                        mobile VARCHAR(11) COMMENT '手机号',
                        PRIMARY KEY (id),                  -- 去除了单引号
                        UNIQUE KEY mobile_unique (mobile)  -- 去除了单引号
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员';

drop table if exists passenger;
CREATE TABLE passenger  (
    id bigint not null comment 'id',
    member_id bigint not null comment '会员id',
    name varchar(20) not null comment '姓名',
    id_card varchar(18) not null comment '身份证',
    type char(1) not null comment '旅客类型|枚举[PassangerTypeEnum]',
    create_time datetime(3) comment '新增时间',
    update_time datetime(3) comment '修改时间',
    primary key (id),
    INDEX member_id_index (member_id)
)engine=innodb default charset=utf8mb4 COMMENT='乘车人';
