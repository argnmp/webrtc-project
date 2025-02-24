package com.netproto.hycord.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class RoomChat {
    private final UUID userId;
    private final String name;
    private final String message;
    private final LocalDateTime timestamp;
}
