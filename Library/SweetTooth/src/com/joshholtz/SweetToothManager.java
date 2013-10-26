package com.joshholtz;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import android.app.Activity;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;

public class SweetToothManager {
	
	public final static String LOG_TAG = "SweetTooth";
	public final static int REQUEST_BLE_ENABLE = 3011989;

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
	
	
	public interface SweetToothServiceListener {
		public void onReadServices(BluetoothGatt gatt, List<BluetoothGattService> services);
	}

	public interface SweetToothCharacteristicListener {
		public void onReadCharacteristics(BluetoothGatt gatt, BluetoothGattService service, List<BluetoothGattCharacteristic> characteristics);
		public void onReadCharacteristicsFailure();
	}
	
	/**
	 * Singletony stuff
	 */
	private SweetToothManager() {}

	private static class LazyHolder {
		private static final SweetToothManager INSTANCE = new SweetToothManager();
	}

	public static SweetToothManager initInstance(Application application) {
		getInstance().context = application.getApplicationContext();
		getInstance().initBluetoothManager();
		return getInstance();
	}
	
	public static SweetToothManager getInstance() {
		return LazyHolder.INSTANCE;
	}
	
	/**
	 * Private instance variables
	 */
	private Context context;
	private BluetoothManager bluetoothManager;
	private BluetoothAdapter bluetoothAdapter;
	
	private boolean scanning;
    private Handler handler = new Handler();
	
	private List<SweetToothListener> listeners = new ArrayList<SweetToothListener>();
		
	private void initBluetoothManager() {
		if (context != null) {
			bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
			if (bluetoothManager != null) {
				bluetoothAdapter = bluetoothManager.getAdapter();
			}
		}
	}
	
	public void addListener(SweetToothListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}
	
	public void removeListener(SweetToothListener listener) {
		listeners.remove(listener);
	}
	
	/**
	 * @return the bluetoothManager
	 */
	public BluetoothManager getBluetoothManager() {
		return bluetoothManager;
	}

	/**
	 * @return the bluetoothAdapter
	 */
	public BluetoothAdapter getBluetoothAdapter() {
		return bluetoothAdapter;
	}

	/**
	 * Returns true if BLE is supported on this device.
	 * @param context
	 * @return
	 */
	public boolean isBLESupported() {
		return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
	}
	
	/**
	 * Returns true if BLE is enabled.
	 * @return
	 */
	public boolean isBLEEnable() {
		return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
	}

	/**
	 * Request user to enable BLE if not enabled
	 * @param activity
	 */
	public void requestBluetoothEnabled(Activity activity) {
		if (bluetoothAdapter != null && !this.isBLEEnable()) {
		    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		    activity.startActivityForResult(intent, REQUEST_BLE_ENABLE);
		}
	}
	
	/**
	 * Handles things for request to enable BLE.
	 * @param requestCode
	 * @param resultCode
	 * @param data
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (REQUEST_BLE_ENABLE == requestCode) {
			if (Activity.RESULT_OK == resultCode) {
				Log.d(LOG_TAG, "Request BLE - RESULT_OK");
			} else {
				Log.d(LOG_TAG, "Request BLE - NOT RESULT_OK");
			}
		}
	}
	
	/**
	 * Starts scan
	 */
	public void start() {
		start(new UUID[]{});
	}
	
	/**
	 * Starts scan for specific UUIDs
	 * 
	 * @param uuids
	 */
	public void start(UUID[] uuids) {
		Log.d(LOG_TAG, "start()");
        scanning = true;
        bluetoothAdapter.startLeScan(uuids, leScanCallback);
        
        for (SweetToothListener listener : listeners) {
			listener.onScanningStateChange(scanning);
		}
	}
	
	/**
	 * Stops scan
	 */
	public void stop() {
		Log.d(LOG_TAG, "stop()");
		handler.removeCallbacks(null);
		
		scanning = false;
        bluetoothAdapter.stopLeScan(leScanCallback);
        
        for (SweetToothListener listener : listeners) {
			listener.onScanningStateChange(scanning);
		}
	}
	
	/**
	 * Starts and stops scan on an interval
	 * @param scanPeriodOn
	 * @param scanPeriodOff
	 */
	public void startOnInterval(long scanPeriodOn, long scanPeriodOff) {
		startOnInterval(new UUID[]{}, scanPeriodOn, scanPeriodOff);
	}
	
	/**
	 * Starts and stops scan on an interval for specific UUIDs
	 * @param uuids
	 * @param scanPeriodOn
	 * @param scanPeriodOff
	 */
	public void startOnInterval(UUID[] uuids, long scanPeriodOn, long scanPeriodOff) {
		scanning = true;
	
		for (SweetToothListener listener : listeners) {
			listener.onScanningStateChange(scanning);
		}
		
		doOnInterval(uuids, scanPeriodOn, scanPeriodOff);
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
	
	private void doOnInterval(final UUID[] uuids, final long scanPeriodOn, final long scanPeriodOff) {
		if (!scanning) return;
		
		for (SweetToothListener listener : listeners) {
			listener.onScanningIntervalStateChange(true);
		}
		
		bluetoothAdapter.startLeScan(uuids, leScanCallback);
		
		handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                bluetoothAdapter.stopLeScan(leScanCallback);
                
                for (SweetToothListener listener : listeners) {
        			listener.onScanningIntervalStateChange(false);
        		}
                
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                    	doOnInterval(uuids, scanPeriodOn, scanPeriodOff);
                    }
                }, scanPeriodOff);
            }
        }, scanPeriodOn);
		
	}
	
	public void readCharacteristics(BluetoothDevice device, final UUID serviceUUID, UUID[] characteristicUUIDs, final long timeout, final SweetToothCharacteristicListener listener) {
		device.connectGatt(context, true, new BluetoothGattCallback() {
			
			Timer timer;
			boolean timerCalled;
			
			BluetoothGatt gatt;
			BluetoothGattService service;
			List<BluetoothGattCharacteristic> characteristicsRead = new ArrayList<BluetoothGattCharacteristic>();
			
			LinkedList<BluetoothGattCharacteristic> readQueue = new LinkedList<BluetoothGattCharacteristic>();
			
			{
				timer = new Timer();
				timer.schedule(new TimerTask() {

					@Override
					public void run() {
						timerCalled = true;
						new Handler(context.getMainLooper()).post(new Runnable() {

							@Override
							public void run() {
								listener.onReadCharacteristicsFailure();
							}
							
						});
					}
					
				}, timeout);
			}
			
			@Override
	        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
	            if (newState == BluetoothProfile.STATE_CONNECTED) {
	            	this.gatt = gatt;
	                Log.i(LOG_TAG, "Connected to GATT server.");
	                Log.i(LOG_TAG, "Attempting to start service discovery:" +
	                        gatt.discoverServices()
	                        );

	            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
	                Log.i(LOG_TAG, "Disconnected from GATT server.");
	            }
	        }
			
			public void onServicesDiscovered(BluetoothGatt gatt, int status) {
				Log.i(LOG_TAG, "Services discovered");
				for (BluetoothGattService service : gatt.getServices()) {
					Log.d(LOG_TAG ,"Service discovered - " + service.getUuid());
					
					if (service.getUuid().equals(serviceUUID)) {
						this.service = service;
						for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
							Log.d(LOG_TAG ,"\tCharacteristic discovered - " + characteristic.getUuid());
							readQueue.add(characteristic);
						}
						readNext();
					}
				}
	        }
			
			@Override
			public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
				Log.d(LOG_TAG ,"Read " + characteristic.getUuid() + " - " + characteristic.getStringValue(0));
				characteristicsRead.add(characteristic);
				readNext();
			}
			
			private void readNext() {
				if (readQueue.peekLast() != null) {
					Log.i(LOG_TAG, "Trying to read");
					gatt.readCharacteristic(readQueue.pollLast());
				} else {
					Log.i(LOG_TAG, "Done reading");
					if (!timerCalled) {
						timer.cancel();
						new Handler(context.getMainLooper()).post(new Runnable() {

							@Override
							public void run() {
								listener.onReadCharacteristics(gatt, service, characteristicsRead);
							}
							
						});
					}
				}
			}
			
		});
		
	}
	
	BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
		
		@Override
		public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
			Runnable myRunnable = new Runnable() {
				@Override
				public void run() {
					for (SweetToothListener listener : listeners) {
						listener.onDiscoveredDevice(device, rssi, scanRecord);
					}
				}
			};
			
			Handler mainHandler = new Handler(context.getMainLooper());
			mainHandler.post(myRunnable);
		}
	};
	
}
