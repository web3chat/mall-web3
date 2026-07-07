package com.fzm.mall.controller.manage;

import com.fzm.mall.constant.response.ResponseUtils;
import com.fzm.mall.constant.response.ResponseVO;
import com.fzm.mall.third.component.FileComponent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "系统-文件")
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RequestMapping(value = "/m/sys/file", produces = MediaType.APPLICATION_JSON_VALUE)
public class MSysFileController {
    private final FileComponent fileComponent;

    @Operation(summary = "上传")
    @PostMapping("/upload")
    public ResponseVO<String> upload(@RequestPart("file") MultipartFile file) {
        String fileUrl = fileComponent.upload(file);
        return ResponseUtils.success(fileUrl);
    }

}
