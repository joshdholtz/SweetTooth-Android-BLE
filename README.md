# SweetTooth
A simplistic, sugarcoated, iOS CoreBluetooth Wrapper

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
