package com.joshholtz.sweettooth.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.logging.Logger;

import com.joshholtz.sweettooth.ArrayUtils;
import com.joshholtz.sweettooth.BLEAdvertisementData;
import com.joshholtz.sweettooth.SweetToothCharacteristicListener;
import com.joshholtz.sweettooth.SweetToothListener;
import com.joshholtz.sweettooth.manager.SamsungSweetToothManager;
import com.joshholtz.sweettooth.manager.SweetToothManager;
import com.joshholtz.sweetttooth.R;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
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
	
	Timer timer;
	
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
//					SweetToothManager.getInstance().start();
					SweetToothManager.getInstance().startOnInterval(2000, 250);
					
					SweetToothManager manager = SweetToothManager.getInstance();
					if (manager.getManager() instanceof SamsungSweetToothManager) {
						Log.d(SweetToothManager.LOG_TAG, "Going to create timer");
						if (timer == null) {
							timer = new Timer();
							timer.scheduleAtFixedRate(new TimerTask() {
								@Override
								public void run() {
									MainActivity.this.runOnUiThread(new Runnable() {
										@Override
										public void run() {
											checkToRemoveDevices();
										}
									});
								}
							}, 6000, 6000);
						}
					}
				} else {
					if (timer != null) {
						timer.cancel();
					}
					timer = null;
					SweetToothManager.getInstance().stop();
				}
			}        	
        });
        lstView = (ListView) this.findViewById(R.id.list_view);
        lstView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				BluetoothDeviceWrapper wrapper = devices.get(arg2);
				
				SweetToothManager.getInstance().readCharacteristics(wrapper.device,
						UUID.fromString("beb54859-b4b6-4aff-bc7f-a12e8a3cd858"),
						new UUID[]{},
						10000L,
						new SweetToothCharacteristicListener() {

							@Override
							public void onReadCharacteristics( BluetoothGatt gatt, BluetoothGattService service, List<BluetoothGattCharacteristic> characteristics) {
								Log.d(SweetToothManager.LOG_TAG, "onReadCharacteristics");
								for (BluetoothGattCharacteristic characteristic : characteristics) {
									Toast.makeText(getApplicationContext(), "Characteristic read - " + characteristic.getUuid() + " = " + characteristic.getStringValue(0), Toast.LENGTH_SHORT).show();
								}
							}

							@Override
							public void onReadCharacteristicsFailure() {
								Log.d(SweetToothManager.LOG_TAG, "onReadCharacteristicsFailure");
								Toast.makeText(getApplicationContext(), "Failed to read characteristics", Toast.LENGTH_SHORT).show();
							}
					
				});
				
			}
        	
        });
        
        
        // Initializing list
        devices = new ArrayList<BluetoothDeviceWrapper>();
        lstViewAdapter = new DevicesAdapter(this, devices);
        lstView.setAdapter(lstViewAdapter);
        
        // Initializing SweetToothManager
        SweetToothManager.getInstance().initInstance(getApplication());
        SweetToothManager.getInstance().addListener(this);

//        Toast.makeText(this, "BLE Supported - " + SweetToothManager.getInstance().isBLESupported(), Toast.LENGTH_SHORT).show();
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
			checkToRemoveDevices();
		}
	}
    
	@Override
	public void onDiscoveredDevice(BluetoothDevice device, int rssi, byte[] scanRecord) {
		// Adds devices to list if it isn't already discovered
		Log.d(SweetToothManager.LOG_TAG, "onDiscovered");
		BluetoothDeviceWrapper wrapper = new BluetoothDeviceWrapper(device, System.currentTimeMillis());
		if (devices.contains(wrapper)) {
			devices.remove(wrapper);
		}
		
		Log.d(SweetToothManager.LOG_TAG, "adding device - " + devices.size());
		if (SweetToothManager.scanRecordHasService("beb54859b4b64affbc7fa12e8a3cd858", scanRecord)) {
			devices.add(wrapper);
			lstViewAdapter.notifyDataSetChanged();
		}

	}
	
	private void checkToRemoveDevices() {
		List<BluetoothDeviceWrapper> devicesToRemove = new ArrayList<BluetoothDeviceWrapper>();
		for (BluetoothDeviceWrapper wrapper : devices) {
			if (System.currentTimeMillis() - wrapper.lastSeen > 5000) {
				devicesToRemove.add(wrapper);
			}
		}
		devices.removeAll(devicesToRemove);
		lstViewAdapter.notifyDataSetChanged();
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
