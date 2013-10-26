package com.joshholtz.sweettooth;

import java.util.List;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

public interface SweetToothCharacteristicListener {

	public void onReadCharacteristics(BluetoothGatt gatt, BluetoothGattService service, List<BluetoothGattCharacteristic> characteristics);
	public void onReadCharacteristicsFailure();
	
}
