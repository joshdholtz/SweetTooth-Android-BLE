package com.joshholtz.sweettooth;

import java.util.List;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattService;

public interface SweetToothServiceListener {
	public void onReadServices(BluetoothGatt gatt, List<BluetoothGattService> services);
}
