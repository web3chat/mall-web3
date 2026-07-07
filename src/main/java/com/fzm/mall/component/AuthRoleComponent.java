package com.fzm.mall.component;

import com.fzm.mall.constant.enums.UserEnum;
import com.fzm.mall.entity.dataobject.AuthRoleDO;
import com.fzm.mall.entity.viewobject.back.MUserAdminVO;
import com.fzm.mall.mapper.AuthRoleMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AuthRoleComponent {
    private final AuthRoleMapper authRoleMapper;

    public void setRoleName(List<MUserAdminVO> vos) {
        if (CollectionUtils.isEmpty(vos)) {
            return;
        }

        List<Integer> childRoleList = vos.stream().map(MUserAdminVO::getChildRole).toList();
        List<AuthRoleDO> roleDOS = authRoleMapper.listByChildRoles(childRoleList);
        Map<Integer, AuthRoleDO> roleDOMap = roleDOS.stream().collect(Collectors.toMap(AuthRoleDO::getChildRole, o -> o));

        for (MUserAdminVO vo : vos) {
            UserEnum.RoleEnum roleEnum = UserEnum.RoleEnum.exist(vo.getRole());
            vo.setRoleName(roleEnum == null ? "ERROR" : roleEnum.getMessage());

            Integer childRole = vo.getChildRole();
            if (childRole == UserEnum.ChildRoleEnum.none.getRole()) {
                vo.setChildRoleName("-");
            } else {
                AuthRoleDO roleDO = roleDOMap.get(childRole);
                vo.setChildRoleName(roleDO == null ? "ERROR" : roleDO.getName());
            }
        }
    }

    public void setRoleName(MUserAdminVO vo) {
        if (vo == null) {
            return;
        }

        UserEnum.RoleEnum roleEnum = UserEnum.RoleEnum.exist(vo.getRole());
        vo.setRoleName(roleEnum == null ? "ERROR" : roleEnum.getMessage());

        Integer childRole = vo.getChildRole();
        if (childRole == UserEnum.ChildRoleEnum.none.getRole()) {
            vo.setChildRoleName("-");
        } else {
            AuthRoleDO roleDO = authRoleMapper.getByChildRole(childRole);
            vo.setChildRoleName(roleDO == null ? "ERROR" : roleDO.getName());
        }
    }

}
