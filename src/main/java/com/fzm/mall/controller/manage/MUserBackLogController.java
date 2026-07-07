package com.fzm.mall.controller.manage;

import com.fzm.mall.constant.enums.UserEnum;
import com.fzm.mall.constant.response.ResponseVO;
import com.fzm.mall.entity.common.PageVO;
import com.fzm.mall.entity.common.ThreadInfo;
import com.fzm.mall.entity.dataobject.UserAdminLogDO;
import com.fzm.mall.entity.queryobject.back.MUserAdminLogPageQO;
import com.fzm.mall.entity.viewobject.back.MUserAdminLogVO;
import com.fzm.mall.service.UserAdminLogService;
import com.fzm.mall.util.PageVOUtils;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用户-后台日志")
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RequestMapping(value = "/m/user/back/log", produces = MediaType.APPLICATION_JSON_VALUE)
public class MUserBackLogController {
    private final UserAdminLogService userAdminLogService;

    @Operation(summary = "分页查询")
    @PostMapping("/page")
    public ResponseVO<PageVO<MUserAdminLogVO>> page(@RequestBody MUserAdminLogPageQO pageQO) {
        if (ThreadInfo.getInfo().getRole() == UserEnum.RoleEnum.merchant.getRole()) {
            pageQO.setParentAddress(ThreadInfo.getInfo().getParentAddress());
        }

        PageInfo<UserAdminLogDO> pageInfo = userAdminLogService.page(pageQO);
        for (UserAdminLogDO logDO : pageInfo.getList()) {
            if (StringUtils.isBlank(logDO.getUriName())) {
                try {
                    logDO.setUriName(logDO.getUri().substring(logDO.getUri().lastIndexOf("/") + 1));
                } catch (Exception e) {
                    logDO.setUriName(logDO.getUri());
                }
            }
        }

        return PageVOUtils.pageVO(pageInfo, MUserAdminLogVO.class);
    }
}
