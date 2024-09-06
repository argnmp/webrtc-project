package com.netproto.hycord.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class RoomInfo {
    private final UUID id;
    private String name;
    private UUID userId;
    private ArrayList<RoomChat> chats;
}
