package com.netproto.hycord.dto;

import lombok.Data;

public class UserRequestDto {
    @Data
    public static class CREATE {
        private String name;
    }
}
