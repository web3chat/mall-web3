package com.fzm.mall.third;

import com.fzm.mall.entity.properties.FileProperties;

public class FileClient {
    protected FileProperties.Properties properties;

    public FileClient(FileProperties.Properties properties) {
        this.properties = properties;
    }

    public String getHost() {
        return "https://" + properties.bucket() + "." + properties.endpoint();
    }
}
