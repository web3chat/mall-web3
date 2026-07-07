SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for airdrop_info
-- ----------------------------
DROP TABLE IF EXISTS `airdrop_info`;
CREATE TABLE `airdrop_info`
(
    `info_id`     bigint UNSIGNED                                              NOT NULL AUTO_INCREMENT,
    `address`     varchar(42) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商户地址',
    `name`        varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '空投名称',
    `status`      tinyint                                                      NULL DEFAULT 0 COMMENT '状态，0待上架，1已上架',
    `task_status` tinyint(1)                                                   NULL DEFAULT 0 COMMENT '0等待，1库存检查，2分配token，3成功，4失败',
    `tx_hash`     varchar(66) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '空投哈希',
    `tx_note`     text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci        NULL COMMENT '空投备注',
    `start_time`  bigint                                                       NULL DEFAULT NULL COMMENT '空投开始时间',
    `create_time` bigint                                                       NULL DEFAULT NULL COMMENT '空投创建时间',
    PRIMARY KEY (`info_id`) USING BTREE,
    INDEX `idx_a` (`address` ASC) USING BTREE,
    INDEX `idx_s` (`status` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '空投-信息'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of airdrop_info
-- ----------------------------

-- ----------------------------
-- Table structure for airdrop_white
-- ----------------------------
DROP TABLE IF EXISTS `airdrop_white`;
CREATE TABLE `airdrop_white`
(
    `id`            bigint                                                       NOT NULL AUTO_INCREMENT,
    `info_id`       bigint                                                       NOT NULL COMMENT '空投编号',
    `address`       varchar(42) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '用户地址',
    `meta_type`     int                                                          NULL DEFAULT 0 COMMENT '类型，0商品',
    `meta_id`       varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '编号',
    `num`           int                                                          NULL DEFAULT 0 COMMENT '数量',
    `token_id_json` json                                                         NULL,
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_ii` (`info_id` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '空投-白名单'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of airdrop_white
-- ----------------------------

-- ----------------------------
-- Table structure for auth_permission
-- ----------------------------
DROP TABLE IF EXISTS `auth_permission`;
CREATE TABLE `auth_permission`
(
    `id`        int                                                           NOT NULL,
    `parent_id` int                                                           NOT NULL COMMENT '父编号',
    `name`      varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT '' COMMENT '名称',
    `role`      tinyint                                                       NULL DEFAULT 0 COMMENT '角色，0通用，1管理员专属，2商户专属',
    `level`     tinyint                                                       NULL DEFAULT 0 COMMENT '层级',
    `uri`       varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '请求uri',
    PRIMARY KEY (`id`, `parent_id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '授权-权限'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of auth_permission
-- ----------------------------
INSERT INTO `auth_permission`
VALUES (10000000, 0, '商品中心', 0, 0, '');
INSERT INTO `auth_permission`
VALUES (10010000, 10000000, '商品管理', 0, 1, '');
INSERT INTO `auth_permission`
VALUES (10010001, 10010000, '查询商品列表', 0, 2, '/m/goods/page');
INSERT INTO `auth_permission`
VALUES (10010002, 10010000, '查看商品详情', 0, 2, '/m/goods/detail');
INSERT INTO `auth_permission`
VALUES (10010003, 10010000, '商品添加/编辑', 2, 2, '/m/goods/add-update');
INSERT INTO `auth_permission`
VALUES (10010004, 10010000, '商品铸造', 2, 2, '/m/goods/mint');
INSERT INTO `auth_permission`
VALUES (10010005, 10010000, '商品铸造失败重试', 2, 2, '/m/goods/mint/retry');
INSERT INTO `auth_permission`
VALUES (10010006, 10010000, '查看商品铸造记录', 0, 2, '/m/goods/mint/record');
INSERT INTO `auth_permission`
VALUES (10010007, 10010000, '商品上下架', 0, 2, '/m/goods/status');
INSERT INTO `auth_permission`
VALUES (10010008, 10010000, '商品隐藏', 0, 2, '/m/goods/hidden');
INSERT INTO `auth_permission`
VALUES (10010009, 10010000, '商品删除', 0, 2, '/m/goods/delete');
INSERT INTO `auth_permission`
VALUES (10010010, 10010000, '查看商品白名单', 0, 2, '/m/goods/white-list');
INSERT INTO `auth_permission`
VALUES (10010011, 10010000, '上传商品白名单', 2, 2, '/m/goods/add-white-list');
INSERT INTO `auth_permission`
VALUES (10010012, 10010000, '商品推荐', 1, 2, '/m/goods/recommend');
INSERT INTO `auth_permission`
VALUES (20000000, 0, '订单中心', 0, 0, '');
INSERT INTO `auth_permission`
VALUES (20010000, 20000000, '订单管理', 0, 1, '');
INSERT INTO `auth_permission`
VALUES (20010001, 20010000, '查询订单列表', 0, 2, '/m/order/info/page');
INSERT INTO `auth_permission`
VALUES (20010002, 20010000, '查询订单详情', 0, 2, '/m/order/info/detail');
INSERT INTO `auth_permission`
VALUES (20010003, 20010000, '退款审核', 2, 2, '/m/order/info/refund/audit');
INSERT INTO `auth_permission`
VALUES (20020000, 20000000, '提货管理', 0, 1, '');
INSERT INTO `auth_permission`
VALUES (20020001, 20020000, '查询提货列表', 0, 2, '/m/order/express/page');
INSERT INTO `auth_permission`
VALUES (20020002, 20020000, '查询提货详情', 0, 2, '/m/order/express/detail');
INSERT INTO `auth_permission`
VALUES (20020003, 20020000, '发货', 2, 2, '/m/order/express/express');
INSERT INTO `auth_permission`
VALUES (20020004, 20020000, '查询提货物流', 0, 2, '/m/order/express/logistics');
INSERT INTO `auth_permission`
VALUES (20020005, 20020000, '退货审核', 2, 2, '/m/order/express/refund/audit');
INSERT INTO `auth_permission`
VALUES (20020006, 20020000, '退货确认', 2, 2, '/m/order/express/refund/confirm');
INSERT INTO `auth_permission`
VALUES (20020007, 20020000, '导出提货列表', 0, 2, '/m/order/express/excel');
INSERT INTO `auth_permission`
VALUES (30000000, 0, '运营中心', 0, 0, '');
INSERT INTO `auth_permission`
VALUES (30010000, 30000000, '空投管理', 0, 1, '');
INSERT INTO `auth_permission`
VALUES (30010001, 30010000, '查询空投列表', 0, 2, '/m/airdrop/page');
INSERT INTO `auth_permission`
VALUES (30010002, 30010000, '查询空投详情', 0, 2, '/m/airdrop/detail');
INSERT INTO `auth_permission`
VALUES (30010003, 30010000, '空投新建/编辑', 2, 2, '/m/airdrop/add-update');
INSERT INTO `auth_permission`
VALUES (30010004, 30010000, '空投上下架', 0, 2, '/m/airdrop/status');
INSERT INTO `auth_permission`
VALUES (30010005, 30010000, '空投失败重试', 0, 2, '/m/airdrop/retry');
INSERT INTO `auth_permission`
VALUES (30010006, 30010000, '空投删除', 0, 2, '/m/airdrop/delete');
INSERT INTO `auth_permission`
VALUES (30020000, 30000000, '公告管理', 1, 1, '');
INSERT INTO `auth_permission`
VALUES (30020001, 30020000, '查询公告列表', 1, 2, '/m/sys/notice/page');
INSERT INTO `auth_permission`
VALUES (30020002, 30020000, '查询公告详情', 1, 2, '/m/sys/notice/detail');
INSERT INTO `auth_permission`
VALUES (30020003, 30020000, '公告新增/编辑', 1, 2, '/m/sys/notice/add-update');
INSERT INTO `auth_permission`
VALUES (30020004, 30020000, '公告上下架', 1, 2, '/m/sys/notice/status');
INSERT INTO `auth_permission`
VALUES (30020005, 30020000, '公告删除', 1, 2, '/m/sys/notice/delete');
INSERT INTO `auth_permission`
VALUES (30030000, 30000000, 'Banner管理', 1, 1, '');
INSERT INTO `auth_permission`
VALUES (30030001, 30030000, '查询banner列表', 1, 2, '/m/sys/banner/page');
INSERT INTO `auth_permission`
VALUES (30030002, 30030000, '查询banner详情', 1, 2, '/m/sys/banner/detail');
INSERT INTO `auth_permission`
VALUES (30030003, 30030000, 'banner新增/编辑', 1, 2, '/m/sys/banner/add-update');
INSERT INTO `auth_permission`
VALUES (30030004, 30030000, 'banner上下架', 1, 2, '/m/sys/banner/status');
INSERT INTO `auth_permission`
VALUES (30030005, 30030000, 'banner删除', 1, 2, '/m/sys/banner/delete');
INSERT INTO `auth_permission`
VALUES (30040000, 30000000, '首页菜单', 0, 1, '');
INSERT INTO `auth_permission`
VALUES (30040001, 30040000, '查询菜单列表', 0, 2, '/m/sys/menu/page');
INSERT INTO `auth_permission`
VALUES (30040002, 30040000, '查询菜单详情', 0, 2, '/m/sys/menu/detail');
INSERT INTO `auth_permission`
VALUES (30040003, 30040000, '菜单添加/编辑', 1, 2, '/m/sys/menu/add-update');
INSERT INTO `auth_permission`
VALUES (30040004, 30040000, '菜单上下架', 1, 2, '/m/sys/menu/status');
INSERT INTO `auth_permission`
VALUES (30040005, 30040000, '菜单删除', 1, 2, '/m/sys/menu/delete');
INSERT INTO `auth_permission`
VALUES (40000000, 0, '用户中心', 0, 0, '');
INSERT INTO `auth_permission`
VALUES (40010000, 40000000, '前台用户管理', 1, 1, '');
INSERT INTO `auth_permission`
VALUES (40010001, 40010000, '查询用户列表', 1, 2, '/m/user/front/page');
INSERT INTO `auth_permission`
VALUES (40010002, 40010000, '查询用户详情', 1, 2, '/m/user/front/info');
INSERT INTO `auth_permission`
VALUES (40010003, 40010000, '用户邀请人修改', 1, 2, '/m/user/front/parent-address');
INSERT INTO `auth_permission`
VALUES (40010201, 40010000, '查询用户资产列表', 1, 2, '/m/user/asset/page');
INSERT INTO `auth_permission`
VALUES (40020000, 40000000, '后台用户管理', 0, 1, '');
INSERT INTO `auth_permission`
VALUES (40020001, 40020000, '查询用户列表', 0, 2, '/m/user/back/page');
INSERT INTO `auth_permission`
VALUES (40020002, 40020000, '查询用户详情', 0, 2, '/m/user/back/info');
INSERT INTO `auth_permission`
VALUES (40020003, 40020000, '子账号添加/编辑', 0, 2, '/m/user/back/child/add-update');
INSERT INTO `auth_permission`
VALUES (40020004, 40020000, '子账号删除', 0, 2, '/m/user/back/child/delete');
INSERT INTO `auth_permission`
VALUES (40020010, 40020000, '用户信息更新', 0, 2, '/m/account/info-update');
INSERT INTO `auth_permission`
VALUES (40030000, 40000000, '角色权限管理', 0, 1, '');
INSERT INTO `auth_permission`
VALUES (40030001, 40030000, '查询角色列表', 0, 2, '/m/auth/role/list');
INSERT INTO `auth_permission`
VALUES (40030002, 40030000, '角色添加/编辑', 0, 2, '/m/auth/role/add-update');
INSERT INTO `auth_permission`
VALUES (40030003, 40030000, '角色删除', 0, 2, '/m/auth/role/delete');
INSERT INTO `auth_permission`
VALUES (40030004, 40030000, '查询角色的权限', 0, 2, '/m/auth/permission/list');
INSERT INTO `auth_permission`
VALUES (40030005, 40030000, '角色的权限编辑', 0, 2, '/m/auth/permission/update');
INSERT INTO `auth_permission`
VALUES (40040000, 40000000, '后台日志', 0, 1, '');
INSERT INTO `auth_permission`
VALUES (40040001, 40040000, '查询后台日志', 0, 2, '/m/user/back/log/page');
INSERT INTO `auth_permission`
VALUES (50000000, 0, '财务中心', 0, 0, '');
INSERT INTO `auth_permission`
VALUES (50010000, 50000000, '提币管理', 0, 1, '');
INSERT INTO `auth_permission`
VALUES (50010001, 50010000, '查询商户资产列表', 0, 2, '/m/user-back/asset/page');
INSERT INTO `auth_permission`
VALUES (50010002, 50010000, '查询商户资产记录', 0, 2, '/m/user-back/asset/log/page');
INSERT INTO `auth_permission`
VALUES (50010003, 50010000, '资产提现', 2, 2, '/m/user-back/asset/withdraw/apply');
INSERT INTO `auth_permission`
VALUES (90000000, 0, '其他', 0, 0, '');
INSERT INTO `auth_permission`
VALUES (90090000, 90000000, '文件管理', 0, 1, '');
INSERT INTO `auth_permission`
VALUES (90090001, 90090000, '上传文件', 0, 2, '/m/sys/file/upload');
INSERT INTO `auth_permission`
VALUES (90090002, 90090000, '获取上传凭证', 0, 2, '/m/sys/file/upload-signature');

-- ----------------------------
-- Table structure for auth_role
-- ----------------------------
DROP TABLE IF EXISTS `auth_role`;
CREATE TABLE `auth_role`
(
    `child_role` int                                                          NOT NULL AUTO_INCREMENT COMMENT '权限',
    `address`    varchar(42) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '所属地址',
    `name`       varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL     DEFAULT '' COMMENT '名称',
    PRIMARY KEY (`child_role`) USING BTREE,
    INDEX `idx_a` (`address` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '授权-角色'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of auth_role
-- ----------------------------

-- ----------------------------
-- Table structure for auth_role_permission
-- ----------------------------
DROP TABLE IF EXISTS `auth_role_permission`;
CREATE TABLE `auth_role_permission`
(
    `role_id`       int NOT NULL COMMENT '角色',
    `permission_id` int NOT NULL COMMENT '权限',
    PRIMARY KEY (`role_id`, `permission_id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '授权-角色权限'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of auth_role_permission
-- ----------------------------

-- ----------------------------
-- Table structure for chain_contract
-- ----------------------------
DROP TABLE IF EXISTS `chain_contract`;
CREATE TABLE `chain_contract`
(
    `ct_id`   int                                                          NOT NULL AUTO_INCREMENT COMMENT '合约编号',
    `type`    varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT '0' COMMENT '类型',
    `name`    varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '名称',
    `address` varchar(42) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '合约地址',
    PRIMARY KEY (`ct_id`) USING BTREE,
    UNIQUE INDEX `uni_ca` (`address` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '区块链-合约'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of chain_contract
-- ----------------------------

-- ----------------------------
-- Table structure for goods_favorite
-- ----------------------------
DROP TABLE IF EXISTS `goods_favorite`;
CREATE TABLE `goods_favorite`
(
    `id`          bigint                                                       NOT NULL AUTO_INCREMENT,
    `goods_id`    varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品编号',
    `address`     varchar(42) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '地址',
    `create_time` bigint                                                       NULL DEFAULT NULL COMMENT '收藏时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uni_gi_a` (`goods_id` ASC, `address` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品-收藏'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of goods_favorite
-- ----------------------------

-- ----------------------------
-- Table structure for goods_mint
-- ----------------------------
DROP TABLE IF EXISTS `goods_mint`;
CREATE TABLE `goods_mint`
(
    `id`         bigint                                                       NOT NULL AUTO_INCREMENT,
    `goods_id`   varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品编号',
    `sku_id`     varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'sku_id',
    `start_num`  bigint UNSIGNED                                              NOT NULL COMMENT '起始编号（包含）',
    `end_num`    bigint UNSIGNED                                              NOT NULL COMMENT '结束编号（包含）',
    `status`     tinyint                                                      NULL DEFAULT 0 COMMENT '0等待发行，1发行中，2发行失败，3发行成功',
    `uri_status` tinyint                                                      NULL DEFAULT 0 COMMENT '0等待更新，1更新中，2更新失败，3更新成功',
    `tx_hash`    varchar(66) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '哈希',
    `tx_note`    text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci        NULL COMMENT '备注',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uni_gi_si_sn_en` (`goods_id` ASC, `sku_id` ASC, `start_num` ASC, `end_num` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品-铸造'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of goods_mint
-- ----------------------------

-- ----------------------------
-- Table structure for goods_sku
-- ----------------------------
DROP TABLE IF EXISTS `goods_sku`;
CREATE TABLE `goods_sku`
(
    `goods_id`       varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci         NOT NULL COMMENT '商品编号',
    `sku_id`         varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci         NOT NULL COMMENT 'sku编号',
    `address`        varchar(42) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci         NULL     DEFAULT '' COMMENT '商户地址',
    `token_prefix`   int UNSIGNED                                                         NOT NULL DEFAULT 0 COMMENT '序列号，token前缀',
    `prop_value_1`   varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci         NULL     DEFAULT '' COMMENT '参数1',
    `prop_value_2`   varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci         NULL     DEFAULT '' COMMENT '参数2',
    `name`           varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci        NULL     DEFAULT '' COMMENT '名称',
    `token_name`     varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci        NULL     DEFAULT '' COMMENT 'token名称',
    `cover`          varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci        NULL     DEFAULT '' COMMENT '封面',
    `price`          decimal(36, 18) UNSIGNED                                             NULL     DEFAULT 0.000000000000000000 COMMENT '价格',
    `total`          int                                                                  NULL     DEFAULT 0 COMMENT '总量',
    `sales`          int                                                                  NULL     DEFAULT 0 COMMENT '销量',
    `stock`          int GENERATED ALWAYS AS ((`total` - `sales`)) VIRTUAL COMMENT '库存' NULL,
    `order_limit`    int                                                                  NULL     DEFAULT -1 COMMENT '限购数量，-1不限购',
    `order_pack`     int                                                                  NULL     DEFAULT 1 COMMENT '购买数量必须是该值的整数倍',
    `express_type`   tinyint                                                              NULL     DEFAULT 0 COMMENT '提货类型，0不可提货，1可以提货',
    `blind_box_type` tinyint                                                              NULL     DEFAULT 0 COMMENT '是否盲盒，0不是，1是',
    `trace_hash`     varchar(66) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci         NULL     DEFAULT '' COMMENT '溯源哈希',
    `status`         tinyint                                                              NULL     DEFAULT 0 COMMENT '0等待发行，1发行中，2发行失败，3发行成功',
    `create_time`    bigint                                                               NULL     DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`goods_id`, `sku_id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品-SKU'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of goods_sku
-- ----------------------------

-- ----------------------------
-- Table structure for goods_sku_properties
-- ----------------------------
DROP TABLE IF EXISTS `goods_sku_properties`;
CREATE TABLE `goods_sku_properties`
(
    `goods_id`     varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品编号',
    `title_1`      varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '标题1',
    `value_1_json` json                                                         NULL COMMENT '值数组1',
    `title_2`      varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '标题2',
    `value_2_json` json                                                         NULL COMMENT '值数组2',
    PRIMARY KEY (`goods_id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品-SKU-参数'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of goods_sku_properties
-- ----------------------------

-- ----------------------------
-- Table structure for goods_spu
-- ----------------------------
DROP TABLE IF EXISTS `goods_spu`;
CREATE TABLE `goods_spu`
(
    `goods_id`         varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci         NOT NULL COMMENT '商品编号',
    `address`          varchar(42) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci         NOT NULL COMMENT '商户地址',
    `type`             tinyint                                                              NULL DEFAULT 0 COMMENT '类型，0普通，1盲盒，2合成',
    `menu_json`        json                                                                 NULL COMMENT '菜单',
    `classify_json`    json                                                                 NULL COMMENT '类目',
    `name`             varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci        NULL DEFAULT '' COMMENT '名称',
    `des`              varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci        NULL DEFAULT '' COMMENT '描述',
    `detail`           text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci                NULL COMMENT '详情',
    `cover`            varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci        NULL DEFAULT '' COMMENT '封面',
    `image_json`       json                                                                 NULL COMMENT '图片',
    `price`            decimal(36, 18) UNSIGNED                                             NULL DEFAULT 0.000000000000000000 COMMENT '价格',
    `total`            int                                                                  NULL DEFAULT 0 COMMENT '总量',
    `sales`            int                                                                  NULL DEFAULT 0 COMMENT '销量',
    `stock`            int GENERATED ALWAYS AS ((`total` - `sales`)) VIRTUAL COMMENT '库存' NULL,
    `favorite`         int                                                                  NULL DEFAULT 0 COMMENT '收藏量',
    `order_limit`      int                                                                  NULL DEFAULT -1 COMMENT '限购数量，-1不限购',
    `sale_type`        int                                                                  NULL DEFAULT 0 COMMENT '销售类型，0常规销售，1白名单销售',
    `sale_time`        bigint                                                               NULL DEFAULT NULL COMMENT '开始售卖时间',
    `sale_time_normal` bigint                                                               NULL DEFAULT NULL COMMENT '非常规销售转为常规销售的开始时间',
    `status`           tinyint                                                              NULL DEFAULT 0 COMMENT '状态，0草稿，1发行中，2发行失败，3发行成功待上架，4已上架，5修改中，6增发失败',
    `hidden_status`    tinyint                                                              NULL DEFAULT 0 COMMENT '隐藏状态，0否，1是',
    `create_time`      bigint                                                               NULL DEFAULT NULL COMMENT '创建时间',
    `recommend`        int                                                                  NULL DEFAULT 0 COMMENT '是否平台推荐，1是0否',
    `show_order`       int                                                                  NULL DEFAULT 0 COMMENT '商户手动排序权重，数值越小越靠前',
    PRIMARY KEY (`goods_id`) USING BTREE,
    INDEX `idx_addr_st` (`address` ASC, `status` ASC) USING BTREE,
    INDEX `idx_status` (`status` ASC) USING BTREE,
    INDEX `score` (`status` ASC, `recommend` DESC, `sales` DESC, `favorite` DESC, `sale_time` DESC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品-SPU'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of goods_spu
-- ----------------------------

-- ----------------------------
-- Table structure for goods_white
-- ----------------------------
DROP TABLE IF EXISTS `goods_white`;
CREATE TABLE `goods_white`
(
    `id`       bigint                                                       NOT NULL AUTO_INCREMENT,
    `goods_id` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品编号',
    `sku_id`   varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'sku编号',
    `address`  varchar(42) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '地址',
    `num`      int                                                          NULL DEFAULT 0 COMMENT '数量',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uni_gi_a` (`goods_id` ASC, `address` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品-白名单'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of goods_white
-- ----------------------------

-- ----------------------------
-- Table structure for order_blind_box
-- ----------------------------
DROP TABLE IF EXISTS `order_blind_box`;
CREATE TABLE `order_blind_box`
(
    `order_id`         varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
    `seller_address`   varchar(42) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '卖家地址',
    `buyer_address`    varchar(42) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '买家地址',
    `goods_id`         varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '商品编号',
    `sku_id`           varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '',
    `ct_id`            int                                                          NULL DEFAULT 0 COMMENT '合约编号',
    `token_id`         bigint                                                       NULL DEFAULT 0,
    `tx_hash`          varchar(66) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '',
    `reward_sku_id`    varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '',
    `reward_token_id`  bigint                                                       NULL DEFAULT 0,
    `reward_tx_hash`   varchar(66) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '',
    `reward_tx_note`   text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci        NULL,
    `reward_tx_status` tinyint                                                      NULL DEFAULT 0 COMMENT '0等待，1成功，2失败',
    `order_time`       bigint                                                       NULL DEFAULT NULL,
    PRIMARY KEY (`order_id`) USING BTREE,
    UNIQUE INDEX `uni_th` (`tx_hash` ASC) USING BTREE,
    INDEX `idx_s` (`reward_tx_status` ASC) USING BTREE,
    INDEX `idx_gi_si` (`goods_id` ASC, `sku_id` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单-盲盒'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of order_blind_box
-- ----------------------------

-- ----------------------------
-- Table structure for order_express
-- ----------------------------
DROP TABLE IF EXISTS `order_express`;
CREATE TABLE `order_express`
(
    `order_id`                 varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL COMMENT '订单编号',
    `seller_address`           varchar(42) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT '' COMMENT '卖家地址',
    `buyer_address`            varchar(42) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT '' COMMENT '买家地址',
    `goods_id`                 varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT '' COMMENT '商品编号',
    `sku_id`                   varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT '' COMMENT 'sku_id',
    `ct_id`                    int                                                           NULL DEFAULT 0 COMMENT '合约编号',
    `token_id_json`            json                                                          NULL,
    `num`                      int                                                           NULL DEFAULT 1 COMMENT '数量',
    `tx_hash`                  varchar(66) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT '' COMMENT '哈希',
    `status`                   tinyint                                                       NULL DEFAULT 0 COMMENT '状态\r\n0待发货\r\n1发货中\r\n2已收货\r\n3申请退货\r\n4不同意退货\r\n5已同意退货，用户填写退货单号\r\n6退货中\r\n7已退货',
    `name`                     text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci         NULL COMMENT '收货人',
    `phone`                    text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci         NULL COMMENT '手机号',
    `detail`                   text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci         NULL COMMENT '具体地址',
    `note`                     text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci         NULL COMMENT '提货备注',
    `express_code`             varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '物流编号',
    `order_time`               bigint                                                        NULL DEFAULT NULL COMMENT '下单时间',
    `express_time`             bigint                                                        NULL DEFAULT NULL COMMENT '发货时间',
    `confirm_time`             bigint                                                        NULL DEFAULT NULL COMMENT '确认时间',
    `confirm_auto_time`        bigint                                                        NULL DEFAULT NULL COMMENT '自动确认时间',
    `refund_note`              json                                                          NULL COMMENT '退货备注',
    `refund_express_code`      varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '退货物流编号',
    `refund_apply_time`        bigint                                                        NULL DEFAULT NULL COMMENT '退货申请时间',
    `refund_audit_time`        bigint                                                        NULL DEFAULT NULL COMMENT '退货审核时间',
    `refund_confirm_time`      bigint                                                        NULL DEFAULT NULL COMMENT '退货确认时间',
    `refund_confirm_auto_time` bigint                                                        NULL DEFAULT NULL COMMENT '退货自动确认时间',
    `refund_tx_status`         int                                                           NULL DEFAULT 0 COMMENT '退货打币状态0等待中，1成功，2失败',
    `refund_tx_hash`           varchar(66) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT '' COMMENT '退货打币哈希',
    `refund_tx_note`           text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci         NULL COMMENT '退货打币备注',
    `delete_status`            int                                                           NULL DEFAULT 0 COMMENT '软删除标记 0正常，1删除',
    PRIMARY KEY (`order_id`) USING BTREE,
    INDEX `idx_sa_ba` (`seller_address` ASC, `buyer_address` ASC) USING BTREE,
    INDEX `idx_s` (`status` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单-提货'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of order_express
-- ----------------------------

-- ----------------------------
-- Table structure for order_info
-- ----------------------------
DROP TABLE IF EXISTS `order_info`;
CREATE TABLE `order_info`
(
    `order_id`             varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单编号',
    `seller_address`       varchar(42) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '卖家地址',
    `buyer_address`        varchar(42) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '买家地址',
    `goods_id`             varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '商品编号',
    `sku_id`               varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT 'sku_id',
    `token_id_json`        json                                                         NULL,
    `num`                  int                                                          NULL DEFAULT 0 COMMENT '数量',
    `price`                decimal(36, 18)                                              NULL DEFAULT 0.000000000000000000 COMMENT '单价',
    `amount`               decimal(36, 18)                                              NULL DEFAULT 0.000000000000000000 COMMENT '总价',
    `pay_chain`            int                                                          NULL DEFAULT 0 COMMENT '支付链',
    `pay_coin`             int                                                          NULL DEFAULT 0 COMMENT '支付币',
    `pay_price`            decimal(36, 18)                                              NULL DEFAULT 0.000000000000000000 COMMENT '支付行情',
    `pay_amount`           decimal(36, 18)                                              NULL DEFAULT 0.000000000000000000 COMMENT '支付总价',
    `pay_hash`             varchar(66) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '支付哈希',
    `status`               tinyint                                                      NULL DEFAULT 0 COMMENT '0待支付，1已支付，2已撤销，3已超时，4申请退款，5不同意，6同意',
    `order_time`           bigint                                                       NULL DEFAULT NULL COMMENT '下单时间',
    `pay_time`             bigint                                                       NULL DEFAULT NULL COMMENT '支付时间',
    `expire_time`          bigint                                                       NULL DEFAULT NULL COMMENT '过期时间',
    `cancel_time`          bigint                                                       NULL DEFAULT NULL COMMENT '撤销时间',
    `tx_status`            tinyint                                                      NULL DEFAULT 0 COMMENT '0等待中，1成功，2失败',
    `tx_hash`              varchar(66) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '打币哈希',
    `tx_note`              text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci        NULL COMMENT '打币备注',
    `unfreeze_status`      int                                                          NULL DEFAULT 0 COMMENT '解冻状态，0无需处理，1已冻结，2已解冻',
    `unfreeze_time`        bigint                                                       NULL DEFAULT NULL COMMENT '解冻时间',
    `client_ip`            varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '下单ip',
    `user_agent`           text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci        NULL COMMENT '代理',
    `delete_status`        int                                                          NULL DEFAULT 0 COMMENT '	\r\n软删除标记 0正常，1删除',
    `refund_note`          json                                                         NULL COMMENT '退款备注',
    `refund_apply_tx_hash` varchar(66) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '退款申请哈希',
    `refund_price`         decimal(36, 18)                                              NULL DEFAULT 0.000000000000000000 COMMENT '退款行情',
    `refund_amount`        decimal(36, 18)                                              NULL DEFAULT 0.000000000000000000 COMMENT '退款总量',
    `refund_tx_status`     int                                                          NULL DEFAULT 0 COMMENT '退款打币状态0等待中，1成功，2失败',
    `refund_tx_hash`       varchar(66) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '退款打币哈希',
    `refund_tx_note`       text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci        NULL COMMENT '退款打币备注',
    `express_type`         int                                                          NULL DEFAULT 0 COMMENT '是否直接提货，0否1是',
    `express_json`         json                                                         NULL COMMENT '提货信息',
    PRIMARY KEY (`order_id`) USING BTREE,
    INDEX `idx_s_ts` (`status` ASC, `tx_status` ASC) USING BTREE,
    INDEX `idx_sa_ba` (`seller_address` ASC, `buyer_address` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单-信息'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of order_info
-- ----------------------------

-- ----------------------------
-- Table structure for order_limit
-- ----------------------------
DROP TABLE IF EXISTS `order_limit`;
CREATE TABLE `order_limit`
(
    `id`       bigint                                                       NOT NULL AUTO_INCREMENT,
    `goods_id` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '商品编号',
    `sku_id`   varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'sku编号',
    `address`  varchar(42) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '地址',
    `num`      int                                                          NULL     DEFAULT 0 COMMENT '已购买数量',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uni_gi_si_a` (`goods_id` ASC, `sku_id` ASC, `address` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单-用户商品购买数量'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of order_limit
-- ----------------------------

-- ----------------------------
-- Table structure for sys_banner
-- ----------------------------
DROP TABLE IF EXISTS `sys_banner`;
CREATE TABLE `sys_banner`
(
    `id`          bigint UNSIGNED                                                NOT NULL AUTO_INCREMENT,
    `title`       varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci   NULL DEFAULT '' COMMENT '标题',
    `cover`       varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT '' COMMENT '封面',
    `target`      varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '指向目标',
    `show_order`  int                                                            NULL DEFAULT 0 COMMENT '排序，越小越前',
    `status`      tinyint                                                        NULL DEFAULT 0 COMMENT '状态，0未上线，1已上线',
    `start_time`  bigint                                                         NULL DEFAULT NULL COMMENT '开始时间',
    `end_time`    bigint                                                         NULL DEFAULT NULL COMMENT '结束时间',
    `create_time` bigint                                                         NULL DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_merchant_uid_state` (`status` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统-banner'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_banner
-- ----------------------------

-- ----------------------------
-- Table structure for sys_classify
-- ----------------------------
DROP TABLE IF EXISTS `sys_classify`;
CREATE TABLE `sys_classify`
(
    `id`         int                                                          NOT NULL AUTO_INCREMENT COMMENT '类目编号',
    `type`       tinyint                                                      NULL DEFAULT 0 COMMENT '类型，0商品，1公告',
    `parent_id`  int                                                          NULL DEFAULT 0 COMMENT '父类目编号，0为顶级类目',
    `name`       varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '类目名称',
    `level`      tinyint                                                      NULL DEFAULT 0 COMMENT '类目层级，商品共4级1234，公告共2级12',
    `show_order` int                                                          NULL DEFAULT 0 COMMENT '类目排序，越小越靠前',
    `status`     tinyint                                                      NULL DEFAULT 1 COMMENT '状态，0隐藏，1显示',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_type` (`type` ASC) USING BTREE,
    INDEX `idx_parent_id` (`parent_id` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统-类目'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_classify
-- ----------------------------

-- ----------------------------
-- Table structure for sys_file
-- ----------------------------
DROP TABLE IF EXISTS `sys_file`;
CREATE TABLE `sys_file`
(
    `id`          bigint UNSIGNED                                               NOT NULL AUTO_INCREMENT,
    `file_url`    varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '图片地址',
    `create_time` bigint                                                        NULL DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uni_f` (`file_url` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统-文件'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_file
-- ----------------------------

-- ----------------------------
-- Table structure for sys_hash_used
-- ----------------------------
DROP TABLE IF EXISTS `sys_hash_used`;
CREATE TABLE `sys_hash_used`
(
    `id`          bigint                                                       NOT NULL AUTO_INCREMENT,
    `tx_hash`     varchar(66) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '交易哈希',
    `create_time` bigint                                                       NULL     DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uni_tx` (`tx_hash` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统-已经使用过的哈希'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_hash_used
-- ----------------------------

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`
(
    `menu_id`     int                                                           NOT NULL AUTO_INCREMENT,
    `title`       varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT '' COMMENT '标题',
    `cover`       varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '图标',
    `status`      int                                                           NULL DEFAULT NULL COMMENT '状态，0待上架，1已上架',
    `show_order`  int                                                           NULL DEFAULT NULL COMMENT '排序，越小越前',
    `create_time` bigint                                                        NULL DEFAULT NULL,
    PRIMARY KEY (`menu_id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统-菜单'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_menu
-- ----------------------------

-- ----------------------------
-- Table structure for sys_notice
-- ----------------------------
DROP TABLE IF EXISTS `sys_notice`;
CREATE TABLE `sys_notice`
(
    `id`            bigint UNSIGNED                                               NOT NULL AUTO_INCREMENT,
    `classify_json` json                                                          NULL COMMENT '类目',
    `title`         varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '标题',
    `content`       text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci         NULL COMMENT '内容',
    `cover`         varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '封面',
    `status`        tinyint                                                       NULL DEFAULT 0 COMMENT '状态，0待上架，1已上架',
    `top`           tinyint                                                       NULL DEFAULT 0 COMMENT '置顶，0否，1是',
    `scroll`        tinyint                                                       NULL DEFAULT 0 COMMENT '滚动，0否，1是',
    `active_time`   bigint                                                        NULL DEFAULT NULL COMMENT '上架时间',
    `create_time`   bigint                                                        NULL DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_s_ss` (`status` ASC, `scroll` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统-公告'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_notice
-- ----------------------------

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`
(
    `uid`               bigint UNSIGNED                                               NOT NULL AUTO_INCREMENT COMMENT 'uid',
    `address`           varchar(42) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL DEFAULT '' COMMENT '地址',
    `parent_address`    varchar(42) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs  NULL     DEFAULT '' COMMENT '上级用户的外部地址',
    `status`            tinyint                                                       NULL     DEFAULT 1 COMMENT '状态，0冻结，1正常',
    `nickname`          varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL     DEFAULT '' COMMENT '昵称',
    `head_url`          varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL     DEFAULT '' COMMENT '头像',
    `invite_num`        int                                                           NULL     DEFAULT 0 COMMENT '已邀请人数',
    `register_ip`       varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL     DEFAULT '' COMMENT '注册ip',
    `register_time`     bigint                                                        NULL     DEFAULT NULL COMMENT '注册时间',
    `latest_login_ip`   varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL     DEFAULT '' COMMENT '最近登录ip',
    `latest_login_time` bigint                                                        NULL     DEFAULT NULL COMMENT '最近登录时间',
    `btc_address`       varchar(34) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL     DEFAULT '' COMMENT '1开头的比特币地址',
    PRIMARY KEY (`uid`) USING BTREE,
    UNIQUE INDEX `uni_a` (`address` ASC) USING BTREE,
    INDEX `idx_pa` (`parent_address` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户-前台用户'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user
-- ----------------------------

-- ----------------------------
-- Table structure for user_admin
-- ----------------------------
DROP TABLE IF EXISTS `user_admin`;
CREATE TABLE `user_admin`
(
    `uid`                bigint UNSIGNED                                               NOT NULL AUTO_INCREMENT COMMENT 'uid',
    `address`            varchar(42) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL DEFAULT '' COMMENT '地址',
    `parent_address`     varchar(42) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL     DEFAULT '' COMMENT '父账号地址',
    `status`             tinyint                                                       NULL     DEFAULT 1 COMMENT '状态，0冻结，1正常',
    `role`               tinyint                                                       NULL     DEFAULT 1 COMMENT '角色，0无，1管理员，2商户',
    `child_role`         int                                                           NULL     DEFAULT 0 COMMENT '子角色，0无',
    `nickname`           varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL     DEFAULT '' COMMENT '昵称',
    `head_url`           varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL     DEFAULT '' COMMENT '头像',
    `inside_address`     varchar(42) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL     DEFAULT '' COMMENT '内部地址',
    `inside_private_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL     DEFAULT '' COMMENT '内部私钥',
    `apply_status`       tinyint                                                       NULL     DEFAULT 0 COMMENT '子账号状态，0初始状态，1商户添加子账号，待同意',
    `profit_sharing`     decimal(6, 2)                                                 NULL     DEFAULT 0.00 COMMENT '平台分账比例',
    `register_ip`        varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL     DEFAULT '' COMMENT '注册ip',
    `register_time`      bigint                                                        NULL     DEFAULT NULL COMMENT '注册时间',
    `latest_login_ip`    varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL     DEFAULT '' COMMENT '最近登录ip',
    `latest_login_time`  bigint                                                        NULL     DEFAULT NULL COMMENT '最近登录时间',
    `btc_address`        varchar(34) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL     DEFAULT '' COMMENT '1开头的比特币地址',
    PRIMARY KEY (`uid`) USING BTREE,
    UNIQUE INDEX `uni_a` (`address` ASC) USING BTREE,
    UNIQUE INDEX `uni_ia` (`inside_address` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户-后台用户'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_admin
-- ----------------------------

-- ----------------------------
-- Table structure for user_admin_log
-- ----------------------------
DROP TABLE IF EXISTS `user_admin_log`;
CREATE TABLE `user_admin_log`
(
    `id`             bigint UNSIGNED                                               NOT NULL AUTO_INCREMENT,
    `address`        varchar(42) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT '' COMMENT '用户地址',
    `parent_address` varchar(42) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT '' COMMENT '父用户地址',
    `lang`           varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci   NULL DEFAULT NULL COMMENT '语言',
    `duration`       int                                                           NULL DEFAULT NULL COMMENT '用时，毫秒',
    `method`         varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci   NULL DEFAULT '' COMMENT '方法',
    `uri`            varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '路径',
    `params_json`    json                                                          NULL COMMENT '请求信息',
    `result_json`    json                                                          NULL COMMENT '响应信息',
    `client_ip`      varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT '' COMMENT 'ip地址',
    `user_agent`     text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci         NULL COMMENT 'User-Agent',
    `create_time`    bigint                                                        NULL DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_admin_uid` (`address` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户-后台用户日志'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_admin_log
-- ----------------------------

-- ----------------------------
-- Table structure for user_asset_collection
-- ----------------------------
DROP TABLE IF EXISTS `user_asset_collection`;
CREATE TABLE `user_asset_collection`
(
    `id`          bigint                                                       NOT NULL AUTO_INCREMENT,
    `goods_id`    varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品编号',
    `address`     varchar(42) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
    `num`         decimal(36, 18)                                              NULL DEFAULT 0.000000000000000000 COMMENT '数量',
    `update_time` bigint                                                       NULL DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uni_gi_a` (`goods_id` ASC, `address` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户-资产-集合'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_asset_collection
-- ----------------------------

-- ----------------------------
-- Table structure for user_asset_token
-- ----------------------------
DROP TABLE IF EXISTS `user_asset_token`;
CREATE TABLE `user_asset_token`
(
    `id`          bigint                                                       NOT NULL AUTO_INCREMENT,
    `goods_id`    varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL     DEFAULT '' COMMENT '商品编号',
    `sku_id`      varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'sku_id',
    `address`     varchar(42) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '地址',
    `ct_id`       int                                                          NOT NULL COMMENT '合约编号',
    `token_id`    bigint                                                       NOT NULL,
    `num`         decimal(36, 18)                                              NULL     DEFAULT 0.000000000000000000 COMMENT '数量',
    `update_time` bigint                                                       NULL     DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uni_gi_si_a_ci_ti` (`address` ASC, `ct_id` ASC, `token_id` ASC) USING BTREE,
    INDEX `idx_gi_a` (`goods_id` ASC, `address` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户-资产'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_asset_token
-- ----------------------------

-- ----------------------------
-- Table structure for user_back_asset
-- ----------------------------
DROP TABLE IF EXISTS `user_back_asset`;
CREATE TABLE `user_back_asset`
(
    `id`             bigint                                                       NOT NULL AUTO_INCREMENT,
    `address`        varchar(42) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
    `coin_type`      int                                                          NOT NULL COMMENT '币',
    `balance`        decimal(36, 18)                                              NULL DEFAULT NULL COMMENT '余额',
    `frozen_balance` decimal(36, 18)                                              NULL DEFAULT NULL COMMENT '冻结余额',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uni_a_cc` (`address` ASC, `coin_type` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户-资产-后台'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_back_asset
-- ----------------------------

-- ----------------------------
-- Table structure for user_back_asset_change_history
-- ----------------------------
DROP TABLE IF EXISTS `user_back_asset_change_history`;
CREATE TABLE `user_back_asset_change_history`
(
    `log_id`        bigint                                                       NOT NULL AUTO_INCREMENT,
    `address`       varchar(42) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
    `coin_type`     int                                                          NULL DEFAULT NULL,
    `number`        decimal(36, 18)                                              NULL DEFAULT NULL COMMENT '数量',
    `balance_type`  int                                                          NULL DEFAULT 0 COMMENT '0可用余额，1冻结中',
    `log_type`      int                                                          NULL DEFAULT NULL COMMENT '0提现申请，\r\n1提现申请撤销，\r\n2提现申请审核未通过，\r\n3提现申请审核通过打币中，\r\n4提现上链确认中，\r\n5提现上链失败，\r\n6提现上链成功',
    `withdraw_type` int                                                          NULL DEFAULT NULL COMMENT '是否是提现，0否，1是',
    `create_time`   bigint                                                       NULL DEFAULT NULL COMMENT '日期',
    `extend_data`   json                                                         NULL COMMENT '拓展信息',
    PRIMARY KEY (`log_id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户-资金-后台-变动记录'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_back_asset_change_history
-- ----------------------------

-- ----------------------------
-- Table structure for user_express
-- ----------------------------
DROP TABLE IF EXISTS `user_express`;
CREATE TABLE `user_express`
(
    `id`          bigint UNSIGNED                                               NOT NULL AUTO_INCREMENT,
    `address`     varchar(42) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL COMMENT '地址',
    `name`        varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT '' COMMENT '收货人',
    `phone`       varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT '' COMMENT '手机',
    `region`      varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '所在地区',
    `location`    varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '详细地址',
    `status`      tinyint                                                       NULL DEFAULT 0 COMMENT '默认：0否，1是',
    `create_time` bigint                                                        NULL DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_a` (`address` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户-收货地址'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_express
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
