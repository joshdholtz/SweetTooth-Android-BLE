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
	
	private SweetToothManager() {
		if (android.os.Build.VERSION.SDK_INT >= 18) {
			Log.d(LOG_TAG, "Using NativeSweetToothManager");
			duck = NativeSweetToothManager.getInstance();
		} else if (android.os.Build.VERSION.SDK_INT == 17 && android.os.Build.MANUFACTURER.toLowerCase().contains("samsung")) {
			Log.d(LOG_TAG, "Using SamsungSweetToothManager");
			duck = SamsungSweetToothManager.getInstance();
		} else {
			Log.d(LOG_TAG, "Using NoneSweetToothManager");
		}
	};
	
	public ISweetToothManager getManager() {
		return duck;
	}
	
	public static SweetToothManager getInstance() {
		return LazyHolder.INSTANCE;
	}
	
	private static class LazyHolder {
		private static final SweetToothManager INSTANCE = new SweetToothManager();
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
		BLEAdvertisementData blueTipzData = BLEAdvertisementData.parseAdvertisementData(scanRecord);
		return Arrays.asList(blueTipzData.get128BitServiceUUIDs()).contains(serviceUUID.toUpperCase());
	}

	@Override
	public void initInstance(Application application) {
		if (duck == null) return;
		duck.initInstance(application);
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
	public void readCharacteristics(BluetoothDevice device, UUID serviceUUID, UUID[] characteristicUUIDs, long timeout, SweetToothCharacteristicListener listener) {
		if (duck == null) return;
		duck.readCharacteristics(device, serviceUUID, characteristicUUIDs, timeout, listener);
	}
	
}