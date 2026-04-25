package com.mj.mijing.utils;

import com.mj.mijing.dto.UserDTO;

/**
 * 当前用户 ThreadLocal 持有者
 */
public class UserHolder {

    private static final ThreadLocal<UserDTO> tl = new ThreadLocal<>();

    public static void saveUser(UserDTO user) {
        tl.set(user);
    }

    public static UserDTO getUser() {
        return tl.get();
    }

    public static void removeUser() {
        tl.remove();
    }
}
