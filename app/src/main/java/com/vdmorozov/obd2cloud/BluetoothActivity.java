package com.vdmorozov.obd2cloud;

import android.bluetooth.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class BluetoothActivity extends AppCompatActivity {

    //todo: indicating search
    //todo: search restart
    //todo: separate paired and found devices

    private static final String TAG = MainActivity.class.getSimpleName();

    List<HashMap<String, String>> mDeviceList;
    ListView mListView;
    SimpleAdapter mPairedAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.bt_devices);

        mDeviceList = new ArrayList<>();
        mListView = (ListView) findViewById(R.id.pairedView);

        // Register for broadcasts when a device is discovered.
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);

        showBluetoothDevices(mListView);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == android.R.id.home){
            finish();
            onBackPressed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    public void showBluetoothDevices(View view) {

        //BT init
        BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();

        if (bluetooth == null) {
            Snackbar.make(view, "no bluetooth support", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            return;
        }

        if (!bluetooth.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            int REQUEST_ENABLE_BT = 1;
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }

        //Snackbar.make(view, "Yippee-ki-yay, motherfucker! " + bluetooth.getName(), Snackbar.LENGTH_LONG).setAction("Action", null).show();

        //show paired devices
        Set<BluetoothDevice> pairedDevices = bluetooth.getBondedDevices();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                HashMap<String, String> deviceMap = new HashMap<>();
                deviceMap.put("name", device.getName());
                deviceMap.put("address", device.getAddress());
                mDeviceList.add(deviceMap);
            }
        }

        mPairedAdapter = new SimpleAdapter(
                view.getContext(),
                mDeviceList,
                android.R.layout.simple_list_item_2,
                new String[]{"name", "address"},
                new int[]{android.R.id.text1, android.R.id.text2}
        );
        mListView.setAdapter(mPairedAdapter);

        //search for BT devices
        bluetooth.startDiscovery();
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                HashMap<String, String> deviceMap = new HashMap<>();
                String deviceName = device.getName();
                if (deviceName != null && !deviceName.isEmpty()) {
                    deviceMap.put("name", deviceName);
                    deviceMap.put("address", device.getAddress());
                    mDeviceList.add(deviceMap);
                    mPairedAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Don't forget to unregister the ACTION_FOUND receiver.
        try {
            unregisterReceiver(mReceiver);
        } catch (IllegalArgumentException e){
            //если получатель не был зарегистрирован, ничего не делаем (можно проверять с помощью переменной класса)
        }
    }
}
