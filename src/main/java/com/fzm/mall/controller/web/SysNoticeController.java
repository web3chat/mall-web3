package com.fzm.mall.controller.web;

import com.fzm.mall.annotation.UnAuthorization;
import com.fzm.mall.constant.enums.CommonEnum;
import com.fzm.mall.constant.response.ResponseUtils;
import com.fzm.mall.constant.response.ResponseVO;
import com.fzm.mall.entity.common.PageVO;
import com.fzm.mall.entity.dataobject.SysNoticeDO;
import com.fzm.mall.entity.queryobject.back.MSysNoticeQO;
import com.fzm.mall.entity.viewobject.SysNoticeVO;
import com.fzm.mall.service.SysNoticeService;
import com.fzm.mall.util.BeanCopierUtils;
import com.fzm.mall.util.PageVOUtils;
import com.fzm.mall.util.TimeUtils;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "系统-公告")
@RestController
@UnAuthorization
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RequestMapping(value = "/v/sys/notice", produces = MediaType.APPLICATION_JSON_VALUE)
public class SysNoticeController {
    private final SysNoticeService sysNoticeService;

    @Operation(summary = "分页查询")
    @PostMapping("/page")
    public ResponseVO<PageVO<SysNoticeVO>> page(@RequestBody MSysNoticeQO pageQO) {
        pageQO.setStatus(CommonEnum.BoolEnum.YES.getStatus());
        pageQO.setNowDateTime(TimeUtils.nowTimestamp());
        PageInfo<SysNoticeDO> pageInfo = sysNoticeService.pageNotice(pageQO);
        List<SysNoticeVO> noticeVOS = pageInfo.getList().stream().map(o -> {
            SysNoticeVO noticeVO = BeanCopierUtils.copy(o, SysNoticeVO.class);
            noticeVO.setContent("");
            return noticeVO;
        }).toList();

        return PageVOUtils.pageVO(pageInfo, noticeVOS);
    }

    @Operation(summary = "详情")
    @Parameter(name = "id", description = "编号", in = ParameterIn.QUERY)
    @GetMapping("/detail")
    public ResponseVO<SysNoticeVO> detail(@RequestParam("id") Long id) {
        SysNoticeDO noticeDO = sysNoticeService.getById(id);
        SysNoticeVO noticeVO = BeanCopierUtils.copy(noticeDO, SysNoticeVO.class);

        return ResponseUtils.success(noticeVO);
    }
}
