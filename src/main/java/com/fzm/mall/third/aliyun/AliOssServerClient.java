package com.fzm.mall.third.aliyun;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.fzm.mall.constant.response.ResponseEnum;
import com.fzm.mall.constant.response.ValidateException;
import com.fzm.mall.entity.properties.FileProperties;
import com.fzm.mall.third.FileClient;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;

@Slf4j
public class AliOssServerClient extends FileClient {


    public AliOssServerClient(FileProperties properties) {
        super(properties.getAliOss());
    }

    public void upload(String suffixName, byte[] fileBytes) {
        OSS client = new OSSClientBuilder().build(properties.endpoint(), properties.accessKeyId(), properties.accessKeySecret());

        try (ByteArrayInputStream in = new ByteArrayInputStream(fileBytes)) {
            client.putObject(properties.bucket(), suffixName, in);
        } catch (OSSException e) {
            log.error("阿里云OSS服务异常，错误码：{}，错误信息：{}", e.getErrorCode(), e.getErrorMessage());
            throw new ValidateException(ResponseEnum.file_upload_failed);
        } catch (Exception e) {
            log.error("阿里云OSS服务异常，错误信息：", e);
            throw new ValidateException(ResponseEnum.file_upload_failed);
        } finally {
            if (client != null) {
                client.shutdown();
            }
        }
    }
}
