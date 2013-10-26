package com.joshholtz.sweettooth;

import android.bluetooth.BluetoothDevice;

public interface SweetToothListener {

	/**
	 * Gets called when scanning starts or stops.
	 * Called from start() and stop()
	 * @param scanning
	 */
	public void onScanningStateChange(boolean scanning);
	
	/**
	 * Gets called when startOnInterval() starts and stops scanning.
	 * @param scanning
	 */
	public void onScanningIntervalStateChange(boolean scanning);
	
	/**
	 * Gets called when a device is discovered.
	 * @param device
	 * @param rssi
	 * @param scanRecord
	 */
	public void onDiscoveredDevice(BluetoothDevice device, int rssi, byte[] scanRecord);
	
}
