package com.fzm.mall.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

public class EscapeSqlDeserializer {
    public static class StringDeserializer extends JsonDeserializer<String> {
        @Override
        public String deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            String input = jsonParser.getValueAsString();
            if (StringUtils.isBlank(input)) {
                return "";
            }

            if (input.contains("_") || input.contains("%") || input.contains("'") || input.contains("\\")) {
                StringBuilder sb = new StringBuilder();
                char[] chars = input.toCharArray();
                for (char c : chars) {
                    if (c == '_' || c == '%' || c == '\'' || c == '\\') {
                        sb.append("\\");
                    }
                    sb.append(c);
                }
                input = sb.toString();
            }

            return input;
        }
    }
}
