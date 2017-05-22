package com.vdmorozov.obd2cloud;

import android.app.Notification;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.widget.Toast;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class DataSyncService extends Service {

    private final static int FOREGROUND_ID = 1;

    private Thread syncThread;

    public void onCreate() {
        super.onCreate();
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_menu_camera)
                .setContentTitle("Obd2cloud sync is working")
                .setContentText("Touch to stop")
                .build();
        startForeground(FOREGROUND_ID, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        if(syncThread != null){
            //todo: а что возвращать-то?
            return START_NOT_STICKY;
        }

        try {
            syncThread = new DataSyncThread(intent.getStringExtra("btDeviceAddress"));
            syncThread.start();
        } catch (Exception e) {
            //todo: сообщить Activity о провале

            stopForeground(true);
            stopSelf();
            e.printStackTrace();
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy(){
        syncThread.interrupt();
        stopForeground(true);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
