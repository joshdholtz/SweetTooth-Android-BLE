package com.joshholtz.sweettooth.manager;

import java.util.UUID;

import com.joshholtz.sweettooth.SweetToothCharacteristicListener;
import com.joshholtz.sweettooth.SweetToothListener;

import android.app.Application;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

public interface ISweetToothManager {
	
	public final static String LOG_TAG = "SweetTooth";

	public void initInstance(Application application);
	
	public void addListener(SweetToothListener listener);
	public void removeListener(SweetToothListener listener);
	
	public void start();
	public void start(UUID[] uuids);
	public void stop();
	public void startOnInterval(long scanPeriodOn, long scanPeriodOff);
	public void startOnInterval(UUID[] uuids, long scanPeriodOn, long scanPeriodOff);
	
	public void readCharacteristics(BluetoothDevice device, final UUID serviceUUID, UUID[] characteristicUUIDs, final long timeout, final SweetToothCharacteristicListener listener);
	
}
