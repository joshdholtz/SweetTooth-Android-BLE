package com.joshholtz.sweettooth.test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.joshholtz.sweettooth.SweetToothListener;
import com.joshholtz.sweettooth.manager.SweetToothManager;
import com.joshholtz.sweetttooth.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

public class SweetToothManagerService extends Service {
	
	public static String BROADCAST_START = "com.joshholtz.sweettooth.SERVICE_SCAN_START";
	public static String BROADCAST_STOP = "com.joshholtz.sweettooth.SERVICE_SCAN_STOP";
	public static String BROADCAST_SCAN_DISCOVERED = "com.joshholtz.sweettooth.SERVICE_SCAN_DISCOVERED";
	public static String REQUEST_BROADCAST_STATE = "com.joshholtz.sweettooth.REQUEST_BROADCAST_SCAN_STATE";
	public static String BROADCAST_STATE = "com.joshholtz.sweettooth.SCAN_STATE";

	private BluetoothManager bluetoothManager;
	private BluetoothAdapter bluetoothAdapter;
	
	protected SweetToothManager sweetToothManager;
	protected boolean isScanning;
	
	private BluetoothAdapter.LeScanCallback leScanCallback;
	private SweetToothListener sweetToothListener;
	
	public SweetToothManagerService() {
		super();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return Service.START_STICKY;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		this.registerReceiver(startReceiver, new IntentFilter(BROADCAST_START));
		this.registerReceiver(stopReceiver, new IntentFilter(BROADCAST_STOP));
		this.registerReceiver(requestStateReceiver, new IntentFilter(REQUEST_BROADCAST_STATE));
		
		leScanCallback = new BluetoothAdapter.LeScanCallback() {
			@Override
			public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
				handleScan(device, rssi, scanRecord);
			}
		};
		sweetToothListener = new SweetToothListener() {

			@Override
			public void onScanningStateChange(boolean scanning) {
				SweetToothManagerService.this.onScanningStateChange(scanning);
			}

			@Override
			public void onScanningIntervalStateChange(boolean scanning) {
				
			}

			@Override
			public void onDiscoveredDevice(BluetoothDevice device, int rssi, byte[] scanRecord) {
				handleScan(device, rssi, scanRecord);
			}
			
		};
		
		if (android.os.Build.VERSION.SDK_INT < 18) {
			sweetToothManager = new SweetToothManager();
			sweetToothManager.initInstance(getApplication());
			sweetToothManager.addListener(sweetToothListener);
		} else {
			bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
			if (bluetoothManager != null) {
				bluetoothAdapter = bluetoothManager.getAdapter();
			}
		}
		
		sendState();
	}
	
	@Override
	public void onDestroy() {
		this.unregisterReceiver(startReceiver);
		this.unregisterReceiver(stopReceiver);
		sweetToothManager.stop();
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	
	public static void startScan(Context context) {
		context.sendBroadcast(new Intent(BROADCAST_START));
	}
	
	public static void stopScan(Context context) {
		context.sendBroadcast(new Intent(BROADCAST_STOP));
	}
	
	protected void startScan() {
		if (android.os.Build.VERSION.SDK_INT < 18) {
			sweetToothManager.startOnInterval(2000, 2000);
		} else {
			this.onScanningStateChange(true);
			bluetoothAdapter.startLeScan(leScanCallback);
		}
	}
	
	protected void stopScan() {
		if (android.os.Build.VERSION.SDK_INT < 18) {
			sweetToothManager.stop();
		} else {
			this.onScanningStateChange(false);
			bluetoothAdapter.stopLeScan(leScanCallback);
		}
	}
	
	protected void onScanningStateChange(boolean scanning) {
		isScanning = scanning;
		sendState();
	}
	
	BroadcastReceiver startReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			startScan();
		}
	};
	
	BroadcastReceiver stopReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			stopScan();
		}
	};
	
	BroadcastReceiver requestStateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			sendState();
		}
	};
	
	private void sendState() {
		boolean bleSupported = false;
		boolean bleEnabled = false;
		
		if (android.os.Build.VERSION.SDK_INT < 18) {
			bleSupported = sweetToothManager.isBLESupported();
			bleEnabled = sweetToothManager.isBLEEnabled();
		} else {
			bleSupported = (bluetoothAdapter != null);
			bleEnabled = (bluetoothAdapter != null && bluetoothAdapter.isEnabled());
		}
		
		Intent intent = new Intent(BROADCAST_STATE);
		intent.putExtra("scanning", isScanning);
		intent.putExtra("ble_supported", bleEnabled);
		intent.putExtra("ble_enabled", bleSupported);
		
		sendBroadcast(intent);
	}
	
	public void handleScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
		Intent intent = new Intent(BROADCAST_SCAN_DISCOVERED);
		intent.putExtra("device", device);
		intent.putExtra("rssi", rssi);
		intent.putExtra("scanRecord", scanRecord);
		
		Log.d(SweetToothManager.LOG_TAG, "Sending broadcast in SweetToothManagerService");
		
		sendBroadcast(intent);
	}

}
