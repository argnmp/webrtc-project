package com.netproto.hycord.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.netproto.hycord.domain.UserInfo;
import com.netproto.hycord.domain.Users;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{
    private final Users users;

    public String addUser(String name) {
        String uuid = UUID.randomUUID().toString();
        users.addNewUser(new UserInfo(uuid, name));
        
        return uuid;
    }

    public void deleteUser(String uuid){
        users.deleteUser(uuid);
    }

    public Optional<UserInfo> findUser(String uuid){
        return users.getUserInfo(uuid);
    }
}
