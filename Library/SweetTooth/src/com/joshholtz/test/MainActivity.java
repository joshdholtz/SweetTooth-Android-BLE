package com.joshholtz.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.logging.Logger;

import com.joshholtz.ArrayUtils;
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
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity implements SweetToothListener {

	ToggleButton tglScanning;
	
	ListView lstView;
	DevicesAdapter lstViewAdapter;
	
	List<BluetoothDeviceWrapper> devices;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
     
        // Initializing toggle button
        tglScanning = (ToggleButton) this.findViewById(R.id.tgl_scanning);
        tglScanning.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
//					UUID uuid = UUID.fromString("beb54859-b4b6-4aff-bc7f-a12e8a3cd858");
//					UUID uuid = UUID.fromString("1800");
//					Log.d(SweetToothManager.LOG_TAG, "UUID - " + uuid);
					SweetToothManager.getInstance().startOnInterval(1000, 250);
//					SweetToothManager.getInstance().startOnInterval(new UUID[]{ uuid }, 1000, 250);
				} else {
					SweetToothManager.getInstance().stop();
				}
			}        	
        });
        lstView = (ListView) this.findViewById(R.id.list_view);
        
        // Initializing list
        devices = new ArrayList<BluetoothDeviceWrapper>();
        lstViewAdapter = new DevicesAdapter(this, devices);
        lstView.setAdapter(lstViewAdapter);
        
        // Initializing SweetToothManager
        SweetToothManager.initInstance(getApplication());
        SweetToothManager.getInstance().addListener(this);

        Toast.makeText(this, "BLE Supported - " + SweetToothManager.getInstance().isBLESupported(), Toast.LENGTH_SHORT).show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
	public void onScanningStateChange(boolean scanning) {
    	// Clears devices from list when scanning is off
		if (!scanning) {
			devices.clear();
			lstViewAdapter.notifyDataSetChanged();
		} 
	}

    @Override
	public void onScanningIntervalStateChange(boolean scanning) {
    	// Removes device from list if not seen for 5 seconds
		if (!scanning) {
			List<BluetoothDeviceWrapper> devicesToRemove = new ArrayList<BluetoothDeviceWrapper>();
			for (BluetoothDeviceWrapper wrapper : devices) {
				if (System.currentTimeMillis() - wrapper.lastSeen > 5000) {
					devicesToRemove.add(wrapper);
				}
			}
			devices.removeAll(devicesToRemove);
			lstViewAdapter.notifyDataSetChanged();
		}
	}
    
	@Override
	public void onDiscoveredDevice(BluetoothDevice device, int rssi, byte[] scanRecord) {
		// Adds devices to list if it isn't already discovered
		BluetoothDeviceWrapper wrapper = new BluetoothDeviceWrapper(device, System.currentTimeMillis());
		if (!devices.contains(wrapper)) {
			if (SweetToothManager.scanRecordHasService("beb54859b4b64affbc7fa12e8a3cd858", scanRecord)) {
				devices.add(wrapper);
				lstViewAdapter.notifyDataSetChanged();
			}
		}

	}
	
	/**
	 * Wrapper for BluetoothDevice to hold when it was last seen
	 *
	 */
	public static class BluetoothDeviceWrapper {
		public BluetoothDevice device;
		public long lastSeen;
		
		public BluetoothDeviceWrapper(BluetoothDevice device, long lastSeen) {
			this.device = device;
			this.lastSeen = lastSeen;
			
		}
		
		@Override
		public int hashCode() {
			return this.device.hashCode();
		}
		
		@Override
		public boolean equals(Object other) {
			return this.device.equals(((BluetoothDeviceWrapper)other).device);
		}
		
	}
    
}
