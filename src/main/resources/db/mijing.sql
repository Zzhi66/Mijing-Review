-- 创建数据库
CREATE DATABASE IF NOT EXISTS `mijing` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `mijing`;

-- 用户表
DROP TABLE IF EXISTS `tb_user`;
CREATE TABLE `tb_user` (
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `phone`       VARCHAR(11)     NOT NULL COMMENT '手机号码',
    `password`    VARCHAR(128)    DEFAULT '' COMMENT '密码，加密存储',
    `nick_name`   VARCHAR(32)     DEFAULT '觅境用户' COMMENT '昵称，默认为随机字符',
    `icon`        VARCHAR(255)    DEFAULT '' COMMENT '头像',
    `create_time` TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 用户详情表
DROP TABLE IF EXISTS `tb_user_info`;
CREATE TABLE `tb_user_info` (
    `user_id`     BIGINT UNSIGNED NOT NULL COMMENT '用户id',
    `city`        VARCHAR(64)     DEFAULT '' COMMENT '所在城市',
    `introduce`   VARCHAR(512)    DEFAULT NULL COMMENT '个人介绍',
    `fans`        INT UNSIGNED    DEFAULT '0' COMMENT '粉丝数量',
    `followee`    INT UNSIGNED    DEFAULT '0' COMMENT '关注的人的数量',
    `gender`      TINYINT UNSIGNED DEFAULT '0' COMMENT '性别，0：男，1：女',
    `birthday`    DATE            DEFAULT NULL COMMENT '生日',
    `credits`     INT UNSIGNED    DEFAULT NULL COMMENT '积分',
    `level`       TINYINT UNSIGNED DEFAULT '0' COMMENT '等级，0-5',
    `create_time` TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户详情表';

-- 商铺类型表
DROP TABLE IF EXISTS `tb_shop_type`;
CREATE TABLE `tb_shop_type` (
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '类型ID',
    `name`        VARCHAR(32)     NOT NULL COMMENT '类型名称',
    `icon`        VARCHAR(255)    DEFAULT NULL COMMENT '图标',
    `sort`        INT UNSIGNED    NOT NULL DEFAULT '0' COMMENT '顺序',
    `create_time` TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商铺类型表';

-- 商铺表
DROP TABLE IF EXISTS `tb_shop`;
CREATE TABLE `tb_shop` (
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '商铺ID',
    `name`        VARCHAR(128)    NOT NULL COMMENT '商铺名称',
    `type_id`     BIGINT UNSIGNED NOT NULL COMMENT '商铺类型 id',
    `images`      VARCHAR(1024)   NOT NULL COMMENT '商铺图片，多个逗号分割',
    `province`    VARCHAR(10)     DEFAULT NULL COMMENT '省份名称',
    `city`        VARCHAR(10)     DEFAULT NULL COMMENT '城市名称',
    `district`    VARCHAR(10)     DEFAULT NULL COMMENT '区县名称',
    `address`     VARCHAR(255)    NOT NULL COMMENT '地址',
    `x`           DOUBLE(9,6)     NOT NULL COMMENT '经度',
    `y`           DOUBLE(9,6)     NOT NULL COMMENT '纬度',
    `avg_price`   BIGINT UNSIGNED DEFAULT NULL COMMENT '均价（元）',
    `sold`        INT UNSIGNED    DEFAULT '0' COMMENT '已售数量',
    `comments`    INT UNSIGNED    DEFAULT '0' COMMENT '评论数量',
    `score`       TINYINT UNSIGNED DEFAULT '5' COMMENT '评价得分，1~5分',
    `open_hours`  VARCHAR(32)     DEFAULT NULL COMMENT '营业时间，例如10:00-22:00',
    `create_time` TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商铺信息表';

-- 探店笔记表
DROP TABLE IF EXISTS `tb_blog`;
CREATE TABLE `tb_blog` (
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '笔记ID',
    `shop_id`     BIGINT UNSIGNED NOT NULL COMMENT '商铺id',
    `user_id`     BIGINT UNSIGNED NOT NULL COMMENT '用户id',
    `title`       VARCHAR(255)    NOT NULL COMMENT '标题',
    `images`      VARCHAR(2048)   NOT NULL COMMENT '探店图片，最多9张',
    `content`     VARCHAR(2048)   NOT NULL COMMENT '探店的文字描述',
    `liked`       INT UNSIGNED    DEFAULT '0' COMMENT '点赞数量',
    `comments`    INT UNSIGNED    DEFAULT NULL COMMENT '评论数量',
    `create_time` TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='探店笔记表';

-- 关注表
DROP TABLE IF EXISTS `tb_follow`;
CREATE TABLE `tb_follow` (
    `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`         BIGINT UNSIGNED NOT NULL COMMENT '用户id',
    `follow_user_id`  BIGINT UNSIGNED NOT NULL COMMENT '关联的达人id',
    `create_time`     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_follow` (`user_id`, `follow_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户关注表';

-- 优惠券表
DROP TABLE IF EXISTS `tb_voucher`;
CREATE TABLE `tb_voucher` (
    `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `shop_id`       BIGINT UNSIGNED DEFAULT NULL COMMENT '商铺id',
    `title`         VARCHAR(255)    NOT NULL COMMENT '代金券标题',
    `sub_title`     VARCHAR(512)    DEFAULT NULL COMMENT '副标题',
    `rules`         VARCHAR(1024)   DEFAULT NULL COMMENT '使用规则',
    `pay_value`     BIGINT UNSIGNED NOT NULL COMMENT '抵扣金额（分）',
    `actual_value`  BIGINT NOT NULL COMMENT '实际购买价格（分）',
    `type`          TINYINT UNSIGNED NOT NULL DEFAULT '0' COMMENT '0,普通券；1,秒杀券',
    `status`        TINYINT UNSIGNED NOT NULL DEFAULT '1' COMMENT '1,上架；2,下架；3,过期',
    `create_time`   TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`   TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券的基本信息';

-- 秒杀券表（优惠券扩展）
DROP TABLE IF EXISTS `tb_seckill_voucher`;
CREATE TABLE `tb_seckill_voucher` (
    `voucher_id`  BIGINT UNSIGNED NOT NULL COMMENT '关联的优惠券的id',
    `stock`       INT NOT NULL COMMENT '库存',
    `create_time` TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `begin_time`  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开始抢购时间',
    `end_time`    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '结束抢购时间',
    `update_time` TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`voucher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀活动的优惠券表';

-- 优惠券订单表（含乐观锁version字段）
DROP TABLE IF EXISTS `tb_voucher_order`;
CREATE TABLE `tb_voucher_order` (
    `id`         BIGINT NOT NULL COMMENT '订单id（全局唯一ID）',
    `user_id`    BIGINT UNSIGNED NOT NULL COMMENT '下单的用户id',
    `voucher_id` BIGINT UNSIGNED NOT NULL COMMENT '购买的代金券id',
    `pay_type`   TINYINT UNSIGNED NOT NULL DEFAULT '1' COMMENT '支付方式 1余额支付；2第三方支付',
    `status`     TINYINT UNSIGNED NOT NULL DEFAULT '1' COMMENT '订单状态：1待支付；2已支付；3已核销；4已取消；5退款中；6已退款',
    `version`    INT UNSIGNED NOT NULL DEFAULT '0' COMMENT '乐观锁版本号',
    `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
    `pay_time`   TIMESTAMP DEFAULT NULL COMMENT '支付时间',
    `use_time`   TIMESTAMP DEFAULT NULL COMMENT '核销时间',
    `refund_time` TIMESTAMP DEFAULT NULL COMMENT '退款时间',
    `update_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券的订单表';

-- =========== 初始化数据 ===========

-- 商铺类型
INSERT INTO `tb_shop_type`(`id`, `name`, `icon`, `sort`) VALUES
(1,  '美食',   '/imgs/shoptype/美食.png',   1),
(2,  '酒店',   '/imgs/shoptype/酒店.png',   2),
(3,  '娱乐',   '/imgs/shoptype/娱乐.png',   3),
(4,  '美容美发','/imgs/shoptype/美容美发.png',4),
(5,  '名胜古迹','/imgs/shoptype/名胜古迹.png',5),
(6,  '购物',   '/imgs/shoptype/购物.png',   6),
(7,  '运动健康','/imgs/shoptype/运动健康.png',7),
(8,  '亲子',   '/imgs/shoptype/亲子.png',   8),
(9,  '休闲',   '/imgs/shoptype/休闲.png',   9),
(10, '医疗',   '/imgs/shoptype/医疗.png',   10);

-- 示例商铺数据（含经纬度，北京地区）
INSERT INTO `tb_shop`(`id`,`name`,`type_id`,`images`,`province`,`city`,`district`,`address`,`x`,`y`,`avg_price`,`sold`,`comments`,`score`,`open_hours`) VALUES
(1,'觅境精品咖啡',1,'/imgs/shops/cafe.jpg','北京市','北京市','朝阳区','朝阳区工体北路10号',116.454407,39.913285,50,500,200,5,'08:00-22:00'),
(2,'山野轻食餐厅',1,'/imgs/shops/restaurant.jpg','北京市','北京市','海淀区','海淀区中关村大街18号',116.307590,39.982380,100,800,350,5,'10:00-21:00'),
(3,'隐溪精品酒店',2,'/imgs/shops/hotel.jpg','北京市','北京市','东城区','东城区王府井大街10号',116.418026,39.918058,500,300,150,4,'00:00-23:59'),
(4,'云端健身会所',7,'/imgs/shops/gym.jpg','北京市','北京市','西城区','西城区长安街88号',116.366794,39.898148,200,600,80,5,'06:00-23:00'),
(5,'原味寿司',1,'/imgs/shops/sushi.jpg','北京市','北京市','朝阳区','朝阳区三里屯路19号',116.455432,39.937009,150,400,120,4,'11:00-22:00');

-- 示例优惠券
INSERT INTO `tb_voucher`(`id`,`shop_id`,`title`,`sub_title`,`rules`,`pay_value`,`actual_value`,`type`,`status`) VALUES
(1,1,'100元代金券','周一至周五可用','无门槛，有效期7天',100,80,0,1),
(2,2,'50元秒杀券','限量20张！','每人限购1张',50,10,1,1);

-- 秒杀券信息
INSERT INTO `tb_seckill_voucher`(`voucher_id`,`stock`,`begin_time`,`end_time`) VALUES
(2,20,'2025-01-01 00:00:00','2026-12-31 23:59:59');
