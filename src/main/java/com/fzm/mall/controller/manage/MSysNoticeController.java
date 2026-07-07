package com.fzm.mall.controller.manage;

import com.alibaba.fastjson2.JSON;
import com.fzm.mall.constant.enums.CommonEnum;
import com.fzm.mall.constant.enums.FileEnum;
import com.fzm.mall.constant.response.ResponseUtils;
import com.fzm.mall.constant.response.ResponseVO;
import com.fzm.mall.entity.common.PageVO;
import com.fzm.mall.entity.dataobject.SysNoticeDO;
import com.fzm.mall.entity.queryobject.back.MSysNoticeQO;
import com.fzm.mall.entity.requestobject.LongIdRO;
import com.fzm.mall.entity.requestobject.back.MLongIdStatusRO;
import com.fzm.mall.entity.requestobject.back.MSysNoticeRO;
import com.fzm.mall.entity.viewobject.back.MSysNoticeVO;
import com.fzm.mall.service.SysNoticeService;
import com.fzm.mall.third.component.FileComponent;
import com.fzm.mall.util.AssertUtils;
import com.fzm.mall.util.BeanCopierUtils;
import com.fzm.mall.util.PageVOUtils;
import com.fzm.mall.util.ParamsUtils;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "系统-公告")
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RequestMapping(value = "/m/sys/notice", produces = MediaType.APPLICATION_JSON_VALUE)
public class MSysNoticeController {
    private final SysNoticeService sysNoticeService;
    private final FileComponent fileComponent;

    @Operation(summary = "分页查询")
    @PostMapping("/page")
    public ResponseVO<PageVO<MSysNoticeVO>> page(@RequestBody MSysNoticeQO pageQO) {
        PageInfo<SysNoticeDO> pageInfo = sysNoticeService.pageNotice(pageQO);
        return PageVOUtils.pageVO(pageInfo, MSysNoticeVO.class);
    }

    @Operation(summary = "详情")
    @Parameter(name = "id", description = "编号", in = ParameterIn.QUERY)
    @GetMapping("/detail")
    public ResponseVO<MSysNoticeVO> detail(@RequestParam("id") Long id) {
        SysNoticeDO noticeDO = sysNoticeService.getById(id);
        MSysNoticeVO noticeVO = BeanCopierUtils.copy(noticeDO, MSysNoticeVO.class);

        return ResponseUtils.success(noticeVO);
    }

    @Operation(summary = "新增/编辑")
    @PostMapping("/add-update")
    public ResponseVO<Object> addUpdate(@RequestBody MSysNoticeRO noticeRO) {
        List<Integer> classifies = noticeRO.getClassifies();
        // TODO 校验分类

        String title = noticeRO.getTitle();
        AssertUtils.isNotBlank(title, "title为空");
        AssertUtils.isTrue(title.length() <= 32, "title最多只能32个字");

        String content = noticeRO.getContent();
        AssertUtils.isNotBlank(content, "content为空");

        if (StringUtils.isBlank(noticeRO.getCover())) {
            noticeRO.setCover("");
        } else {
            fileComponent.verifyFileUrl(noticeRO.getCover(), FileEnum.image, "cover只能使用图片");
        }

        AssertUtils.isNotNull(CommonEnum.BoolEnum.exist(noticeRO.getTop()), "top错误");
        AssertUtils.isNotNull(CommonEnum.BoolEnum.exist(noticeRO.getScroll()), "scroll错误");

        AssertUtils.isNotNull(noticeRO.getActiveTime(), "activeTime为空");

        SysNoticeDO noticeDO = BeanCopierUtils.copy(noticeRO, SysNoticeDO.class);
        noticeDO.setClassifyJson(JSON.toJSONString(noticeRO.getClassifies()));

        sysNoticeService.addOrUpdate(noticeDO);

        return ResponseUtils.success();
    }

    @Operation(summary = "上下架")
    @PostMapping("/status")
    public ResponseVO<Object> status(@RequestBody MLongIdStatusRO statusRO) {
        AssertUtils.isNotNull(CommonEnum.BoolEnum.exist(statusRO.getStatus()), "status错误");
        AssertUtils.isNotNull(statusRO.getId(), "id错误");

        SysNoticeDO noticeDO = BeanCopierUtils.copy(statusRO, SysNoticeDO.class);

        sysNoticeService.updateById(noticeDO);

        return ResponseUtils.success();
    }

    @Operation(summary = "删除")
    @PostMapping("/delete")
    public ResponseVO<Object> delete(@RequestBody LongIdRO idRO) {
        ParamsUtils.positiveLong(idRO.getId());

        sysNoticeService.deleteById(idRO.getId());

        return ResponseUtils.success();
    }
}
