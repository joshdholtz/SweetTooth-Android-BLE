package com.joshholtz.test;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.joshholtz.R;
import com.joshholtz.SweetToothManager;
import com.joshholtz.SweetToothManager.SweetToothListener;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

public class MainActivity extends Activity implements SweetToothListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
     
        Log.d(SweetToothManager.LOG_TAG, "============== STARTING =============");
        
        // Initing SweetToothManager
        SweetToothManager.initInstance(getApplication());
        SweetToothManager.getInstance().addListener(this);
        
        SweetToothManager.getInstance().startOnInterval(5000, 500);
        
        Toast.makeText(this, "BLE Supported - " + SweetToothManager.getInstance().isBLESupported(), Toast.LENGTH_SHORT).show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


	@Override
	public void discoveredDevice(BluetoothDevice device, int rssi, byte[] scanRecord) {
		Log.d(SweetToothManager.LOG_TAG, "Found device in listener - " + device.toString());
	}
    
}
