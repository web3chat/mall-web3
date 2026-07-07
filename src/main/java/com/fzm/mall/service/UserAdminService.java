package com.fzm.mall.service;

import com.fzm.mall.constant.enums.CommonEnum;
import com.fzm.mall.constant.enums.UserEnum;
import com.fzm.mall.constant.response.ResponseEnum;
import com.fzm.mall.entity.common.ThreadInfo;
import com.fzm.mall.entity.dataobject.AuthRoleDO;
import com.fzm.mall.entity.dataobject.UserAdminDO;
import com.fzm.mall.entity.queryobject.back.MUserAdminPageQO;
import com.fzm.mall.mapper.AuthRoleMapper;
import com.fzm.mall.mapper.UserAdminMapper;
import com.fzm.mall.redis.cache.UserCacheComponent;
import com.fzm.mall.third.chain.entity.IndexEnum;
import com.fzm.mall.third.chain.entity.Wallet;
import com.fzm.mall.third.chain.wallet.ETHUtils;
import com.fzm.mall.util.AssertUtils;
import com.fzm.mall.util.TimeUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserAdminService {
    private final UserAdminMapper userAdminMapper;
    private final AuthRoleMapper authRoleMapper;
    private final UserCacheComponent userCacheComponent;

    @Transactional(rollbackFor = Exception.class)
    public void register(String address) {
        addOrUpdateUser(address, address, UserEnum.RoleEnum.none.getRole(), UserEnum.ChildRoleEnum.none.getRole(), UserEnum.ApplyStatusEnum.normal);
    }

    @Transactional(rollbackFor = Exception.class)
    public void addChild(String address, String parentAddress, int role, int childRole) {
        AuthRoleDO authRoleDO = authRoleMapper.getByChildRole(childRole);
        AssertUtils.isNotNull(authRoleDO, "角色编号错误");
        AssertUtils.isTrue(authRoleDO.getAddress().equals(parentAddress), "角色编号错误");

        addOrUpdateUser(address, parentAddress, role, childRole, UserEnum.ApplyStatusEnum.wait);
    }

    private void addOrUpdateUser(String address, String parentAddress, int role, int childRole, UserEnum.ApplyStatusEnum applyStatusEnum) {
        // 注册时，设置基本信息与区块链账户信息
        UserAdminDO updateAdminDO = new UserAdminDO();
        updateAdminDO.setAddress(address);
        updateAdminDO.setParentAddress(parentAddress);
        updateAdminDO.setStatus(UserEnum.StatusEnum.normal.getStatus());
        updateAdminDO.setRole(role);
        updateAdminDO.setChildRole(childRole);
        updateAdminDO.setNickname("");
        updateAdminDO.setHeadUrl("");
        updateAdminDO.setApplyStatus(applyStatusEnum.getStatus());
        updateAdminDO.setProfitSharing(BigDecimal.ZERO);

        UserAdminDO selectUserAdmin = getByAddress(address);
        // 新增
        if (selectUserAdmin == null) {
            updateAdminDO.setRegisterIp(ThreadInfo.getInfo().getClientIp());
            updateAdminDO.setRegisterTime(TimeUtils.nowTimestamp());

            int ok;
            try {
                ok = userAdminMapper.register(updateAdminDO);
            } catch (Exception e) {
                ok = 0;
            }
            AssertUtils.isTrue(ok == 1, ResponseEnum.too_many_requests_plz_try_again_later);

            Wallet wallet = ETHUtils.newWallet(updateAdminDO.getUid(), IndexEnum.back);
            updateAdminDO.setInsideAddress(wallet.address());
            updateAdminDO.setInsidePrivateKey(wallet.privateKey());
            ok = userAdminMapper.updateInsideByAddress(updateAdminDO);
            AssertUtils.isTrue(ok == 1, ResponseEnum.too_many_requests_plz_try_again_later);
        }
        // 编辑
        else {
            //
            AssertUtils.isTrue(selectUserAdmin.getStatus() == UserEnum.StatusEnum.normal.getStatus(), ResponseEnum.cannot_add_this_address);
            // 被添加地址如果没有分配过角色，则添加申请
            // 如果已经分配过角色，不能是顶级角色，上级地址不能是其他地址
            if (selectUserAdmin.getRole() != UserEnum.RoleEnum.none.getRole()) {
                AssertUtils.isFalse(selectUserAdmin._isTopRole(), ResponseEnum.cannot_add_this_address);
                AssertUtils.isTrue(selectUserAdmin.getParentAddress().equals(parentAddress), ResponseEnum.cannot_add_this_address);
                updateAdminDO.setApplyStatus(selectUserAdmin.getApplyStatus());
            }

            int ok = userAdminMapper.updateByAddress(updateAdminDO);
            AssertUtils.isTrue(ok == 1, ResponseEnum.too_many_requests_plz_try_again_later);
        }

        userCacheComponent.delAddressToUserAdminDO(address);
    }

    public void delChild(String address) {
        UserAdminDO userAdminDO = new UserAdminDO();
        userAdminDO.setAddress(address);
        userAdminDO.setParentAddress(address);
        userAdminDO.setRole(UserEnum.RoleEnum.none.getRole());
        userAdminDO.setChildRole(UserEnum.ChildRoleEnum.none.getRole());
        userAdminDO.setNickname("");
        userAdminDO.setHeadUrl("");
        userAdminDO.setApplyStatus(UserEnum.ApplyStatusEnum.normal.getStatus());
        userAdminDO.setProfitSharing(BigDecimal.ZERO);

        updateByAddress(userAdminDO);
    }

    public void childAudit(String address, CommonEnum.BoolEnum boolEnum) {
        if (boolEnum == CommonEnum.BoolEnum.NO) {
            delChild(address);
        } else {
            UserAdminDO userAdminDO = new UserAdminDO();
            userAdminDO.setAddress(address);
            userAdminDO.setApplyStatus(UserEnum.ApplyStatusEnum.normal.getStatus());

            updateByAddress(userAdminDO);
        }
    }

    public void updateByAddress(UserAdminDO userAdminDO) {
        int i = userAdminMapper.updateByAddress(userAdminDO);
        if (i == 1) {
            userCacheComponent.delAddressToUserAdminDO(userAdminDO.getAddress());
        }
        AssertUtils.isTrue(i == 1, ResponseEnum.too_many_requests_plz_try_again_later);
    }

    public UserAdminDO getByAddress(String address) {
        UserAdminDO userAdminDO = userCacheComponent.getAddressToUserAdminDO(address);
        if (userAdminDO != null) {
            return userAdminDO;
        }

        userAdminDO = userAdminMapper.getByAddress(address);
        if (userAdminDO != null) {
            userCacheComponent.setAddressToUserAdminDO(userAdminDO);
        }

        return userAdminDO;
    }

    public List<UserAdminDO> listByAddressList(List<String> addressList) {
        if (CollectionUtils.isEmpty(addressList)) {
            return Collections.emptyList();
        }
        return userAdminMapper.listByAddressList(addressList);
    }

    public PageInfo<UserAdminDO> page(MUserAdminPageQO pageQO) {
        PageHelper.startPage(pageQO.getPage(), pageQO.getSize());
        List<UserAdminDO> dos = userAdminMapper.listByPageQO(pageQO);
        return new PageInfo<>(dos);
    }
}
