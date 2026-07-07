// SPDX-License-Identifier: MIT
pragma solidity ^0.8.1;

contract FzmERC1155Web {

    function transferSuperAdmin(address account) external {}

    function setAdmin(address account, bool status) external {}

    function setMerchant(address account, bool status) external {}

    function freezeAccount(address account, bool status) external {}

    function expressNFT(uint256[] memory ids, string calldata detail) external {}

    function openBlindBoxNFT(uint256 id) external {}

    function uri(uint256) public view virtual returns (string memory) {}

    function balanceOf(address account, uint256 id) public view virtual returns (uint256) {}

    function safeTransferFrom(address from, address to, uint256 id, uint256 amount, bytes memory data) public virtual {}

    function safeBatchTransferFrom(address from, address to, uint256[] memory ids, uint256[] memory amounts, bytes memory data) public virtual {}

}