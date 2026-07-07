package com.fzm.mall.service;

import com.fzm.mall.entity.common.ReqInfo;
import com.fzm.mall.entity.dataobject.AuthPermissionDO;
import com.fzm.mall.entity.dataobject.AuthRoleDO;
import com.fzm.mall.entity.viewobject.back.MAuthPermissionTreeVO;
import com.fzm.mall.mapper.AuthPermissionMapper;
import com.fzm.mall.mapper.AuthRoleMapper;
import com.fzm.mall.mapper.AuthRolePermissionMapper;
import com.fzm.mall.util.AssertUtils;
import com.fzm.mall.util.BeanCopierUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AuthService {
    private final AuthPermissionMapper authPermissionMapper;
    private final AuthRoleMapper authRoleMapper;
    private final AuthRolePermissionMapper authRolePermissionMapper;

    public void addOrUpdateRole(String address, Integer childRole, String name) {
        AuthRoleDO selectRoleDO = getRoleByChildRole(childRole);
        if (selectRoleDO == null) {
            authRoleMapper.insert(address, name);
        } else {
            AssertUtils.isTrue(selectRoleDO.getAddress().equals(address), "角色编号错误");
            authRoleMapper.updateByChildRole(childRole, name);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteRoleByChildRole(Integer childRole) {
        authRoleMapper.deleteByChildRole(childRole);
        authRolePermissionMapper.deleteByChildRole(childRole);
    }

    public AuthRoleDO getRoleByChildRole(Integer childRole) {
        if (childRole == null || childRole <= 0) {
            return null;
        }
        return authRoleMapper.getByChildRole(childRole);
    }

    public List<AuthRoleDO> listRoleByAddress(String address) {
        return authRoleMapper.listByAddress(address);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateChildRolePermission(Integer role, Integer childRole, List<Integer> permissionIds) {
        if (CollectionUtils.isNotEmpty(permissionIds)) {
            List<AuthPermissionDO> allPermissionDOS = authPermissionMapper.listAll();
            // 筛选出对应角色的所有2级权限
            Set<Integer> levelTwoIds = allPermissionDOS.stream()
                    .filter(o -> o.getRole() == 0 || o.getRole().equals(role))
                    .filter(o -> o.getLevel() == 2)
                    .map(AuthPermissionDO::getId).collect(Collectors.toSet());
            permissionIds = permissionIds.stream().filter(levelTwoIds::contains).distinct().collect(Collectors.toList());
        }
        authRolePermissionMapper.deleteByChildRole(childRole);
        if (CollectionUtils.isNotEmpty(permissionIds)) {
            authRolePermissionMapper.insertBatch(childRole, permissionIds);
        }

    }

    public List<MAuthPermissionTreeVO> getPermissionTree(ReqInfo reqInfo, Integer queryChildRole) {
        boolean isCurrent = queryChildRole == null;
        // 所有权限
        List<AuthPermissionDO> allPermissionDOS = authPermissionMapper.listAll();
        allPermissionDOS = allPermissionDOS.stream().filter(o -> o.getRole() == 0 || o.getRole().equals(reqInfo.getRole())).toList();
        // 按父权限分类
        LinkedHashMap<Integer, List<AuthPermissionDO>> allPermissionDOSGroupMap = allPermissionDOS.stream().collect(Collectors.groupingBy(AuthPermissionDO::getParentId, LinkedHashMap::new, Collectors.toList()));

        //
        List<AuthPermissionDO> permissionDOS;
        if (isCurrent) {
            // 顶级角色查询所有权限，子角色查询分配的权限
            if (reqInfo._isTopRole()) {
                permissionDOS = allPermissionDOS;
            } else {
                permissionDOS = authRolePermissionMapper.listByChildRole(reqInfo.getChildRole());
            }
        } else {
            permissionDOS = authRolePermissionMapper.listByChildRole(queryChildRole);
        }
        List<Integer> chileRolePermissionIdS = permissionDOS.stream().map(AuthPermissionDO::getId).toList();


        List<AuthPermissionDO> levelZeroDOS = allPermissionDOSGroupMap.getOrDefault(0, Collections.emptyList());
        List<MAuthPermissionTreeVO> levelZeroVOS = new ArrayList<>(levelZeroDOS.size());
        for (AuthPermissionDO levelZeroDO : levelZeroDOS) {
            MAuthPermissionTreeVO levelZeroVO = BeanCopierUtils.copy(levelZeroDO, MAuthPermissionTreeVO.class);

            List<AuthPermissionDO> levelOneDOS = allPermissionDOSGroupMap.getOrDefault(levelZeroDO.getId(), Collections.emptyList());
            List<MAuthPermissionTreeVO> levelOneVOS = new ArrayList<>(levelOneDOS.size());
            for (AuthPermissionDO levelOneDO : levelOneDOS) {
                MAuthPermissionTreeVO levelOneVO = BeanCopierUtils.copy(levelOneDO, MAuthPermissionTreeVO.class);

                List<AuthPermissionDO> levelTwoDOS = allPermissionDOSGroupMap.getOrDefault(levelOneDO.getId(), Collections.emptyList());
                List<MAuthPermissionTreeVO> levelTwoVOS = new ArrayList<>(levelTwoDOS.size());
                for (AuthPermissionDO levelTwoDO : levelTwoDOS) {
                    if (!isCurrent || chileRolePermissionIdS.contains(levelTwoDO.getId())) {
                        MAuthPermissionTreeVO levelTwoVO = BeanCopierUtils.copy(levelTwoDO, MAuthPermissionTreeVO.class);
                        levelTwoVO.setChecked(chileRolePermissionIdS.contains(levelTwoDO.getId()));
                        levelTwoVOS.add(levelTwoVO);
                    }
                }
                if (CollectionUtils.isNotEmpty(levelTwoVOS)) {
                    levelOneVO.setTree(levelTwoVOS);
                    levelOneVOS.add(levelOneVO);
                }
            }
            if (CollectionUtils.isNotEmpty(levelOneVOS)) {
                levelZeroVO.setTree(levelOneVOS);
                levelZeroVOS.add(levelZeroVO);
            }
        }

        return levelZeroVOS;
    }
}
