SET NAMES utf8mb4;

CREATE database if NOT EXISTS `s-pay-mall` default character set utf8mb4;
use `s-pay-mall`;

# 转储表 pay_order
# ------------------------------------------------------------

DROP TABLE IF EXISTS `pay_order`;

CREATE TABLE `pay_order`
(
    `id`           int unsigned                      NOT NULL AUTO_INCREMENT COMMENT '自增ID',
    `user_id`      varchar(32) CHARACTER SET utf8mb4 NOT NULL COMMENT '用户ID',
    `product_id`   varchar(16)                       NOT NULL COMMENT '商品ID',
    `product_name` varchar(64)                       NOT NULL COMMENT '商品名称',
    `order_id`     varchar(16) CHARACTER SET utf8mb4 NOT NULL COMMENT '订单ID',
    `order_time`   datetime                          NOT NULL COMMENT '下单时间',
    `total_amount` decimal(8, 2) unsigned                     DEFAULT NULL COMMENT '订单金额',
    `status`       varchar(32) CHARACTER SET utf8mb4 NOT NULL COMMENT '订单状态；create-创建完成、pay_wait-等待支付、pay_success-支付成功、deal_done-交易完成、close-订单关单',
    `pay_url`      varchar(2014) CHARACTER SET utf8mb4        DEFAULT NULL COMMENT '支付信息',
    `pay_time`     datetime                                   DEFAULT NULL COMMENT '支付时间',
    `create_time`  datetime                          NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  datetime                          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_order_id` (`order_id`),
    KEY `idx_user_id_product_id` (`user_id`, `product_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 5
  DEFAULT CHARSET = utf8mb4;

