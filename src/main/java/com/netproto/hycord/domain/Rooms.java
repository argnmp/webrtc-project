package com.netproto.hycord.domain;

import org.springframework.stereotype.Component;

import javax.swing.text.html.Option;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

@Component
public class Rooms {
    private final ArrayList<RoomInfo> currentRooms = new ArrayList<>();

    public synchronized ArrayList<RoomInfo> getRooms() {
        return currentRooms;
    }

    public synchronized Optional<RoomInfo> getRoom(UUID id) {
        return currentRooms.stream()
                .filter(roomInfo -> id.equals(roomInfo.getId()))
                .findAny();

    }

    public synchronized RoomInfo createRoom(String name, UUID userId) {
        RoomInfo s = new RoomInfo(UUID.randomUUID(), name, userId, new ArrayList<>());
        currentRooms.add(s);
        return s;
    }

    public synchronized Optional<RoomInfo> deleteRoom(UUID id) {
        Optional<RoomInfo> r = currentRooms.stream()
                .filter(roomInfo -> id.equals(roomInfo.getId()))
                .findAny();

        r.ifPresent(currentRooms::remove);

        return r;
    }

    public synchronized Optional<RoomInfo> updateRoom(UUID id, String name) {
        Optional<RoomInfo> r = currentRooms.stream()
                .filter(roomInfo -> id.equals(roomInfo.getId()))
                .findAny();

        r.ifPresent(roomInfo -> {
            roomInfo.setName(name);
        });

        return r;
    }

    public synchronized Optional<RoomInfo> addRoomChat(UUID id, RoomChat chat) {
        Optional<RoomInfo> r = currentRooms.stream()
                .filter(roomInfo -> id.equals(roomInfo.getId()))
                .findAny();
        r.ifPresent(roomInfo -> {
            roomInfo.getChats().add(chat);
        });

        return r;
    }
}

