// SPDX-License-Identifier: MIT
pragma solidity ^0.8.1;
import "https://github.com/OpenZeppelin/openzeppelin-contracts/blob/v4.9.6/contracts/token/ERC1155/ERC1155.sol";
import "./FzmAccessControl.sol";

/**
 * @title FZM ERC1155代币合约
 */
contract FzmERC1155 is ERC1155, FzmAccessControl {
    /// @dev 单个前缀下最大序列号（1e8，不包含）
    uint256 private constant _MAX_TOKEN_SERIAL = 1e8;
    /// @dev 单次批量铸造最大数量
    uint256 private constant _MAX_BATCH_NUM = 1e3;
    /// @dev 十六进制字符表（用于URI生成）
    bytes16 private constant _HEX_SYMBOLS = "0123456789abcdef";

    /// @dev 基础元数据URI，格式必须为ipfs://<CID>/ 或 https://<domain>/
    string private _baseURI;

    /**
     * @dev 前缀元数据结构
     * @param minter        铸造者地址（首次铸造时锁定）
     * @param canExpress    是否允许提货（商户可修改）
     * @param isBlindBox    是否是盲盒（商户可修改）
     * @param bitmap      位图存储已用ID：
     *        - 每个uint256存储256个ID状态
     *        - 按bucket划分，bucket = serial / 256
     */
    struct PrefixInfo {
        address minter;
        bool canExpress;
        bool isBlindBox;
        mapping(uint256 => uint256) bitmap;
    }
    /// @dev 前缀信息存储映射
    mapping(uint256 => PrefixInfo) private _prefixInfos;

    /**
     * @dev 多地址批量转账结构
     * @param to    接收地址
     * @param ids   代币ID数组
     */
    struct MultiTX {
        address to;
        uint256[] ids;
    }

    // ========== 事件定义 ==========
    /// @notice 更新基础元数据URI
    event UpdateBaseURI(address indexed operator, string oldBaseURI, string newBaseURI);
    /// @notice 批量铸造NFT
    event MintBatchNFT(address indexed operator, address indexed to, uint256[] ids, uint256[] amounts);
    /// @notice 修改前缀元权限事件
    event UpdatePrefixMeta(address indexed operator, uint256[] prefixes, bool[] canExpresses, bool[] isBlindBoxes);
    /// @notice NFT提货
    event ExpressNFT(address indexed from, address indexed to, uint256[] ids, string detail);
    /// @notice NFT开启盲盒
    event OpenBlindBoxNFT(address indexed from, address indexed to, uint256 id);

    /**
     * @notice 合约初始化函数
     * @dev 设置基础URI
     * @param baseURI 元数据基础路径
     */
    constructor(string memory baseURI) ERC1155("") {
        _setBaseURI(baseURI);
    }

    // ========== override ==========
    /// @inheritdoc ERC1155
    function supportsInterface(bytes4 interfaceId) public view virtual override(ERC1155) returns (bool) {
        return super.supportsInterface(interfaceId);
    }
    /// @inheritdoc ERC1155
    function safeTransferFrom(address from, address to, uint256 id, uint256 amount, bytes memory data) public override onlyUnFrozen() {
        super.safeTransferFrom(from, to, id, amount, data);
    }
    /// @inheritdoc ERC1155
    function safeBatchTransferFrom(address from, address to, uint256[] memory ids, uint256[] memory amounts, bytes memory data) public override onlyUnFrozen() {
        super.safeBatchTransferFrom(from, to, ids, amounts, data);
    }
    /// @inheritdoc ERC1155
    function setApprovalForAll(address operator, bool approved) public override onlyUnFrozen() {
        super.setApprovalForAll(operator, approved);
    }

    // ========== 元数据URI ==========
    /**
     * @notice 获取代币元数据URI
     * @dev 返回格式：baseURI + 64位小写十六进制ID + .json
     * @param id 代币ID
     * @return 完整的元数据URI
     */
    function uri(uint256 id) public view override returns (string memory) {
        bytes memory buffer = new bytes(64);
        for (uint256 i = 0; i < 64; i++) buffer[63 - i] = _HEX_SYMBOLS[(id >> (i * 4)) & 0xf];
        return string(abi.encodePacked(_baseURI, string(buffer), ".json"));
    }

    // ========== 代币管理 ==========
    /**
     * @notice 批量铸造NFT
     * @dev 核心流程：
     *      1. 参数有效性验证（范围、前缀所有权）
     *      2. 位图冲突检查（按256分段验证）
     *      3. 生成连续ID数组
     *      4. 调用ERC1155的_mintBatch
     * @param to 接收地址
     * @param prefix 前缀编号，决定ID高位部分
     * @param start 起始序列号（含）
     * @param end 结束序列号（含）
     * @param canExpress 是否允许提货
     * @param isBlindBox 是否是盲盒
     */
    function mintBatchNFT(address to, uint256 prefix, uint256 start, uint256 end, bool canExpress, bool isBlindBox) external onlyRole(MERCHANT_ROLE) onlyUnFrozen() {
        // 参数有效性检查
        require(prefix > 0 && prefix < type(uint256).max / _MAX_TOKEN_SERIAL, "Invalid prefix");
        require(0 < start && start <= end && end < _MAX_TOKEN_SERIAL, "Invalid range");
        require(end - start + 1 <= _MAX_BATCH_NUM, "Exceeds batch limit");

        PrefixInfo storage info = _prefixInfos[prefix];

        // 前缀信息初始化
        if (info.minter == address(0)) {
            info.minter = msg.sender;
        } else {
            require(info.minter == msg.sender, "Prefix conflict");
        }
        info.canExpress = canExpress;
        info.isBlindBox = isBlindBox;

        // 批量铸造处理
        uint256 arrayLength = end - start + 1;
        uint256[] memory ids = new uint256[](arrayLength);
        uint256[] memory amounts = new uint256[](arrayLength);
        for (uint256 i = 0; i < arrayLength; i++) {
            // 位图
            uint256 serial = start + i;
            uint256 bucket = serial / 256;
            uint256 offset = serial % 256;
            require((info.bitmap[bucket] & (1 << offset)) == 0, "ID conflict");
            info.bitmap[bucket] |= (1 << offset);

            ids[i] = prefix * _MAX_TOKEN_SERIAL + serial;
            amounts[i] = 1;
        }

        _mintBatch(to, ids, amounts, "");

        emit MintBatchNFT(msg.sender, to, ids, amounts);
    }

    /**
     * @notice 修改前缀元权限
     * @dev 仅前缀铸造者可修改
     * @param prefixes 要修改的前缀数组
     * @param canExpresses 新的提货状态数组
     * @param isBlindBoxes 新的提货状态数组
     */
    function updatePrefixMeta(uint256[] calldata prefixes, bool[] calldata canExpresses, bool[] calldata isBlindBoxes) external onlyRole(MERCHANT_ROLE) onlyUnFrozen() {
        require(prefixes.length == canExpresses.length && prefixes.length == isBlindBoxes.length, "Length mismatch");

        for (uint256 i = 0; i < prefixes.length; ++i) {
            PrefixInfo storage info = _prefixInfos[prefixes[i]];
            require(info.minter == msg.sender, "Prefix conflict");
            info.canExpress = canExpresses[i];
            info.isBlindBox = isBlindBoxes[i];
        }

        emit UpdatePrefixMeta(msg.sender, prefixes, canExpresses, isBlindBoxes);
    }

    /**
     * @notice 提货
     * @dev 将NFT转回铸造者地址
     * @param ids 代币ID数组
     * @param detail 提货详情信息
     */
    function expressNFT(uint256[] memory ids, string calldata detail) external onlyUnFrozen() {
        PrefixInfo storage info = _validId(ids[0]);
        _expressNFT(msg.sender, info.minter, ids, detail, false);
    }

    /**
     * @notice 提货-商户
     * @dev 商户直接提货
     * @param account 用户账号
     * @param ids 代币ID数组
     * @param detail 提货详情信息
     */
    function expressNFTByMerchant(address account, uint256[] memory ids, string calldata detail) external onlyRole(MERCHANT_ROLE) onlyUnFrozen() {
        _expressNFT(account, msg.sender, ids, detail, true);
    }

    function _expressNFT(address from, address to, uint256[] memory ids, string calldata detail, bool isMerchant) private {
        require(ids.length > 0, "Empty ids");

        PrefixInfo storage tempInfo = _validId(ids[0]);
        uint256 tempPrefix = ids[0] / _MAX_TOKEN_SERIAL;

        uint256[] memory amounts = new uint256[](ids.length);
        for (uint256 i = 0; i < ids.length; ++i) {
            PrefixInfo storage info = _validId(ids[i]);
            if (isMerchant){
                require(info.minter == msg.sender, "Prefix conflict");
            }
            require(info.canExpress, "Express disabled");
            require(tempInfo.minter == info.minter && tempPrefix == ids[i] / _MAX_TOKEN_SERIAL, "Not same prefix NFT");

            amounts[i] = 1;
        }
        if (!isMerchant) {
            _safeBatchTransferFrom(from, to, ids, amounts, "");
        }

        emit ExpressNFT(from, to, ids, detail);
    }

    /**
     * @notice 开启盲盒
     * @dev 将NFT转回铸造者地址
     * @param id 代币ID
     */
    function openBlindBoxNFT(uint256 id) external onlyUnFrozen() {
        PrefixInfo storage info = _validId(id);
        require(info.isBlindBox, "Not blind box");

        _safeTransferFrom(msg.sender, info.minter, id, 1, "");

        emit OpenBlindBoxNFT(msg.sender, info.minter, id);
    }

    /**
     * @notice 多地址批量转账
     * @dev 每个目标地址对应一组代币ID
     * @param multiTxs 结构体数组
     */
    function multiBatchTransferNFT(MultiTX[] calldata multiTxs) external onlyUnFrozen {
        for (uint256 i = 0; i < multiTxs.length; i++) {
            MultiTX calldata mt = multiTxs[i];

            uint256[] memory amounts = new uint256[](mt.ids.length);
            for (uint256 j = 0; j < mt.ids.length; j++) amounts[j] = 1;

            _safeBatchTransferFrom(msg.sender, mt.to, mt.ids, amounts, "");
        }
    }

    // ========== 内部函数 ==========
    /**
     * @dev 设置基础URI
     * @param newBaseURI 新URI，自动补全末尾斜线
     */
    function _setBaseURI(string memory newBaseURI) private {
        bytes memory b = bytes(newBaseURI);
        if (b.length > 0 && b[b.length - 1] != "/") {
            newBaseURI = string(abi.encodePacked(newBaseURI, "/"));
        }
        _baseURI = newBaseURI;
    }

    /**
     * @dev 检测代币ID
     * @param id 代币ID
     * @return 前缀信息
     */
    function _validId(uint256 id) private view returns (PrefixInfo storage) {
        PrefixInfo storage info = _prefixInfos[id / _MAX_TOKEN_SERIAL];
        require(info.minter != address(0), "Invalid token");
        return info;
    }
}