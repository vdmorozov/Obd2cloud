package com.vdmorozov.obd2cloud;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

public class Snapshot {

    //todo: 1. как хранить парметры? отдельный класс?
    //todo: 2. как инициализировать / возвращать параметры?

    private Date creationDate;
    private Map<Param, ParamValue> params;

    public Snapshot(Map<Param, ParamValue> paramValueMap){
        creationDate = new Date();
        params = paramValueMap;
    }

    public Date getCreationDate(){
        return creationDate;
    }

    public Map<Param, ParamValue> getParams(){
        return Collections.unmodifiableMap(params);
    }
}
