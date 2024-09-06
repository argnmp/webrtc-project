package com.netproto.hycord.service;

import com.netproto.hycord.domain.RoomChat;
import com.netproto.hycord.domain.RoomInfo;
import com.netproto.hycord.dto.RoomRequestDto;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

public interface RoomService {
    public ArrayList<RoomInfo> getRooms();
    public Optional<RoomInfo> getRoom(UUID id);
    public RoomInfo createRoom(RoomRequestDto.Create dto);
    public Optional<RoomInfo> deleteRoom(UUID id);

    public Optional<RoomInfo> updateRoom(UUID id, RoomRequestDto.Update dto);

    public Optional<RoomChat> addRoomChat(UUID id, RoomRequestDto.Chat dto);
}
