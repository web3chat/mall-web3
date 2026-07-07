package com.fzm.mall.service;

import com.fzm.mall.constant.enums.CommonEnum;
import com.fzm.mall.constant.response.ResponseEnum;
import com.fzm.mall.entity.dataobject.UserExpressDO;
import com.fzm.mall.mapper.UserExpressMapper;
import com.fzm.mall.util.AssertUtils;
import com.fzm.mall.util.TimeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserExpressService {
    private final UserExpressMapper userExpressMapper;

    @Transactional(rollbackFor = Exception.class)
    public void addUpdate(UserExpressDO expressDO) {
        if (CommonEnum.BoolEnum.YES.getStatus() == expressDO.getStatus()) {
            userExpressMapper.updateStatusByAddress(expressDO.getAddress(), CommonEnum.BoolEnum.NO.getStatus());
        }

        UserExpressDO selectDO = getById(expressDO.getId());
        if (selectDO == null) {
            List<UserExpressDO> dos = userExpressMapper.listByAddress(expressDO.getAddress());
            AssertUtils.isTrue(dos.size() <= 25, ResponseEnum.maximum_limit_already_reached);
            expressDO.setCreateTime(TimeUtils.nowTimestamp());
            userExpressMapper.insert(expressDO);
        } else {
            userExpressMapper.update(expressDO);
        }
    }

    public void deleteById(Long id) {
        userExpressMapper.deleteById(id);
    }

    public UserExpressDO getById(Long id) {
        if (id == null) {
            return null;
        }
        return userExpressMapper.getById(id);
    }

    public List<UserExpressDO> listByAddress(String address) {
        return userExpressMapper.listByAddress(address);
    }
}
