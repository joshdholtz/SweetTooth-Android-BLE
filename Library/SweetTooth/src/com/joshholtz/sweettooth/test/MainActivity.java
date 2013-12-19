package com.joshholtz.sweettooth.test;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import com.joshdholtz.sentry.Sentry;
import com.joshdholtz.sentry.Sentry.SentryEventBuilder;
import com.joshdholtz.sentry.Sentry.SentryEventCaptureListener;
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
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.AndroidCharacter;
import android.text.TextUtils;
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
	List<String> names = new ArrayList<String>();
	Map<String, Integer> namesCount = new HashMap<String, Integer>();
	
	Timer timer;
	
	private BluetoothManager bluetoothManager;
	private BluetoothAdapter bluetoothAdapter;
	
	BroadcastReceiver scanReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent data) {
			BluetoothDevice device = data.getParcelableExtra("device");
			int rssi = data.getIntExtra("rssi", 0);
			final byte[] scanRecord = data.getByteArrayExtra("scanRecord");
			
			// Adds devices to list if it isn't already discovered
			Log.d(SweetToothManager.LOG_TAG, "onDiscovered");
			BluetoothDeviceWrapper wrapper = new BluetoothDeviceWrapper(device, System.currentTimeMillis());
			if (devices.contains(wrapper)) {
				devices.remove(wrapper);
			}
			
			Log.d(SweetToothManager.LOG_TAG, "adding device - " + devices.size());
//						if (SweetToothManager.scanRecordHasService("beb54859b4b64affbc7fa12e8a3cd858", scanRecord)) {
				devices.add(wrapper);
				Collections.sort(devices);
				lstViewAdapter.notifyDataSetChanged();
//						}
				
		}
		
	};
	
	BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
		
		@Override
		public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
			Intent intent = new Intent("com.joshholtz.sweettooth.SCAN_LE");
			intent.putExtra("device", device);
			intent.putExtra("rssi", rssi);
			intent.putExtra("scanRecord", scanRecord);
			
			Log.d(SweetToothManager.LOG_TAG, "Sending broadcast");
			
			MainActivity.this.getApplicationContext().sendBroadcast(intent);
		}
	};
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Sentry.init(this, "https://d2f5ca7560174444a60a06afa8e3a4e2:1f9ba2f96c1f452a87a0522ce099a086@app.getsentry.com/16920");
        
        Sentry.setCaptureListener(new SentryEventCaptureListener() {

			@Override
			public SentryEventBuilder beforeCapture(SentryEventBuilder builder) {
				JSONObject object = builder.getExtra();
				if (object == null) {
					object = new JSONObject();
					builder.setExtra(object);
				}
				
				// Settings extras
				try {
					object.put("Manufacturer", android.os.Build.MANUFACTURER);
					object.put("Model", android.os.Build.MODEL);
					object.put("SDK", android.os.Build.VERSION.SDK_INT);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				// Platform
				builder.setPlatform("Android");
				
				return builder;
			}
        	
        });
     
        // Initializing toggle button
        tglScanning = (ToggleButton) this.findViewById(R.id.tgl_scanning);
        tglScanning.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					
//					bluetoothAdapter.startLeScan(leScanCallback);
					
//					SweetToothManager.getInstance().start();
					SweetToothManager.getInstance().startOnInterval(2000, 2000);
//					
//					SweetToothManager manager = SweetToothManager.getInstance();
//					if (manager.getManager() instanceof SamsungSweetToothManager) {
//						Log.d(SweetToothManager.LOG_TAG, "Going to create timer");
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
//					}
				} else {
					if (timer != null) {
						timer.cancel();
					}
					timer = null;
					SweetToothManager.getInstance().stop();
//					bluetoothAdapter.stopLeScan(leScanCallback);
				}
			}        	
        });
        lstView = (ListView) this.findViewById(R.id.list_view);
//        lstView.setOnItemClickListener(new OnItemClickListener() {
//
//			@Override
//			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
//				BluetoothDeviceWrapper wrapper = devices.get(arg2);
//				
//				
//				SweetToothManager.getInstance().readCharacteristics(wrapper.device,
//						UUID.fromString("beb54859-b4b6-4aff-bc7f-a12e8a3cd858"),
//						new UUID[]{},
//						10000L,
//						new SweetToothCharacteristicListener() {
//
//							@Override
//							public void onReadCharacteristics( BluetoothGatt gatt, BluetoothGattService service, List<BluetoothGattCharacteristic> characteristics) {
//								Log.d(SweetToothManager.LOG_TAG, "onReadCharacteristics");
//								for (BluetoothGattCharacteristic characteristic : characteristics) {
//									Toast.makeText(getApplicationContext(), "Characteristic read - " + characteristic.getUuid() + " = " + characteristic.getStringValue(0), Toast.LENGTH_SHORT).show();
//								}
//							}
//
//							@Override
//							public void onReadCharacteristicsFailure() {
//								Log.d(SweetToothManager.LOG_TAG, "onReadCharacteristicsFailure");
//								Toast.makeText(getApplicationContext(), "Failed to read characteristics", Toast.LENGTH_SHORT).show();
//							}
//					
//				});
//				
//			}
//        	
//        });
        
        
        // Initializing list
        devices = new ArrayList<BluetoothDeviceWrapper>();
//        lstViewAdapter = new DevicesAdapter(this, devices);
        lstView.setAdapter(lstViewAdapter);
        
        // Initializing SweetToothManager
        SweetToothManager.getInstance().initInstance(getApplication());
        SweetToothManager.getInstance().addListener(this);
        

        Toast.makeText(this, "BLE Supported - " + SweetToothManager.getInstance().isBLESupported(), Toast.LENGTH_SHORT).show();
        Toast.makeText(this, "BLE Enabled - " + SweetToothManager.getInstance().isBLEEnabled(), Toast.LENGTH_SHORT).show();
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
//		if (SweetToothManager.scanRecordHasService("beb54859b4b64affbc7fa12e8a3cd858", scanRecord)) {
			devices.add(wrapper);
			Collections.sort(devices);
			lstViewAdapter.notifyDataSetChanged();
//		}

	}
	
	private void checkToRemoveDevices() {
		List<BluetoothDeviceWrapper> devicesToRemove = new ArrayList<BluetoothDeviceWrapper>();
		for (BluetoothDeviceWrapper wrapper : devices) {
			if (!names.contains(wrapper.getName())) {
				names.add(wrapper.getName());
				namesCount.put(wrapper.getName(), 1);
				Sentry.captureMessage("Found BLE Device 1 time - " + wrapper.getName() + "(" + wrapper.device.getAddress() + ")", Sentry.SentryEventBuilder.SentryEventLevel.DEBUG);
			} else {
				int count = namesCount.get(wrapper.getName());
				count++;
				
				if (count == 3 || count == 10 || count == 20 || count == 50) {
					Sentry.captureMessage("Found BLE Device " + count + " times - " + wrapper.getName() + "(" + wrapper.device.getAddress() + ")", Sentry.SentryEventBuilder.SentryEventLevel.DEBUG);
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
    
}
