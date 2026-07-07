package com.fzm.mall.constant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

public class UserEnum {

    @Getter
    @AllArgsConstructor
    public enum StatusEnum {
        frozen(0, "冻结"),
        normal(1, "正常"),
        ;
        private final int status;
        private final String message;

        public static StatusEnum exist(Integer status) {
            if (status == null) {
                return null;
            }
            return Arrays.stream(values()).filter(v -> v.getStatus() == status).findFirst().orElse(null);
        }
    }


    @Getter
    @AllArgsConstructor
    public enum RoleEnum {
        none(0, "无"),
        admin(1, "管理员"),
        merchant(2, "商户"),
        ;
        private final int role;
        private final String message;

        public static RoleEnum exist(Integer role) {
            if (role == null) {
                return null;
            }
            return Arrays.stream(values()).filter(v -> v.getRole() == role).findFirst().orElse(null);
        }
    }

    @Getter
    @AllArgsConstructor
    public enum ChildRoleEnum {
        none(0, "无"),
        ;
        private final int role;
        private final String message;
    }

    @Getter
    @AllArgsConstructor
    public enum ApplyStatusEnum {
        normal(0, "初始状态"),
        wait(1, "商户添加子账号，待同意"),
        ;
        private final int status;
        private final String message;
    }

}
