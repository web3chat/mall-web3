package com.fzm.mall.constant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
@AllArgsConstructor
public enum FileEnum {
    image(List.of("jpg", "jpeg", "png", "gif")),
    video(Collections.singletonList("mp4")),
    any(null),
    ;

    private final List<String> whiteTypes;
}
