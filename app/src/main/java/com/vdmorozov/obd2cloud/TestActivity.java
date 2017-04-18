package com.vdmorozov.obd2cloud;

import android.bluetooth.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class TestActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    List<HashMap<String, String>> mDeviceList;
    ListView mListView;
    SimpleAdapter mPairedAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDeviceList = new ArrayList<>();
        mListView = (ListView) findViewById(R.id.pairedView);

        // Register for broadcasts when a device is discovered.
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
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

    //СВАЛКА

    public void onBluetoothClick(View view) {

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

        Snackbar.make(view, "Yippee-ki-yay, motherfucker! " + bluetooth.getName(), Snackbar.LENGTH_LONG).setAction("Action", null).show();

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

        String res;
        res = bluetooth.startDiscovery() ? "true" : "false";

        Log.i("BLUETOOTH_DISCOVERY", res);

        if(bluetooth.isDiscovering()){
            Toast.makeText(this, "discovering...", Toast.LENGTH_LONG).show();
        }
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
