package com.fzm.mall;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.fzm.mall.entity.dataobject.AuthPermissionDO;
import com.fzm.mall.mapper.AuthPermissionMapper;
import com.fzm.mall.util.HttpUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@SpringBootTest
@ActiveProfiles("dev")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class PermissionTests {
    private final AuthPermissionMapper authPermissionMapper;

    @Test
    void test() {
        String execute = HttpUtils.url("http://192.168.33.231:10010/v3/api-docs/%E5%90%8E%E5%8F%B0%E6%8E%A5%E5%8F%A3%E6%96%87%E6%A1%A3").execute();
        JSONObject paths = JSON.parseObject(execute).getJSONObject("paths");

        List<String> pathsList = paths.keySet().stream().filter(StringUtils::isNotBlank).toList();

        List<AuthPermissionDO> dos = authPermissionMapper.listAll();
        Map<String, AuthPermissionDO> doMap = dos.stream().filter(o -> StringUtils.isNotBlank(o.getUri())).collect(Collectors.toMap(AuthPermissionDO::getUri, o -> o));

        for (Map.Entry<String, Object> entry : paths.entrySet()) {
            AuthPermissionDO aDo = doMap.get(entry.getKey());
            if (aDo == null) {
                System.out.println(entry.getKey() + "    " + entry.getValue());
            }
        }

        System.out.println();
        System.out.println();

        for (AuthPermissionDO aDo : dos) {
            if (StringUtils.isNotBlank(aDo.getUri()) && !pathsList.contains(aDo.getUri())) {
                System.out.println(aDo);
            }
        }
    }

}
