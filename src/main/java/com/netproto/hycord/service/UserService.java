package com.netproto.hycord.service;

import java.util.Optional;

import com.netproto.hycord.domain.UserInfo;

public interface UserService {
    String addUser(String name);
    void deleteUser(String uuid);
    Optional<UserInfo> findUser(String uuid);
}
