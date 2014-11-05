package com.joshholtz.sweettooth.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.joshholtz.sweettooth.BLEAdvertisementData;
//import com.joshdholtz.sentry.Sentry;
import com.joshholtz.sweettooth.manager.SweetToothManager;
import com.joshholtz.sweettooth.test.MainActivity.BluetoothDeviceWrapper;
import com.joshholtz.sweetttooth.R;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class ServiceActivity extends Activity {

	ToggleButton tglScanning;
	
	ListView lstView;
	DevicesAdapter lstViewAdapter;
	
	List<BluetoothDeviceWrapper> devices;
	List<String> names = new ArrayList<String>();
	Map<String, Integer> namesCount = new HashMap<String, Integer>();
	
	Timer timer;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        this.registerReceiver(scanReceiver, new IntentFilter(SweetToothManagerService.BROADCAST_SCAN_DISCOVERED));
        this.registerReceiver(stateReceiver, new IntentFilter(SweetToothManagerService.BROADCAST_STATE));
        
        Intent serviceIntent = new Intent(this, SweetToothManagerService.class);
		this.startService(serviceIntent);
		
		// Initializing toggle button
        tglScanning = (ToggleButton) this.findViewById(R.id.tgl_scanning);
        tglScanning.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					SweetToothManagerService.startScan(getApplicationContext());
					
					if (timer == null) {
						timer = new Timer();
						timer.scheduleAtFixedRate(new TimerTask() {
							@Override
							public void run() {
								ServiceActivity.this.runOnUiThread(new Runnable() {
									@Override
									public void run() {
										checkToRemoveDevices();
									}
								});
							}
						}, 6000, 6000);
					}
						
				} else {
					SweetToothManagerService.stopScan(getApplicationContext());
					if (timer != null) {
						timer.cancel();
					}
					timer = null;
				}
			}        	
        });
        
        lstView = (ListView) this.findViewById(R.id.list_view);
        devices = new ArrayList<BluetoothDeviceWrapper>();
        lstViewAdapter = new DevicesAdapter(this.getApplicationContext(), devices);
        lstView.setAdapter(lstViewAdapter);
        
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		this.unregisterReceiver(scanReceiver);
	}
	
	private void checkToRemoveDevices() {
		List<BluetoothDeviceWrapper> devicesToRemove = new ArrayList<BluetoothDeviceWrapper>();
		for (BluetoothDeviceWrapper wrapper : devices) {
			if (!names.contains(wrapper.getName())) {
				names.add(wrapper.getName());
				namesCount.put(wrapper.getName(), 1);
//				Sentry.captureMessage("Found BLE Device 1 time - " + wrapper.getName() + "(" + wrapper.device.getAddress() + ")", Sentry.SentryEventBuilder.SentryEventLevel.DEBUG);
			} else {
				int count = namesCount.get(wrapper.getName());
				count++;
				
				if (count == 3 || count == 10 || count == 20 || count == 50) {
//					Sentry.captureMessage("Found BLE Device " + count + " times - " + wrapper.getName() + "(" + wrapper.device.getAddress() + ")", Sentry.SentryEventBuilder.SentryEventLevel.DEBUG);
				}
				
				namesCount.put(wrapper.getName(), count);
			}
			
			
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
	public static class BluetoothDeviceWrapper implements Comparable<BluetoothDeviceWrapper> {
		public BluetoothDevice device;
		public long lastSeen;
		
		public BluetoothDeviceWrapper(BluetoothDevice device, long lastSeen) {
			this.device = device;
			this.lastSeen = lastSeen;
			
		}
		
		public String getName() {
			return TextUtils.isEmpty(this.device.getName()) ? "Unknown Device" : this.device.getName();
		}
		
		@Override
		public int hashCode() {
			return this.device.hashCode();
		}
		
		@Override
		public boolean equals(Object other) {
			return this.device.equals(((BluetoothDeviceWrapper)other).device);
		}

		@Override
		public int compareTo(BluetoothDeviceWrapper other) {
			return this.getName().compareTo(other.getName());
		}
		
	}
	
	BroadcastReceiver scanReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent data) {
			
			BluetoothDevice device = data.getParcelableExtra("device");
			int rssi = data.getIntExtra("rssi", 0);
			final byte[] scanRecord = data.getByteArrayExtra("scanRecord");
			
			// Adds devices to list if it isn't already discovered
			Log.d(SweetToothManager.LOG_TAG, "onDiscovered - " + device.getAddress());
			BluetoothDeviceWrapper wrapper = new BluetoothDeviceWrapper(device, System.currentTimeMillis());
			if (devices.contains(wrapper)) {
				devices.remove(wrapper);
			}
			
			Log.d(SweetToothManager.LOG_TAG, "adding device - " + devices.size());
			if (SweetToothManager.scanRecordHasService("beb54859b4b64affbc7fa12e8a3cd858", scanRecord) ||
					SweetToothManager.scanRecordHasService("beb54859b4b64affbc7fa12e8a3cd859", scanRecord)) {
				devices.add(wrapper);
				Collections.sort(devices);
				lstViewAdapter.notifyDataSetChanged();
				
				if (SweetToothManager.scanRecordHasService("beb54859b4b64affbc7fa12e8a3cd859", scanRecord)) {
					BLEAdvertisementData blueTipzData = BLEAdvertisementData.parseAdvertisementData(scanRecord);
					Log.d(SweetToothManager.LOG_TAG, "MANU - " + blueTipzData.get48BitValue(BLEAdvertisementData.MANUFACTURER_SPECIFIC_DATA));
				}
			}
				
		}
		
	};
	
	BroadcastReceiver stateReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent data) {
			Toast.makeText(getApplicationContext(), "Supported - " + data.getBooleanExtra("ble_supported", false), Toast.LENGTH_SHORT).show();
			Toast.makeText(getApplicationContext(), "Enabled - " + data.getBooleanExtra("ble_enabled", false), Toast.LENGTH_SHORT).show();
			Toast.makeText(getApplicationContext(), "Scanning - " + data.getBooleanExtra("scanning", false), Toast.LENGTH_SHORT).show();
		}
		
	};
	
}
