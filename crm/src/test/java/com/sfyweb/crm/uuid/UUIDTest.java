package com.sfyweb.crm.uuid;

import org.junit.Test;

import java.util.UUID;

public class UUIDTest {

    public static void main(String[] args){
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        System.out.println(uuid);
    }
}
