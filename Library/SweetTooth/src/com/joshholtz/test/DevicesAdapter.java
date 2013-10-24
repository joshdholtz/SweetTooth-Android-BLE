package com.joshholtz.test;

import java.util.List;

import com.joshholtz.R;
import com.joshholtz.test.MainActivity.BluetoothDeviceWrapper;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class DevicesAdapter extends ArrayAdapter<BluetoothDeviceWrapper> {

	public DevicesAdapter(Context context, List<BluetoothDeviceWrapper> objects) {
		 super(context, R.layout.list_item_device, objects);
	}
	
	@Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // Inflates the sensor_list_item
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.list_item_device, null);
        }

        final BluetoothDeviceWrapper device = this.getItem(position);

        // Gets the list from the items list
        if (device != null) {
        	TextView txtName = (TextView) v.findViewById(R.id.list_item_device_txt_name);
        	txtName.setText(device.device.getName());
        }

        return v;
    }
	
}
