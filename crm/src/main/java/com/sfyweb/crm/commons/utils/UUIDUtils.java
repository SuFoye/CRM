package com.sfyweb.crm.commons.utils;

import java.util.UUID;

/**
 * 获取uuid的值
 */
public class UUIDUtils {
    public static String getUUID(){
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}
