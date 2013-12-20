package com.joshholtz.sweettooth.manager;

import java.util.Arrays;
import java.util.UUID;

import com.joshholtz.sweettooth.BLEAdvertisementData;
import com.joshholtz.sweettooth.SweetToothCharacteristicListener;
import com.joshholtz.sweettooth.SweetToothListener;

import android.app.Application;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

public class SweetToothManager implements ISweetToothManager {
	
	private ISweetToothManager duck;
	
	public SweetToothManager() {
		if (android.os.Build.VERSION.SDK_INT >= 18) {
			Log.d(LOG_TAG, "Using NativeSweetToothManager");
			duck = new NativeSweetToothManagerV2();
		} else if (android.os.Build.VERSION.SDK_INT == 17 && android.os.Build.MANUFACTURER.toLowerCase().contains("samsung")) {
			try {
				Class.forName("com.samsung.android.sdk.bt.gatt.BluetoothGatt");
				
				Log.d(LOG_TAG, "Using SamsungSweetToothManager");
				duck = new SamsungSweetToothManager();
			} catch (Exception e) {
				Log.d(LOG_TAG, "Using NoneSweetToothManager");
			}
		} else if (android.os.Build.VERSION.SDK_INT == 17 && android.os.Build.MANUFACTURER.toLowerCase().contains("motorola")) {
			try {
				Class.forName("com.motorola.bluetoothle.BluetoothGatt");
				
				Log.d(LOG_TAG, "Using MotorolaSweetToothManager");
			} catch (Exception e) {
				Log.d(LOG_TAG, "Using NoneSweetToothManager");
			}
		} else {
			Log.d(LOG_TAG, "Using NoneSweetToothManager");
		}
	};
	
	public void setManagerInstance(ISweetToothManager manager) {
		duck = manager;
	}
	
	public ISweetToothManager getManager() {
		return duck;
	}
	
	/**
	 * Helper method to check advertising data for a service ID.
	 * Addresses Android 4.3 issue when trying to scan for a specific service
	 * 
	 * Example: beb54859b4b64affbc7fa12e8a3cd858
	 * 
	 * @param serviceUUID
	 * @param scanRecord 
	 * @return boolean
	 */
	public static boolean scanRecordHasService(String serviceUUID, byte[] scanRecord) {
		serviceUUID = serviceUUID.replaceAll("-", "");
		
		BLEAdvertisementData blueTipzData = BLEAdvertisementData.parseAdvertisementData(scanRecord);
		return Arrays.asList(blueTipzData.get128BitServiceUUIDs()).contains(serviceUUID.toUpperCase());
	}

	@Override
	public void initInstance(Application application) {
		if (duck == null) return;
		duck.initInstance(application);
	}
	
	@Override
	public boolean isBLESupported() {
		if (duck == null) return false;
		return duck.isBLESupported();
	}

	@Override
	public boolean isBLEEnabled() {
		if (duck == null) return false;
		return duck.isBLEEnabled();
	}

	@Override
	public void addListener(SweetToothListener listener) {
		if (duck == null) return;
		duck.addListener(listener);
	}

	@Override
	public void removeListener(SweetToothListener listener) {
		if (duck == null) return;
		duck.removeListener(listener);
	}

	@Override
	public void start() {
		if (duck == null) return;
		duck.start();
	}

	@Override
	public void start(UUID[] uuids) {
		if (duck == null) return;
		duck.start(uuids);
	}

	@Override
	public void stop() {
		if (duck == null) return;
		duck.stop();
	}

	@Override
	public void startOnInterval(long scanPeriodOn, long scanPeriodOff) {
		if (duck == null) return;
		duck.startOnInterval(scanPeriodOn, scanPeriodOff);
	}

	@Override
	public void startOnInterval(UUID[] uuids, long scanPeriodOn, long scanPeriodOff) {
		if (duck == null) return;
		duck.startOnInterval(uuids, scanPeriodOn, scanPeriodOff);
	}
	
	@Override
	public boolean isScanning() {
		if (duck == null) return false;
		return duck.isScanning();
	}

	@Override
	public void readCharacteristics(BluetoothDevice device, UUID serviceUUID, UUID[] characteristicUUIDs, long timeout, SweetToothCharacteristicListener listener) {
		if (duck == null) return;
		duck.readCharacteristics(device, serviceUUID, characteristicUUIDs, timeout, listener);
	}
	
}
