package com.netproto.hycord.domain;

import java.util.ArrayList;
import java.util.Optional;

import org.springframework.stereotype.Component;

@Component
public class Users {
    private ArrayList<UserInfo> users = new ArrayList<>();;

    public synchronized void addNewUser(UserInfo Nuser){
        users.add(Nuser);
    }

    public synchronized Optional<UserInfo> getUserInfo(String uuid){
        return users.stream()
                .filter(UserInfo -> uuid.equals(UserInfo.getUuid()))
                .findAny();
    }

    public synchronized void deleteUser(String uuid){
        Optional<UserInfo> info = users.stream()
                                    .filter(Userinfo -> uuid.equals(Userinfo.getUuid()))
                                    .findAny();
        
        if(info.isPresent()){
            users.remove(info.get());
        }
    }
}
