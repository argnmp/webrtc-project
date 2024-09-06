package com.netproto.hycord.service;

import com.netproto.hycord.domain.RoomChat;
import com.netproto.hycord.domain.RoomInfo;
import com.netproto.hycord.domain.Rooms;
import com.netproto.hycord.dto.RoomRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {
    private final Rooms rooms;
    @Override
    public ArrayList<RoomInfo> getRooms() {
        return rooms.getRooms();
    }
    @Override
    public Optional<RoomInfo> getRoom(UUID id) {
        return rooms.getRoom(id);
    }
    @Override
    public RoomInfo createRoom(RoomRequestDto.Create dto) {
        return rooms.createRoom(dto.getName(), dto.getUserId());
    }
    @Override
    public Optional<RoomInfo> deleteRoom(UUID id) {
        return rooms.deleteRoom(id);
    }

    @Override
    public Optional<RoomInfo> updateRoom(UUID id, RoomRequestDto.Update dto){
        return rooms.updateRoom(id, dto.getName());
    }
    @Override
    public Optional<RoomChat> addRoomChat(UUID id, RoomRequestDto.Chat dto){
        RoomChat roomChat = new RoomChat(dto.getUserId(), dto.getName(), dto.getMessage(), LocalDateTime.now());
        Optional<RoomInfo> roomInfo= rooms.addRoomChat(id, roomChat);
        if(roomInfo.isPresent()) {
            return Optional.of(roomChat);
        }
        else {
            return Optional.empty();
        }
    }
}
