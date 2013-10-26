# SweetTooth
A simplistic, sugarcoated, Android BLE Wrapper

## Let's Get Down To Business

1. Initialize SweetToothManager with Application (to get context)
2. Add SweetToothListener to SweetToothManager
    - This listener will get notified of discovered devices
3. Start scanning
    - You know, it starts scanning for devices

````java

public class MainActivity extends Activity implements SweetToothListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
            
        // Initializes SweetToothManager
        SweetToothManager.initInstance(getApplication());
        
        // Adds SweetToothListener
        SweetToothManager.getInstance().addListener(this);
        
        // Starts scanning on an interval - turns on scan for 5 seconds, turns off scanning for 0.5 seconds
        // This is used (in this case) since Android as no way to turn on multiple discoveries of an advertising device
        SweetToothManager.getInstance().startOnInterval(5000, 500);
        
        // Shows toast if BLE is supported
        Toast.makeText(this, "BLE Supported - " + SweetToothManager.getInstance().isBLESupported(), Toast.LENGTH_SHORT).show();
    }

	@Override
	public void discoveredDevice(BluetoothDevice device, int rssi, byte[] scanRecord) {
		Log.d(SweetToothManager.LOG_TAG, "Found device in listener - " + device.toString());
	}
    
}


````

## WTF is with the scanRecord?
The scan record contains advertisement data but the simple byte array does not provide easy access to the information.

<b>BLEAdvertisementData</b> class parses the scanRecord and provides helpers for getting all of the data out.
If the helpers aren't enough, the <b>BLEAdvertisementData</b> class extends HashMap<String, String[] array> so you can grab any data you want out of it.

````java

	@Override
	public void discoveredDevice(BluetoothDevice device, int rssi, byte[] scanRecord) {
		BLEAdvertisementData blueTipzData = BLEAdvertisementData.parseAdvertisementData(scanRecord);
		
		Log.d("SweetTooth", "Flags - " + blueTipzData.getFlags() );
		
		Log.d("SweetTooth", "16 bit UUIDS - " + Arrays.toString( blueTipzData.get16BitServiceUUIDs() ) );
		Log.d("SweetTooth", "More 16 bit UUIDS - " + Arrays.toString( blueTipzData.getMore16BitServiceUUIDs() ) );
		
		Log.d("SweetTooth", "32 bit UUIDS - " + Arrays.toString( blueTipzData.get32BitServiceUUIDs() ) );
		Log.d("SweetTooth", "More 32 bit UUIDS - " + Arrays.toString( blueTipzData.getMore32BitServiceUUIDs() ) );
		
		Log.d("SweetTooth", "128 bit UUIDS - " + Arrays.toString( blueTipzData.get128BitServiceUUIDs() ) );
		Log.d("SweetTooth", "More 128 bit UUIDS - " + Arrays.toString( blueTipzData.getMore128BitServiceUUIDs() ) );

		Log.d("SweetTooth", "Local name complete - " + blueTipzData.getLocalNameComplete() );
		Log.d("SweetTooth", "Local name shortened - " + blueTipzData.getLocalNameShortened() );
	}


````
