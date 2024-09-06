package com.netproto.hycord.controller;

import com.netproto.hycord.domain.RoomInfo;
import com.netproto.hycord.dto.CommonRoomMessageDto;
import com.netproto.hycord.dto.CommonRoomMessageType;
import com.netproto.hycord.dto.RoomRequestDto;
import com.netproto.hycord.dto.RoomResponseDto;
import com.netproto.hycord.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/")
@CrossOrigin(origins = "*")
public class RoomController {
    private final RoomService roomService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @GetMapping(value="/room")
    @Operation(summary = "채널 목록을 반환", description = "개설된 채널의 id와 채널 이름을 반환합니다.")
    public List<RoomResponseDto.Info> getRooms() {
        ArrayList<RoomInfo> roomInfos = roomService.getRooms();
        return roomInfos.stream()
                .map(roomInfo -> new RoomResponseDto.Info(roomInfo.getId(), roomInfo.getName(), roomInfo.getUserId()))
                .collect(Collectors.toList());
    }

    @GetMapping(value="/room/{id}")
    @Operation(summary = "채널 정보와 채팅 내역을 반환", description = "채널의 정보와 저장된 채팅 내역을 반환합니다.")
    public Optional<RoomInfo> getRoom(@PathVariable UUID id) {
        return roomService.getRoom(id);
    }



    @PostMapping(value = "/room")
    @Operation(summary = "채널 생성", description = "채널을 생성합니다. 생성된 채널의 정보를 반환합니다.")
    public RoomInfo createRoom(@RequestBody RoomRequestDto.Create body) {
        RoomInfo r = roomService.createRoom(body);
        simpMessagingTemplate.convertAndSend("/topic/common", new CommonRoomMessageDto(CommonRoomMessageType.ROOM_CREATE, r.getId(), r.getName()));
        return r;
    }

    @DeleteMapping(value = "/room/{id}")
    @Operation(summary = "채널 삭제", description = "채널을 삭제합니다. 삭제된 채널의 정보를 반환합니다.")
    public Optional<RoomInfo> deleteRoom(@PathVariable UUID id) {
        Optional<RoomInfo> result = roomService.deleteRoom(id);
        result.ifPresent(r -> {
            simpMessagingTemplate.convertAndSend("/topic/common", new CommonRoomMessageDto(CommonRoomMessageType.ROOM_DELETE, r.getId(), r.getName()));
        });

        return result;
    }

    @PutMapping(value = "/room/{id}")
    @Operation(summary = "채널 이름 수정", description = "채널의 이름을 수정합니다. 수정된 채널의 정보를 반환합니다.")
    public Optional<RoomInfo> updateRoom(@PathVariable UUID id, @RequestBody RoomRequestDto.Update body) {
        Optional<RoomInfo> result = roomService.updateRoom(id, body);
        result.ifPresent(r -> {
            simpMessagingTemplate.convertAndSend("/topic/common", new CommonRoomMessageDto(CommonRoomMessageType.ROOM_UPDATE, r.getId(), r.getName()));
        });

        return result;

    }
}
