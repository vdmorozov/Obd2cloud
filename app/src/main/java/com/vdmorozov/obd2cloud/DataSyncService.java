package com.vdmorozov.obd2cloud;

import android.app.Service;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DataSyncService extends Service {

    private BluetoothSocket btSocket;
    private Set<Param> availibleParams;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private ParamValue fetchValue(Param param){

        Integer value = null;
        int code = param.getCode();

        //todo: запрос к устройству через btSocket по коду code

        return new ParamValue(value);
    }

    private Snapshot fetchSnapshot(){
        Map<Param, ParamValue> fetched = new HashMap<>();
        for(Param param : availibleParams){
            fetched.put(param, fetchValue(param));
        }
        return new Snapshot(fetched);
    }

    private void sendSnapshot(Snapshot snapshot){
        //todo: отправка снимка в Firebase
    }
}
