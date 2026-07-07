package com.fzm.mall.configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.math.BigDecimal;

@Configuration
public class JacksonConfig {
    private static final ObjectMapper mapper = new ObjectMapper();

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> builder
                // 注册 Java 8 时间模块（处理 LocalDate、LocalDateTime 等）
                .modulesToInstall(new JavaTimeModule())
                // 禁用时间戳格式
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                // 反序列化时忽略未知字段（不报错）
                .featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                // 全局忽略 null 字段
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                // 避免精度丢失或科学计数法的问题
//                .serializerByType(Long.class, ToStringSerializer.instance)
//                .serializerByType(Long.TYPE, ToStringSerializer.instance)
//                .serializerByType(Float.class, ToStringSerializer.instance)
//                .serializerByType(Float.TYPE, ToStringSerializer.instance)
//                .serializerByType(Double.class, ToStringSerializer.instance)
//                .serializerByType(Double.TYPE, ToStringSerializer.instance)
//                .serializerByType(BigInteger.class, ToStringSerializer.instance)
                // 其他自定义配置
                .modulesToInstall(customModule());
    }

    private SimpleModule customModule() {
        SimpleModule module = new SimpleModule();

        // =========================== 序列化配置 ===========================
        module.addSerializer(BigDecimal.class, new JsonSerializer<>() {
            @Override
            public void serialize(BigDecimal bigDecimal, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
                bigDecimal = bigDecimal == null ? BigDecimal.ZERO : bigDecimal;
                jsonGenerator.writeString(bigDecimal.stripTrailingZeros().toPlainString());
            }
        });

        module.addSerializer(String.class, new JsonSerializer<>() {
            @Override
            public void serialize(String s, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
                if (s == null || s.isEmpty()) {
                    jsonGenerator.writeString("");
                    return;
                }
                if (s.charAt(0) != '{' && s.charAt(0) != '[') {
                    jsonGenerator.writeString(s);
                    return;
                }
                try {
                    jsonGenerator.writeObject(mapper.readTree(s));
                } catch (Exception e) {
                    jsonGenerator.writeString(s);
                }
            }
        });

        // =========================== 反序列化配置 ===========================
//        module.addDeserializer(String.class, new JsonDeserializer<>() {
//            @Override
//            public String deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
//                String s = jsonParser.getValueAsString();
//                if (s == null || s.isEmpty()) {
//                    return "";
//                }
//
//                StringBuilder sb = new StringBuilder();
//                for (char c : s.toCharArray()) {
//                    if (c == '_' || c == '%' || c == '\'' || c == '\\') {
//                        sb.append("\\");
//                    }
//                    sb.append(c);
//                }
//
//                return sb.toString();
//            }
//        });

        return module;
    }
}
