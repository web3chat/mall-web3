package com.fzm.mall.service;

import com.fzm.mall.constant.enums.UserEnum;
import com.fzm.mall.constant.response.ResponseEnum;
import com.fzm.mall.constant.response.ValidateException;
import com.fzm.mall.entity.common.ThreadInfo;
import com.fzm.mall.entity.dataobject.UserDO;
import com.fzm.mall.entity.queryobject.back.MUserPageQO;
import com.fzm.mall.mapper.UserMapper;
import com.fzm.mall.redis.cache.UserCacheComponent;
import com.fzm.mall.util.AssertUtils;
import com.fzm.mall.util.TimeUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserService {
    private final UserMapper userMapper;
    private final UserCacheComponent userCacheComponent;

    @Transactional(rollbackFor = Exception.class)
    public void register(String address, String parentAddress) {
        if (StringUtils.isBlank(parentAddress) || address.equals(parentAddress)) {
            parentAddress = "";
        }

        // 邀请人邀请数量+1
        if (StringUtils.isNotBlank(parentAddress)) {
            UserDO parentUserDO = userMapper.getByAddress(parentAddress);
            AssertUtils.isNotNull(parentUserDO, ResponseEnum.invalid_referral_address);
            int i = userMapper.addInviteNumByAddress(parentAddress, 1);
            AssertUtils.isTrue(i == 1, ResponseEnum.too_many_requests_plz_try_again_later);
        }

        // 新增
        UserDO userDO = new UserDO();
        userDO.setAddress(address);
        userDO.setParentAddress(parentAddress);
        userDO.setStatus(UserEnum.StatusEnum.normal.getStatus());
        userDO.setNickname(RandomStringUtils.secure().nextAlphanumeric(8));
        userDO.setHeadUrl("");
        userDO.setInviteNum(0);
        userDO.setRegisterIp(ThreadInfo.getInfo().getClientIp());
        userDO.setRegisterTime(TimeUtils.nowTimestamp());
        int i;
        try {
            i = userMapper.register(userDO);
        } catch (Exception e) {
            i = 0;
        }
        AssertUtils.isTrue(i == 1, ResponseEnum.too_many_requests_plz_try_again_later);

        userCacheComponent.delAddressToUserDO(address);
        userCacheComponent.delAddressToUserDO(parentAddress);
    }

    public void updateByAddress(UserDO userDO) {
        int i = userMapper.updateByAddress(userDO);
        if (i == 1) {
            userCacheComponent.delAddressToUserDO(userDO.getAddress());
        }
        AssertUtils.isTrue(i == 1, ResponseEnum.too_many_requests_plz_try_again_later);
    }

    /**
     * 修改用户邀请人
     *
     * @param address       用户地址
     * @param parentAddress 邀请人地址
     * @param isAccess      是否是登录
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateParentAddressByAddress(String address, String parentAddress, boolean isAccess) {
        if (StringUtils.isBlank(parentAddress)) {
            return;
        }
        // 邀请人地址和用户本身地址相同，如果是登录直接返回，如果不是抛出异常
        if (address.equals(parentAddress)) {
            if (isAccess) {
                return;
            } else {
                throw new ValidateException(ResponseEnum.referral_address_cannot_be_the_same_as_your_current_address);
            }
        }

        // 邀请人地址没有注册过，如果是登录直接返回，如果不是抛出异常
        UserDO parentUserDO = userMapper.getByAddress(parentAddress);
        if (parentUserDO == null) {
            if (isAccess) {
                return;
            } else {
                throw new ValidateException(ResponseEnum.referral_address_is_not_registered);
            }
        }

        // 邀请人地址和用户原本的邀请人地址相同，如果是登录直接返回，如果不是抛出异常
        UserDO userDO = userMapper.getByAddress(address);
        String oldParentAddress = userDO.getParentAddress();
        if (parentAddress.equals(oldParentAddress)) {
            if (isAccess) {
                return;
            } else {
                throw new ValidateException(ResponseEnum.invalid_referral_address);
            }
        }

        // 邀请人地址的上级地址和当前用户地址相同，如果是登录直接返回，如果不是抛出异常
        if (isCircularReference(address, parentUserDO)) {
            if (isAccess) {
                return;
            } else {
                throw new ValidateException(ResponseEnum.circular_reference_detected_in_referral_addresses);
            }
        }

        // 原邀请人地址不为空，需要修改原邀请人的邀请人数-1
        if (StringUtils.isNotBlank(oldParentAddress)) {
            int i = userMapper.addInviteNumByAddress(oldParentAddress, -1);
            AssertUtils.isTrue(i == 1, ResponseEnum.too_many_requests_plz_try_again_later);
        }

        // 新邀请人的邀请人数+1
        int i = userMapper.addInviteNumByAddress(parentAddress, 1);
        AssertUtils.isTrue(i == 1, ResponseEnum.invalid_referral_address);

        // 修改用户的邀请人信息
        UserDO updateUserDO = new UserDO();
        updateUserDO.setAddress(address);
        updateUserDO.setParentAddress(parentAddress);
        updateByAddress(updateUserDO);

        userCacheComponent.delAddressToUserDO(address);
        userCacheComponent.delAddressToUserDO(parentAddress);
        userCacheComponent.delAddressToUserDO(oldParentAddress);
    }

    public UserDO getByUid(long uid) {
        return userMapper.getByUid(uid);
    }

    public UserDO getByAddress(String address) {
        UserDO userDO = userCacheComponent.getAddressToUserDO(address);
        if (userDO != null) {
            return userDO;
        }

        userDO = userMapper.getByAddress(address);
        if (userDO != null) {
            userCacheComponent.setAddressToUserDO(userDO);
        }

        return userDO;
    }

    public PageInfo<UserDO> page(MUserPageQO pageQO) {
        PageHelper.startPage(pageQO.getPage(), pageQO.getSize());
        List<UserDO> dos = userMapper.listByPageQO(pageQO);
        return new PageInfo<>(dos);
    }


    private boolean isCircularReference(String address, UserDO parentUserDO) {
        if (StringUtils.isBlank(parentUserDO.getParentAddress())) {
            return false;
        }
        if (address.equals(parentUserDO.getParentAddress())) {
            return true;
        }

        parentUserDO = userMapper.getByAddress(parentUserDO.getParentAddress());
        return isCircularReference(address, parentUserDO);
    }
}
