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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BluetoothActivity extends AppCompatActivity {

    //todo: indicating search
    //todo: search restart
    //todo: separate paired and found devices

    private static final String TAG = MainActivity.class.getSimpleName();

    List<HashMap<String, String>> mDeviceList;
    ListView mListView;
    SimpleAdapter mPairedAdapter;
    private BluetoothAdapter btAdapter;
    public static final int RC_ENABLE_BT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        ActionBar actionBar =  getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.bt_devices);

        mDeviceList = new ArrayList<>();
        mListView = (ListView) findViewById(R.id.pairedView);

        // Register for broadcasts when a device is discovered.
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String address = mDeviceList.get(position).get("address");
                btConnect(address);

                //todo: 2. запустить службу для обмена данными в случае успешного соединения
            }
        });

        showBluetoothDevices(mListView);
    }

    private void btConnect(String address){
        BluetoothDevice device = btAdapter.getRemoteDevice(address);
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        try {
            BluetoothSocket socket = device.createInsecureRfcommSocketToServiceRecord(uuid);
            socket.connect();

            String connected = socket.isConnected() ? "true" : "false";
            Toast.makeText(this, connected, Toast.LENGTH_LONG).show();

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        if (btAdapter == null) {
            Snackbar.make(view, "no bluetooth support", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            return;
        }

        if (!btAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, RC_ENABLE_BT);
            return;
        }

        //Snackbar.make(view, "Yippee-ki-yay, motherfucker! " + bluetooth.getName(), Snackbar.LENGTH_LONG).setAction("Action", null).show();

        //show paired devices
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();

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
        btAdapter.startDiscovery();
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_ENABLE_BT) {
            if(btAdapter.isEnabled()){
                showBluetoothDevices(mListView);
            } else {
                finish();
                Toast.makeText(this, R.string.bt_need_to_enable, Toast.LENGTH_SHORT).show();
            }
        }
    }

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
