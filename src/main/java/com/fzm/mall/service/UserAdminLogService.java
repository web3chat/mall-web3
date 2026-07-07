package com.fzm.mall.service;

import com.fzm.mall.entity.dataobject.UserAdminLogDO;
import com.fzm.mall.entity.queryobject.back.MUserAdminLogPageQO;
import com.fzm.mall.mapper.UserAdminLogMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserAdminLogService {
    private final UserAdminLogMapper userAdminLogMapper;

    public PageInfo<UserAdminLogDO> page(MUserAdminLogPageQO pageQO) {
        PageHelper.startPage(pageQO.getPage(), pageQO.getSize());
        List<UserAdminLogDO> dos = userAdminLogMapper.listByPageQO(pageQO);
        return new PageInfo<>(dos);
    }
}
