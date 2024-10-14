package com.github.demo.user;

import lombok.Getter;

@Getter
public enum Gender {
    MALE("男"),
    FEMALE("女");

    private String name;

    Gender(String name) {
        this.name = name;
    }
}
