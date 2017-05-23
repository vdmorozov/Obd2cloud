package com.vdmorozov.obd2cloud;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.github.pires.obd.commands.SpeedCommand;
import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.enums.ObdProtocols;
import com.github.pires.obd.exceptions.NoDataException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class DataSyncThread extends Thread {

    private static final String TAG = DataSyncThread.class.getSimpleName();

    private BluetoothDevice btDevice;
    private BluetoothSocket btSocket;
    private Set<Param> availibleParams;

    public DataSyncThread(String btDeviceAddress) throws Exception {
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null || !btAdapter.isEnabled()) {
            throw new Exception("bluetooth adapter init failure");
        }
        btDevice = btAdapter.getRemoteDevice(btDeviceAddress);
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
            //obd adapter connection and initialization
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
            BluetoothSocket socket = btDevice.createInsecureRfcommSocketToServiceRecord(uuid);
            socket.connect();

            btSocket = socket;
            OutputStream btOut = btSocket.getOutputStream();
            InputStream btIn = btSocket.getInputStream();

            new EchoOffCommand().run(btIn, btOut);
            new LineFeedOffCommand().run(btIn, btOut);
            new TimeoutCommand(400).run(btIn, btOut);
            new SelectProtocolCommand(ObdProtocols.AUTO).run(btIn, btOut);

            //preparing commands
            RPMCommand RpmCommand = new RPMCommand();
            SpeedCommand speedCommand = new SpeedCommand();

            //main loop
            while (!Thread.interrupted()) {
                RpmCommand.run(btIn, btOut);
                speedCommand.run(btIn, btOut);

                Log.e(TAG, "RPM: " + RpmCommand.getFormattedResult());
                Log.e(TAG, "Speed: " + speedCommand.getFormattedResult());

                sleep(400);
            }
            finish();
        } catch (InterruptedException e) {
            finish();
        } catch (NoDataException | IOException e) {
            finish();
            e.printStackTrace();
            //todo: уведомить сервис об ошибке (чтобы он остановился)
        }
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
