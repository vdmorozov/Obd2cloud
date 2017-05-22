package com.vdmorozov.obd2cloud;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class DataSyncThread extends Thread {

    private BluetoothAdapter btAdapter;
    private BluetoothDevice btDevice;
    private BluetoothSocket btSocket;
    private OutputStream btOut;
    private InputStream btIn;
    private Set<Param> availibleParams;

    public DataSyncThread(String btDeviceAddress) throws Exception {
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null || !btAdapter.isEnabled()) {
            throw new Exception("bluetooth adapter init failure");
        }
        btDevice = btAdapter.getRemoteDevice(btDeviceAddress);
    }

    private void init() throws IOException {
        btSocket = btObdConnect(btDevice);
        btOut = btSocket.getOutputStream();
        btIn = btSocket.getInputStream();
    }

    private void finish() {
        try {
            btSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            init();
            String cmd = "ATZ";
            while (!Thread.interrupted()) {
                Log.e("ACHTUNG", btSendCommand(cmd));
                sleep(400);
            }
            finish();
        } catch (InterruptedException e) {
            finish();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private BluetoothSocket btObdConnect(BluetoothDevice btDevice) throws IOException {
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        BluetoothSocket socket = btDevice.createInsecureRfcommSocketToServiceRecord(uuid);
        socket.connect();

        return socket;
    }

    private String btSendCommand(String code) throws IOException {
        btOut.write((code + "\r").getBytes());
        btOut.flush();

        byte b;
        StringBuilder res = new StringBuilder();

        // read until '>' arrives OR end of stream reached
        char c;
        // -1 if the end of the stream is reached
        while (((b = (byte) btIn.read()) > -1)) {
            c = (char) b;
            if (c == '>') // read until '>' arrives
            {
                break;
            }
            res.append(c);
        }
        return res.toString();
    }

    private ParamValue fetchValue(Param param) {

        Integer value = null;
        int code = param.getCode();

        //todo: запрос к устройству через btSocket по коду code

        return new ParamValue(value);
    }

    private Snapshot fetchSnapshot() {
        Map<Param, ParamValue> fetched = new HashMap<>();
        for (Param param : availibleParams) {
            fetched.put(param, fetchValue(param));
        }
        return new Snapshot(fetched);
    }

    private void sendSnapshot(Snapshot snapshot) {
        //todo: отправка снимка в Firebase
    }

}
