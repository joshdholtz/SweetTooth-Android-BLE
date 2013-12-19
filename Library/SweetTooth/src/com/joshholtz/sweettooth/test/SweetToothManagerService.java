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

public class SweetToothManagerService extends Service {
	
	public final static String BROADCAST_START = "com.joshholtz.sweettooth.SERVICE_SCAN_START";
	public final static String BROADCAST_STOP = "com.joshholtz.sweettooth.SERVICE_SCAN_STOP";
	public final static String BROADCAST_SCAN_DISCOVERED = "com.joshholtz.sweettooth.SERVICE_SCAN_DISCOVERED";

	private BluetoothManager bluetoothManager;
	private BluetoothAdapter bluetoothAdapter;
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return Service.START_STICKY;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		this.registerReceiver(startReceiver, new IntentFilter(BROADCAST_START));
		this.registerReceiver(stopReceiver, new IntentFilter(BROADCAST_STOP));
		
//		bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
//		if (bluetoothManager != null) {
//			bluetoothAdapter = bluetoothManager.getAdapter();
//		}
		
		SweetToothManager.getInstance().initInstance(getApplication());
		SweetToothManager.getInstance().addListener(sweetToothListener);
	}
	
	@Override
	public void onDestroy() {
		this.unregisterReceiver(startReceiver);
		this.unregisterReceiver(stopReceiver);
		SweetToothManager.getInstance().stop();
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
	
	BroadcastReceiver startReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			SweetToothManager.getInstance().start();
//			bluetoothAdapter.startLeScan(leScanCallback);
		}
	};
	
	BroadcastReceiver stopReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			SweetToothManager.getInstance().stop();
//			bluetoothAdapter.stopLeScan(leScanCallback);
		}
	};
	
	SweetToothListener sweetToothListener = new SweetToothListener() {

		@Override
		public void onScanningStateChange(boolean scanning) {
			
		}

		@Override
		public void onScanningIntervalStateChange(boolean scanning) {
			
		}

		@Override
		public void onDiscoveredDevice(BluetoothDevice device, int rssi, byte[] scanRecord) {
			handleScan(device, rssi, scanRecord);
		}
		
	};
	
	BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
		@Override
		public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
			handleScan(device, rssi, scanRecord);
		}
	};
	
	public void handleScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
		Intent intent = new Intent(BROADCAST_SCAN_DISCOVERED);
		intent.putExtra("device", device);
		intent.putExtra("rssi", rssi);
		intent.putExtra("scanRecord", scanRecord);
		
		Log.d(SweetToothManager.LOG_TAG, "Sending broadcast in SweetToothManagerService");
		
		sendBroadcast(intent);
	}

}
