package com.netproto.hycord.controller;

import com.netproto.hycord.domain.RoomInfo;
import com.netproto.hycord.domain.UserInfo;
import com.netproto.hycord.domain.Users;
import com.netproto.hycord.dto.UserRequestDto;
import com.netproto.hycord.dto.UserResponseDto;
import com.netproto.hycord.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/")
@CrossOrigin(origins = "*")
public class UserController {
    private final UserService userService;

    @PostMapping(value="/user")
    @Operation(summary = "유저 key 생성", description = "유저를 구분하는데 사용하는 key를 반환합니다.")
    public UserResponseDto.CREATE addUser(@RequestBody UserRequestDto.CREATE body) {
        System.out.println(body.getName());
        return new UserResponseDto.CREATE(userService.addUser(body.getName()));
    }

    @GetMapping(value = "/user/{uuid}")
    @Operation(summary = "유저 이름 요청", description = "유저의 uuid를 이용해 유저의 정보를 반환합니다.")
    public Optional<UserInfo> getUserInfo(@PathVariable String uuid){
        return userService.findUser(uuid);
    }

}
