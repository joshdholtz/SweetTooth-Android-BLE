package com.joshholtz.sweettooth.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.joshholtz.sweettooth.SweetToothCharacteristicListener;
import com.joshholtz.sweettooth.SweetToothListener;
import com.samsung.android.sdk.bt.gatt.BluetoothGatt;
import com.samsung.android.sdk.bt.gatt.BluetoothGattAdapter;
import com.samsung.android.sdk.bt.gatt.BluetoothGattCallback;
import com.samsung.android.sdk.bt.gatt.BluetoothGattCharacteristic;
import com.samsung.android.sdk.bt.gatt.BluetoothGattDescriptor;
import com.samsung.android.sdk.bt.gatt.BluetoothGattServer;

public class SamsungSweetToothManager implements ISweetToothManager {

	/**
	 * Singletony stuff
	 */
	private SamsungSweetToothManager() {
		
	}

	private static class LazyHolder {
		private static final SamsungSweetToothManager INSTANCE = new SamsungSweetToothManager();
	}
	
	public static SamsungSweetToothManager getInstance() {
		return LazyHolder.INSTANCE;
	}
	
	/**
	 * Private instance variables
	 */
	private Context context;
	private BluetoothAdapter bluetoothAdapter;
	public BluetoothGatt bluetoothGatt;
	public BluetoothGattServer bluetoothGattServer;
	
	private boolean scanning;
    private Handler handler = new Handler();
	
    private List<SweetToothListener> listeners = new ArrayList<SweetToothListener>();
	
	@Override
	public void initInstance(Application application) {
		context = application.getApplicationContext();
		initBluetoothManager();
	}
	
	private void initBluetoothManager() {
		if (context != null) {
			bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			
			// Start
	        if (bluetoothGatt == null) {
	        	Log.d(LOG_TAG, "getting profileeee");
	        	boolean result = BluetoothGattAdapter.getProfileProxy(context, mProfileServiceListener, BluetoothGattAdapter.GATT);
	        	Log.d(LOG_TAG, "getting profileeee - " + result);
	        }
		}
	}

	@Override
	public void addListener(SweetToothListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}
	
	@Override
	public void removeListener(SweetToothListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Starts scan
	 */
	@Override
	public void start() {
		start(new UUID[]{});
	}
	
	/**
	 * Starts scan for specific UUIDs
	 * 
	 * @param uuids
	 */
	@Override
	public void start(UUID[] uuids) {
		Log.d(LOG_TAG, "start()");
        scanning = true;
        
        if (bluetoothGatt != null) {
        	bluetoothGatt.startScan();
        }
        
        for (SweetToothListener listener : listeners) {
			listener.onScanningStateChange(scanning);
		}
	}
	
	/**
	 * Stops scan
	 */
	@Override
	public void stop() {
		Log.d(LOG_TAG, "stop()");
		handler.removeCallbacks(null);
		
		scanning = false;
		if (bluetoothGatt != null) {
        	bluetoothGatt.stopScan();
        }
        
        for (SweetToothListener listener : listeners) {
			listener.onScanningStateChange(scanning);
		}
	}
	
	/**
	 * Starts and stops scan on an interval
	 * @param scanPeriodOn
	 * @param scanPeriodOff
	 */
	@Override
	public void startOnInterval(long scanPeriodOn, long scanPeriodOff) {
		start(new UUID[]{});
	}
	
	/**
	 * Starts and stops scan on an interval for specific UUIDs
	 * @param uuids
	 * @param scanPeriodOn
	 * @param scanPeriodOff
	 */
	@Override
	public void startOnInterval(UUID[] uuids, long scanPeriodOn, long scanPeriodOff) {
		start(uuids);
	}

	@Override
	public void readCharacteristics(BluetoothDevice device, UUID serviceUUID, UUID[] characteristicUUIDs, long timeout, SweetToothCharacteristicListener listener) {

	}
	
	private BluetoothProfile.ServiceListener mProfileServiceListener = new BluetoothProfile.ServiceListener() {
        @SuppressLint("NewApi")
        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            Log.d(LOG_TAG, "onServiceConnected() - client. profile is" + profile);

            if (profile == BluetoothGattAdapter.GATT) {
                Log.d(LOG_TAG, " Inside GATT onServiceConnected() - client. profile is" + profile);
                bluetoothGatt = (BluetoothGatt) proxy;
                boolean yayService = bluetoothGatt.registerApp(mGattCallbacks);
                Log.d(LOG_TAG, " yay service" + yayService);
            }
        }

        @Override
        public void onServiceDisconnected(int profile) {
            if (profile == BluetoothGattAdapter.GATT) {

                if (bluetoothGatt != null) {
                	bluetoothGatt.unregisterApp();
                	bluetoothGatt = null;
                }
            }

        }
    };
	
    /**
     * GATT client callbacks
     */
    private BluetoothGattCallback mGattCallbacks = new BluetoothGattCallback() {
    	@Override
    	public void onAppRegistered(int status) {
    		Log.d(LOG_TAG, "onAppRegistered() - status=" + status);
    	}
    	
        @Override
        public void onScanResult(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
            Log.d(LOG_TAG, "onScanResult() - device=" + device + ", rssi=" + rssi);
            
            new Handler(context.getMainLooper()).post(new Runnable() {
				@Override
				public void run() {
					for (SweetToothListener listener : listeners) {
						listener.onDiscoveredDevice(device, rssi, scanRecord);
					}
				}
			});
            
//            if (!checkIfBroadcastMode(scanRecord)) {
//                Bundle mBundle = new Bundle();
//                Message msg = Message.obtain(mDeviceListHandler, GATT_DEVICE_FOUND_MSG);
//                mBundle.putParcelable(BluetoothDevice.EXTRA_DEVICE, device);
//                mBundle.putInt(EXTRA_RSSI, rssi);
//                mBundle.putInt(EXTRA_SOURCE, DEVICE_SOURCE_SCAN);
//                msg.setData(mBundle);
//                msg.sendToTarget();
//            } else
//                Log.i(TAG, "device =" + device + " is in Brodacast mode, hence not displaying");
        }

        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            Log.d(LOG_TAG, " Client onConnectionStateChange (" + device.getAddress() + ")");
            // Device has been connected - start service discovery
//            if (newState == BluetoothProfile.STATE_CONNECTED && mBluetoothGatt != null) {
//                Bundle mBundle = new Bundle();
//                Message msg = Message.obtain(mActivityHandler, PXP_CONNECT_MSG);
//                mBundle.putString(BluetoothDevice.EXTRA_DEVICE, device.getAddress());
//                msg.setData(mBundle);
//                msg.sendToTarget();
//                mBluetoothGatt.discoverServices(device);
//
//            }
//            if (newState == BluetoothProfile.STATE_DISCONNECTED && mBluetoothGatt != null) {
//                Bundle mBundle = new Bundle();
//                Message msg = Message.obtain(mActivityHandler, PXP_DISCONNECT_MSG);
//                mBundle.putString(BluetoothDevice.EXTRA_DEVICE, device.getAddress());
//                msg.setData(mBundle);
//                msg.sendToTarget();
//            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGattCharacteristic characteristic) {
            Log.d(LOG_TAG, "onCharacteristicChanged()");
//            if (TX_POWER_LEVEL_UUID.equals(characteristic.getUuid())) {
//                Bundle mBundle = new Bundle();
//                Message msg = Message.obtain(mActivityHandler, PXP_VALUE_MSG);
//                mBundle.putByteArray(EXTRA_VALUE, characteristic.getValue());
//                msg.setData(mBundle);
//                msg.sendToTarget();
//            }
        }

        @Override
        public void onServicesDiscovered(BluetoothDevice device, int status) {
            Log.d(LOG_TAG, "onServicesDiscovered()");
//            Message msg = Message.obtain(mActivityHandler, PXP_READY_MSG);
//            msg.sendToTarget();
//            DummyReadForSecLevelCheck(device);
        }

        @Override
        public void onCharacteristicRead(BluetoothGattCharacteristic characteristic, int status) {
            Log.d(LOG_TAG, "onCharacteristicRead()");
//            if (TX_POWER_LEVEL_UUID.equals(characteristic.getUuid())) {
//                Bundle mBundle = new Bundle();
//                Message msg = Message.obtain(mActivityHandler, PXP_VALUE_MSG);
//                mBundle.putByteArray(EXTRA_VALUE, characteristic.getValue());
//                msg.setData(mBundle);
//                msg.sendToTarget();
//            }
        }

        public void onDescriptorRead(BluetoothGattDescriptor descriptor, int status) {
            Log.d(LOG_TAG, "onDescriptorRead");
            BluetoothGattCharacteristic mTxPowerccc = descriptor.getCharacteristic();
            Log.d(LOG_TAG, "Registering for notification");

//            boolean isenabled = enableNotification(true, mTxPowerccc);
//            Log.d(LOG_TAG, "Notification status =" + isenabled);
        }

        public void onReadRemoteRssi(BluetoothDevice device, int rssi, int status) {
            Log.i(LOG_TAG, "onRssiRead rssi value is " + rssi);
//            Bundle mBundle = new Bundle();
//            Message msg = Message.obtain(mActivityHandler, GATT_CHARACTERISTIC_RSSI_MSG);
//            mBundle.putParcelable(EXTRA_DEVICE, device);
//            mBundle.putInt(EXTRA_RSSI, rssi);
//            mBundle.putInt(EXTRA_STATUS, status);
//            msg.setData(mBundle);
//            msg.sendToTarget();
        }

    };

}
