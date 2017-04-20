package com.vdmorozov.obd2cloud;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class DataSyncService extends IntentService {

    public DataSyncService() {
        super(DataSyncService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent startIntent) {

    }

    private void getDataPortion(){

    }

}
