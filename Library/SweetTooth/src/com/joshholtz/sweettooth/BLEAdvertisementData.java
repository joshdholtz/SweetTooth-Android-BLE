package com.joshholtz.sweettooth;

import java.util.Arrays;
import java.util.HashMap;

public class BLEAdvertisementData extends HashMap<String, String[]> {

	// Flags
	public final static String FLAGS = "01";
	
	// Service
	public final static String MORE_16_BIT_SERVICE_UUIDS_AVAILABLE = "02";
	public final static String COMPLETE_LIST_16_BIT_SERVICE_UUIDS_AVAILABLE = "03";
	public final static String MORE_32_BIT_SERVICE_UUIDS_AVAILABLE = "04";
	public final static String COMPLETE_LIST_32_BIT_SERVICE_UUIDS_AVAILABLE = "05";
	public final static String MORE_128_BIT_SERVICE_UUIDS_AVAILABLE = "06";
	public final static String COMPLETE_LIST_128_BIT_SERVICE_UUIDS_AVAILABLE = "07";
	
	// Local name
	public final static String LOCAL_NAME_SHORTENED = "08";
	public final static String LOCAL_NAME_COMPLETE = "09";
	
	// TX power level
	public final static String TX_POWER_LEVEL = "0A";
	
	// Simple pairing optional OOB tags
	public final static String CLASS_OF_DEVICE = "0D";
	public final static String SIMPLE_PAIRING_HASH_C = "0E";
	public final static String SIMPLE_PAIRING_RANDOMIZER_R = "0F";
	
	// Security manager TK value
	public final static String TK_VALUE = "10";
	
	// Security manager OOB flags
	public final static String SECURITY_MANAGER_FLAG = "11";
	
	// Slave connection interval range
	public final static String SLAVE_CONNECTION_INTERVAL = "12";
	
	// Service solicitation
	public final static String SERVICE_SOLICITATION_16_BIT_SERVICE_UUIDS = "14";
	public final static String SERVICE_SOLICITATION_128_BIT_SERVICE_UUIDS = "15";
	
	// Service data
	public final static String SERVICE_DATA = "16";
	
	// Manufacturer specific data
	public final static String MANUFACTURER_SPECIFIC_DATA = "FF";
	
	public static BLEAdvertisementData parseAdvertisementData(byte[] bytes) {
		BLEAdvertisementData advertisementData = new BLEAdvertisementData();
		
		String[] hex = toHexArray(bytes);
		
		int position = 0;
		while (position < hex.length) {
			String s = hex[position];
			int length = Integer.parseInt(s, 16);
			
			// Validation - makes sure we aren't going passed length of hex array
			if (length > 1 && position + 1 + length < hex.length ) {
				
				String type = hex[position + 1];
				String[] value = Arrays.copyOfRange(hex, position + 2, position + 1 + length, String[].class);
				
				advertisementData.put(type, value);
				
				position += length;
			} else {
				return advertisementData;
			}
			
			
			position++;
		}
		
		return advertisementData;
	}
	
	public int getFlags() {
		return Integer.valueOf( stringArrayToString( this.get(FLAGS)) );
	}
	
	public String[] get16BitServiceUUIDs() {
		return parseToBitArray( this.get(COMPLETE_LIST_16_BIT_SERVICE_UUIDS_AVAILABLE), 16 );
	}
	
	public String[] getMore16BitServiceUUIDs() {
		return parseToBitArray( this.get(MORE_16_BIT_SERVICE_UUIDS_AVAILABLE), 16 );
	}
	
	public String[] get32BitServiceUUIDs() {
		return parseToBitArray( this.get(COMPLETE_LIST_32_BIT_SERVICE_UUIDS_AVAILABLE), 32 );
	}
	
	public String[] getMore32BitServiceUUIDs() {
		return parseToBitArray( this.get(MORE_32_BIT_SERVICE_UUIDS_AVAILABLE), 32 );
	}
	
	public String[] get128BitServiceUUIDs() {
		return parseToBitArray( this.get(COMPLETE_LIST_128_BIT_SERVICE_UUIDS_AVAILABLE), 128 );
	}
	
	public String[] getMore128BitServiceUUIDs() {
		return parseToBitArray( this.get(MORE_128_BIT_SERVICE_UUIDS_AVAILABLE), 128 );
	}
	
	public String getLocalNameComplete() {
		return hexStringToStringValue( this.get(LOCAL_NAME_COMPLETE) );
	}
	
	public String getLocalNameShortened() {
		return hexStringToStringValue( this.get(LOCAL_NAME_SHORTENED) );
	}
	
	/**
	 * Takes our array of hex string and splits it into array of strings containing a certain number of bits.
	 * @param hex
	 * @param numberOfBits
	 * @return String[]
	 */
	private String[] parseToBitArray(String[] hex, int numberOfBits) {
		if (hex == null) return new String[]{};
		
		int bytesPerSomething = 8;
		
		int size = (numberOfBits / bytesPerSomething);
		String[] array = new String[hex.length / (numberOfBits / bytesPerSomething) ];
		
		int newArrayPos = 0;
		int pos = 0;
		while (pos < hex.length) {
			array[newArrayPos] = stringArrayToString( reverse( Arrays.copyOfRange(hex, pos, pos + size, String[].class) ) );
			
			newArrayPos++;
			pos += size;
		}
		
		return array;
	}
	
	/**
	 * Takes array of hex string and turns it into readable string
	 * @param hex
	 * @param numberOfBits
	 * @return String[]
	 */
	private static String hexStringToStringValue(String[] hex ) {
		return getStringValue( hexStringToByteArray( stringArrayToString( hex ) ) );
	}

	/**
	 * Combines string array into a string
	 * @param array
	 * @return String[]
	 */
	private static String stringArrayToString(String[] array) {
		StringBuilder builder = new StringBuilder();
		if (array != null) {
			for(String s : array) {
			    builder.append(s);
			}
		}
		return builder.toString();
	}
	
	/**
	 * Takes string array of hex values an turns it into a byte array again - we probably shouldn't do this
	 * @param s
	 * @return bytes
	 */
	public static byte[] hexStringToByteArray(String s) {
	    int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
	}
	
	/**
	 * Gets a string value for a byte array
	 * @param value
	 * @return
	 */
	public static String getStringValue(byte[] value) {
		int position = 0;
        if (value == null)
                return null;
        if (position > value.length)
                return null;
        byte[] arrayOfByte = new byte[value.length - position];
        for (int i = 0; i != value.length - position; i++) {
                arrayOfByte[i] = value[(position + i)];
        }
        return new String(arrayOfByte);
	}
	
	/**
	 * Takes a byte array and converts it to a string array of hex values
	 * @param bytes
	 * @return
	 */
	private static String[] toHexArray(byte[] bytes) {
		String[] hexArray = new String[bytes.length];
		for (int i = 0; i < bytes.length; ++i) {
			hexArray[i] = String.format("%02X", bytes[i]);
		}
		return hexArray;
	}
	
	private static String[] reverse(String[] array) {
        if (array == null) {
            return new String[]{};
        }
        int i = 0;
        int j = array.length - 1;
        String tmp;
        while (j > i) {
            tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
            j--;
            i++;
        }
        
        return array;
    }
	
}
