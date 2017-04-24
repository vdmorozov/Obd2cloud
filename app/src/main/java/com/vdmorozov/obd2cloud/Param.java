package com.vdmorozov.obd2cloud;

public class Param {
    private String name;
    private Integer code;

    public Param(String name, Integer code){
        this.name = name;
        this.code = code;
    }

    public String getName(){
        return name;
    }

    public Integer getCode(){
        return code;
    }
}
