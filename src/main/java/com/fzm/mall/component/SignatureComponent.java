package com.fzm.mall.component;

import com.fzm.mall.entity.properties.SiweProperties;
import com.fzm.mall.redis.RedisCacheExpireEnum;
import com.fzm.mall.redis.cache.SiweMessageCache;
import com.fzm.mall.third.chain.wallet.ETHUtils;
import com.fzm.mall.third.siwe.SiweMessageBuilder;
import com.fzm.mall.util.AssertUtils;
import com.fzm.mall.util.TimeUtils;
import com.fzm.mall.util.Validator;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SignatureComponent {
    private final SiweMessageCache siweMessageCache;
    private final SiweProperties siweProperties;

    public String createChallenge(String address, String inviteAddress) {
        AssertUtils.isTrue(Validator.isETHAddress(address), "只支持0x系列地址");
        address = address.toLowerCase();
        if (StringUtils.isNotBlank(inviteAddress)) {
            AssertUtils.isTrue(Validator.isETHAddress(inviteAddress), "只支持0x系列地址");
            inviteAddress = inviteAddress.toLowerCase();
        }

        OffsetDateTime now = TimeUtils.nowDateTime();
        String nonce = RandomStringUtils.secure().nextNumeric(12);
        OffsetDateTime expireAt = now.plusMinutes(RedisCacheExpireEnum.expire_minutes_30.getTimeout());

        String content = SiweMessageBuilder.build(siweProperties.getDomain(), address, inviteAddress, nonce, now, expireAt, TimeUtils.RFC3339);

        siweMessageCache.save(address, content);
        return content;
    }

    public String verifyAndGetInviteCode(String address, String signature) {
        AssertUtils.isTrue(Validator.isETHAddress(address), "只支持0x系列地址");
        address = address.toLowerCase();

        String cachedContent = verifyAndConsume(address, signature);
        return StringUtils.defaultString(extractInviteCode(cachedContent));
    }

    private String verifyAndConsume(String address, String signature) {
        String cachedContent = siweMessageCache.getAndConsume(address);
        AssertUtils.isNotBlank(cachedContent, "签名原文已过期");
        verifySignature(address, cachedContent, signature);
        return cachedContent;
    }

    private void verifySignature(String address, String content, String signature) {
        String recoveredAddress = ETHUtils.recoverAddressFromSignature(content, signature);
        AssertUtils.isNotBlank(recoveredAddress, "签名校验失败");

        boolean match = address.equals(recoveredAddress);
        AssertUtils.isTrue(match, "签名校验失败");
    }

    private String extractInviteCode(String content) {
        String prefix = SiweMessageBuilder.INVITE_CODE_FIELD;
        for (String line : content.split("\n")) {
            if (line.startsWith(prefix)) {
                return line.substring(prefix.length()).trim();
            }
        }
        return "";
    }
}
