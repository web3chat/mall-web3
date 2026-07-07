// SPDX-License-Identifier: MIT
pragma solidity ^0.8.1;

/**
 * @title 权限控制合约
 * @notice 实现基于角色的访问控制机制，支持超级管理员、管理员、商户和账户冻结功能
 */
abstract contract FzmAccessControl {
    /// @dev 角色存储映射：角色类型 => 账户地址 => 是否拥有
    mapping(bytes32 => mapping(address => bool)) private _roles;

    // ========== 角色类型常量定义 ==========
    /// @dev 超级管理员拥有最高权限
    bytes32 internal constant SUPER_ADMIN_ROLE = 0x00;
    /// @dev 管理员角色标识
    bytes32 internal constant ADMIN_ROLE = keccak256("ADMIN_ROLE");
    /// @dev 商户角色标识
    bytes32 internal constant MERCHANT_ROLE = keccak256("MERCHANT_ROLE");
    /// @dev 账户冻结状态标识
    bytes32 internal constant FROZEN_STATUS = keccak256("FROZEN_STATUS");

    // ========== 事件定义 ==========
    /// @notice 超级管理员权限变更事件
    event TransferSuperAdmin(address indexed operator, address indexed account);
    /// @notice 管理员权限变更事件
    event SetAdmin(address indexed operator, address indexed account, bool status);
    /// @notice 商户权限变更事件
    event SetMerchant(address indexed operator, address indexed account, bool status);
    /// @notice 账户冻结状态变更事件
    event FreezeAccount(address indexed operator, address indexed account, bool status);

    /**
     * @notice 初始化函数
     * @dev 授予部署者管理员权限
     */
    constructor() {
        _roles[ADMIN_ROLE][msg.sender] = _roles[SUPER_ADMIN_ROLE][msg.sender] = true;
    }

    /**
     * @dev 修饰符：验证调用者是否拥有指定角色
     * @param role 需要验证的角色标识符
     */
    modifier onlyRole(bytes32 role) {
        require(hasRole(role, msg.sender), "Missing role");
        _;
    }

    /// @dev 修饰符：检查调用者是否未被冻结
    modifier onlyUnFrozen() {
        require(!hasRole(FROZEN_STATUS, msg.sender), "Account frozen");
        _;
    }

    // ========== 权限管理 ==========

    /**
     * @notice 转移超级管理员权限
     * @dev 安全限制：
     *      - 禁止操作零地址
     *      - 仅当前超级管理员可调用
     *      - 禁止转移给商户角色
     *      - 撤销原管理员的所有权限
     *      - 授予目标账户双重权限（SUPER_ADMIN和ADMIN）
     * @param account 新的超级管理员地址
     */
    function transferSuperAdmin(address account) external onlyRole(SUPER_ADMIN_ROLE) {
        require(account != address(0), "Invalid address");
        require(!hasRole(MERCHANT_ROLE, account), "Account is merchant");

        // 撤销原权限
        _roles[ADMIN_ROLE][msg.sender] = _roles[SUPER_ADMIN_ROLE][msg.sender] = false;

        // 授予新权限
        _roles[ADMIN_ROLE][account] = _roles[SUPER_ADMIN_ROLE][account] = true;

        emit TransferSuperAdmin(msg.sender, account);
        emit SetAdmin(msg.sender, msg.sender, false);
        emit SetAdmin(msg.sender, account, true);
    }

    /**
     * @notice 设置管理员权限
     * @dev 仅管理员可操作，禁止授予商户角色
     * @param account 目标账户
     * @param status 权限状态（true-授予，false-撤销）
     */
    function setAdmin(address account, bool status) external onlyRole(ADMIN_ROLE) onlyUnFrozen() {
        require(!hasRole(MERCHANT_ROLE, account), "Account is merchant");
        _modifyRole(ADMIN_ROLE, account, status);
        emit SetAdmin(msg.sender, account, status);
    }

    /**
     * @notice 设置商户权限
     * @dev 仅管理员可操作，禁止授予管理员角色
     * @param account 目标账户
     * @param status 权限状态（true-授予，false-撤销）
     */
    function setMerchant(address account, bool status) external onlyRole(ADMIN_ROLE) onlyUnFrozen() {
        require(!hasRole(ADMIN_ROLE, account), "Account is admin");
        _modifyRole(MERCHANT_ROLE, account, status);
        emit SetMerchant(msg.sender, account, status);
    }

    /**
     * @notice 冻结或解冻指定账户
     * @dev 仅管理员可操作，冻结账户无法执行特权操作
     * @param account 目标账户地址
     * @param status 冻结状态（true-冻结，false-解冻）
     */
    function freezeAccount(address account, bool status) external onlyRole(ADMIN_ROLE) onlyUnFrozen() {
        _modifyRole(FROZEN_STATUS, account, status);
        emit FreezeAccount(msg.sender, account, status);
    }

    /**
     * @notice 内部角色状态查询
     * @param role 查询的角色类型
     * @param account 查询的账户地址
     * @return 是否拥有该角色
     */
    function hasRole(bytes32 role, address account) internal view returns (bool) {
        return _roles[role][account];
    }

    /**
     * @notice 内部角色管理函数
     * @dev 包含多重保护机制：
     *      - 禁止操作零地址
     *      - 禁止修改超级管理员状态
     * @param role 目标角色类型
     * @param account 操作的账户地址
     * @param status true=授予角色，false=撤销角色
     */
    function _modifyRole(bytes32 role, address account, bool status) private {
        require(account != address(0), "Invalid address");
        require(!hasRole(SUPER_ADMIN_ROLE, account), "Cannot edit superadmin");

        _roles[role][account] = status;
    }
}