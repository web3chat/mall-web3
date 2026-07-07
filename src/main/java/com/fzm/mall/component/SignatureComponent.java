package com.fzm.mall.component;

import com.fzm.mall.constant.response.ValidateException;
import com.fzm.mall.entity.properties.SiweProperties;
import com.fzm.mall.entity.requestobject.LoginRO;
import com.fzm.mall.redis.RedisCacheExpireEnum;
import com.fzm.mall.redis.cache.SiweCacheComponent;
import com.fzm.mall.util.AssertUtils;
import com.fzm.mall.util.TimeUtils;
import com.fzm.mall.util.Validator;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SignatureComponent {
    // SIWE 消息字段常量
    private static final String version_field = "版本: 1";
    private static final String nonce_field = "随机数: ";
    private static final String invite_address_field = "邀请地址: ";
    private static final String issued_at_field = "签发时间: ";
    private static final String expire_at_field = "过期时间: ";

    private final SiweCacheComponent siweCacheComponent;
    private final SiweProperties siweProperties;

    private String domainField() {
        return siweProperties.getDomain() + " 想要您使用以太坊账户签名（签名不会消耗Gas费用）: ";
    }

    private String tofField() {
        return "请签署此消息以验证您对该地址的控制权，并接受我们的服务条款: https://" + siweProperties.getDomain() + "/tos";
    }

    private String ppField() {
        return "和隐私政策: https://" + siweProperties.getDomain() + "/privacy-policy";
    }

    private String uriField() {
        return "网站: https://" + siweProperties.getDomain() + "/";
    }

    public String getPresignContent(String address, String inviteAddress) {
        AssertUtils.isTrue(Validator.isETHAddress(address), "只支持0x系列地址");
        address = address.toLowerCase();
        if (StringUtils.isNotBlank(inviteAddress)) {
            AssertUtils.isTrue(Validator.isETHAddress(inviteAddress), "只支持0x系列地址");
            inviteAddress = inviteAddress.toLowerCase();
        }

        OffsetDateTime nowDateTime = TimeUtils.nowDateTime();
        String nonce = DigestUtils.md5Hex(UUID.randomUUID().toString());

        String content = domainField() + "\n" +
                address + "\n" +
                "\n" +
                tofField() + "\n" +
                ppField() + "\n" +
                "\n" +
                uriField() + "\n" +
                version_field + "\n" +
                nonce_field + nonce + "\n" +
                (StringUtils.isBlank(inviteAddress) ? "" : invite_address_field + inviteAddress) + "\n" +
                issued_at_field + nowDateTime.format(TimeUtils.RFC3339) + "\n" +
                expire_at_field + nowDateTime.plusMinutes(RedisCacheExpireEnum.expire_minutes_30.getTimeout()).format(TimeUtils.RFC3339) + "\n";

        siweCacheComponent.setNonce(nonce, content);

        return content;
    }

    public String valid(LoginRO loginRO) {
        String inviteAddress = valid(loginRO.getAddress(), loginRO.getContent(), loginRO.getSignature());
        loginRO.setAddress(loginRO.getAddress().toLowerCase());

        return inviteAddress;
    }

    public String valid(String address, String content, String hexSignature) {
        AssertUtils.isTrue(Validator.isETHAddress(address), "只支持0x系列地址");
        address = address.toLowerCase();
        AssertUtils.isNotBlank(content, "原始信息错误");
        AssertUtils.isNotBlank(hexSignature, "签名信息错误");

        String inviteAddress = validContent(address, content);

        try {
            Sign.SignatureData signatureData = Sign.signatureDataFromHex(hexSignature);
            // 恢复公钥
            BigInteger publicKey = Sign.signedPrefixedMessageToKey(content.getBytes(StandardCharsets.UTF_8), signatureData);
            // 生成地址
            String signAddress = Numeric.prependHexPrefix(Keys.getAddress(publicKey));
            AssertUtils.isTrue(signAddress.equals(address), "签名错误");

            return inviteAddress;
        } catch (Exception ignored) {
        }

        throw new ValidateException("校验签名错误");
    }

    public String sign(String content, String privateKey) {
        try {
            Credentials credentials = Credentials.create(privateKey);
            Sign.SignatureData signature = Sign.signPrefixedMessage(content.getBytes(StandardCharsets.UTF_8), credentials.getEcKeyPair());

            byte[] r = signature.getR();
            byte[] s = signature.getS();
            byte[] v = signature.getV();

            byte[] signatureBytes = new byte[65];
            System.arraycopy(r, 0, signatureBytes, 0, 32);
            System.arraycopy(s, 0, signatureBytes, 32, 32);
            signatureBytes[64] = v[0];

            return Numeric.toHexString(signatureBytes);
        } catch (Exception e) {
            throw new ValidateException("生成签名异常");
        }
    }

    private String validContent(String address, String content) {
        try {
            String[] lines = content.split("\n");
            int lineIdx = 0;

            // 域名
            AssertUtils.isTrue(lines[lineIdx++].equals(domainField()), "原始信息非法");
            // 地址
            AssertUtils.isTrue(lines[lineIdx++].equals(address), "原始信息非法");
            // 空行
            AssertUtils.isBlank(lines[lineIdx++], "原始信息非法");
            // 服务条款
            AssertUtils.isTrue(lines[lineIdx++].equals(tofField()), "原始信息非法");
            // 隐私政策
            AssertUtils.isTrue(lines[lineIdx++].equals(ppField()), "原始信息非法");
            // 空行
            AssertUtils.isBlank(lines[lineIdx++], "原始信息非法");
            // 登录URI
            AssertUtils.isTrue(lines[lineIdx++].equals(uriField()), "原始信息非法");
            // 版本号
            AssertUtils.isTrue(lines[lineIdx++].equals(version_field), "原始信息非法");
            // Nonce
            String nonceLine = lines[lineIdx++];
            AssertUtils.isTrue(nonceLine.startsWith(nonce_field), "原始信息非法");
            String nonce = nonceLine.replace(nonce_field, "");
            AssertUtils.isNotBlank(nonce, "原始信息非法");
            String nonceCache = siweCacheComponent.getNonce(nonce);
            AssertUtils.isNotBlank(nonceCache, "原始信息过期");
            AssertUtils.isTrue(nonceCache.equals(content), "原始信息非法");
            siweCacheComponent.delNonce(nonce);

            // 邀请地址
            String inviteAddress = "";
            String inviteAddressLine = lines[lineIdx++];
            if (StringUtils.isNotBlank(inviteAddressLine)) {
                AssertUtils.isTrue(inviteAddressLine.startsWith(invite_address_field), "原始信息非法");
                inviteAddress = inviteAddressLine.replace(invite_address_field, "");
            }

            OffsetDateTime nowDateTime = TimeUtils.nowDateTime();
            // 创建时间
            String issuedLine = lines[lineIdx++];
            AssertUtils.isTrue(issuedLine.startsWith(issued_at_field), "原始信息非法");
            String issuedAt = issuedLine.replace(issued_at_field, "");
            AssertUtils.isNotBlank(issuedAt, "原始信息非法");
            OffsetDateTime issuedDateTime = TimeUtils.RFC3339ToDateTime(issuedAt);
            AssertUtils.isTrue(issuedDateTime.isBefore(nowDateTime), "原始信息非法");
            // 过期时间
            String expirationLine = lines[lineIdx];
            AssertUtils.isTrue(expirationLine.startsWith(expire_at_field), "原始信息非法");
            String expirationAt = expirationLine.replace(expire_at_field, "");
            AssertUtils.isNotBlank(expirationAt, "原始信息非法");
            OffsetDateTime expirationTime = TimeUtils.RFC3339ToDateTime(expirationAt);
            AssertUtils.isTrue(expirationTime.isAfter(nowDateTime), "原始信息过期");

            return inviteAddress;
        } catch (ValidateException e) {
            throw e;
        } catch (Exception e) {
            throw new ValidateException("原始信息非法");
        }

    }
}
