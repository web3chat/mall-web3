package com.fzm.mall.third.component;

import com.alibaba.fastjson2.JSON;
import com.fzm.mall.constant.enums.FileEnum;
import com.fzm.mall.constant.response.ResponseEnum;
import com.fzm.mall.constant.response.ValidateException;
import com.fzm.mall.entity.properties.FileProperties;
import com.fzm.mall.mapper.SysFileMapper;
import com.fzm.mall.third.FileClient;
import com.fzm.mall.third.aliyun.AliOssServerClient;
import com.fzm.mall.third.amazon.AwsS3ServerClient;
import com.fzm.mall.util.AssertUtils;
import com.fzm.mall.util.TimeUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FileComponent {
    private final SysFileMapper sysFileMapper;
    private final FileProperties fileProperties;

    public String getHost() {
        if (fileProperties.getAliOss().enable()) {
            return new FileClient(fileProperties.getAliOss()).getHost() + "/";
        } else if (fileProperties.getAwsS3().enable()) {
            return new FileClient(fileProperties.getAwsS3()).getHost() + "/";
        } else {
            throw new ValidateException(ResponseEnum.file_upload_failed);
        }
    }

    public void verifyFileUrl(String fileUrl, FileEnum fileEnum, String message) {
        AssertUtils.isNotBlank(fileUrl, message);
        AssertUtils.isTrue(fileUrl.startsWith(getHost()), ResponseEnum.file_error);

        String[] split = fileUrl.split("\\.");
        AssertUtils.isTrue(split.length >= 2, message);

        boolean contains = fileEnum.getWhiteTypes().contains(split[split.length - 1]);
        AssertUtils.isTrue(contains, message);

        String selectUrl = sysFileMapper.getByFileUrl(fileUrl);
        AssertUtils.isNotBlank(selectUrl, ResponseEnum.file_not_upload);
    }

    public String upload(MultipartFile file) {
        AssertUtils.isNotNull(file, ResponseEnum.file_error);
        AssertUtils.isFalse(file.isEmpty(), ResponseEnum.file_error);

        String fileName = file.getOriginalFilename();
        AssertUtils.isNotBlank(fileName, ResponseEnum.invalid_file_name);

        String[] split = StringUtils.split(fileName, "\\.");
        AssertUtils.isFalse(split == null, ResponseEnum.file_error);
        String fileType = split[split.length - 1].toLowerCase();

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new ValidateException(ResponseEnum.file_error);
        }

        String hashName = DigestUtils.sha256Hex(bytes) + "." + fileType;
        String fileUrl = getHost() + hashName;
        String selectUrl = sysFileMapper.getByFileUrl(fileUrl);
        if (StringUtils.isNotBlank(selectUrl)) {
            return fileUrl;
        }

        upload(hashName, bytes);

        try {
            sysFileMapper.insert(fileUrl, TimeUtils.nowTimestamp());
        } catch (Exception ignored) {
        }

        return fileUrl;
    }

    public void uploadTokenUri(String hex, Object obj) {
        upload(hex + ".json", JSON.toJSONBytes(obj, StandardCharsets.UTF_8));
    }

    public void upload(String suffixName, byte[] fileBytes) {
        if (fileProperties.getAliOss().enable()) {
            new AliOssServerClient(fileProperties).upload(suffixName, fileBytes);
        } else if (fileProperties.getAwsS3().enable()) {
            new AwsS3ServerClient(fileProperties).upload(suffixName, fileBytes);
        } else {
            throw new ValidateException(ResponseEnum.file_upload_failed);
        }
    }
}
