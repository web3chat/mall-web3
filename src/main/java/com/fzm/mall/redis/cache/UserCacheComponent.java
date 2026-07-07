package com.fzm.mall.redis.cache;

import com.fzm.mall.entity.dataobject.UserAdminDO;
import com.fzm.mall.entity.dataobject.UserDO;
import com.fzm.mall.redis.RedisCacheComponent;
import com.fzm.mall.redis.RedisCacheExpireEnum;
import com.fzm.mall.redis.key.RedisCacheKey;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserCacheComponent {
    private final RedisCacheComponent redisCacheComponent;

    // =============================================
    public void setAddressToUserDO(UserDO userDO) {
        String key = String.format(RedisCacheKey.User.front, userDO.getAddress());
        redisCacheComponent.setObj(key, userDO, RedisCacheExpireEnum.expire_days_7);
    }

    public UserDO getAddressToUserDO(String address) {
        String key = String.format(RedisCacheKey.User.front, address);
        try {
            return (UserDO) redisCacheComponent.getObj(key);
        } catch (Exception e) {
            delAddressToUserDO(address);
            return null;
        }
    }

    public void delAddressToUserDO(String address) {
        if (StringUtils.isBlank(address)) {
            return;
        }
        String key = String.format(RedisCacheKey.User.front, address);
        redisCacheComponent.del(key);
    }

    // =============================================
    public void setAddressToUserAdminDO(UserAdminDO userAdminDO) {
        String key = String.format(RedisCacheKey.User.back, userAdminDO.getAddress());
        redisCacheComponent.setObj(key, userAdminDO, RedisCacheExpireEnum.expire_days_7);
    }

    public UserAdminDO getAddressToUserAdminDO(String address) {
        String key = String.format(RedisCacheKey.User.back, address);
        try {
            return (UserAdminDO) redisCacheComponent.getObj(key);
        } catch (Exception e) {
            delAddressToUserAdminDO(address);
            return null;
        }
    }

    public void delAddressToUserAdminDO(String address) {
        if (StringUtils.isBlank(address)) {
            return;
        }
        String key = String.format(RedisCacheKey.User.back, address);
        redisCacheComponent.del(key);
    }

}
