package com.routon.smartcampus.ble;

import android.annotation.SuppressLint;
import android.bluetooth.le.AdvertiseData;
import android.os.ParcelUuid;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

import com.routon.smartcampus.ble.BleLog;
import com.routon.smartcampus.ble.HexUtil;

/**
 * ble util
 */
public class BleUtil {

    //设置一下scan广播数据
    @SuppressLint("NewApi")
	public static AdvertiseData createScanAdvertiseData(short major, short minor, byte txPower) {
        AdvertiseData.Builder builder = new AdvertiseData.Builder();
        builder.setIncludeDeviceName(true);

        byte[] serverData = new byte[5];
        ByteBuffer bb = ByteBuffer.wrap(serverData);
        bb.order(ByteOrder.BIG_ENDIAN);
//        bb.put((byte) 0x02);
//        bb.put((byte) 0x15);
        bb.putShort(major);
        bb.putShort(minor);
        bb.put(txPower);

        builder.addServiceData(ParcelUuid.fromString(BluetoothUUID.bleServerUUID.toString())
                , serverData);

//        builder.setIncludeTxPowerLevel(true);
        AdvertiseData adv = builder.build();
        return adv;
    }

    /**
     * create AdvertiseDate for iBeacon
     */
    @SuppressLint("NewApi")
	public static AdvertiseData createIBeaconAdvertiseData(UUID proximityUuid, short major, short minor, byte txPower) {
        if (proximityUuid == null) {
            throw new IllegalArgumentException("proximityUuid null");
        }
  

//        String[] uuidstr = proximityUuid.toString().replaceAll("-", "").toLowerCase().split("");
//        byte[] uuidBytes = new byte[16];
//        for (int i = 1, x = 0; i < uuidstr.length; x++) {
//            uuidBytes[x] = (byte) ((Integer.parseInt(uuidstr[i++], 16) << 4) | Integer.parseInt(uuidstr[i++], 16));
//        }
//        
//
////        the 2 byte beacon identifier (0xBEAC)
////        the 16 bytes UUID
////        the 2 byte major
////        the 2 byte minor
////        the 1 byte tx power
//
//        byte[] majorBytes = {(byte) (major >> 8), (byte) (major & 0xff)};
//        byte[] minorBytes = {(byte) (minor >> 8), (byte) (minor & 0xff)};
//        byte[] mPowerBytes = {txPower};
//        byte[] manufacturerData = new byte[0x12];
//        byte[] flagibeacon = {0x02, 0x15};
//
//        System.arraycopy(flagibeacon, 0x0, manufacturerData, 0x0, 0x2);
//        System.arraycopy(uuidBytes, 0x0, manufacturerData, 0x2, 0x10);
//        System.arraycopy(majorBytes, 0x0, manufacturerData, 0x12, 0x2);
//        System.arraycopy(minorBytes, 0x0, manufacturerData, 0x14, 0x2);
//        System.arraycopy(mPowerBytes, 0x0, manufacturerData, 0x16, 0x1);
//
        
        String beaconType="0215"; //按照apple iBeacon协议
        String   uuid=proximityUuid.toString().replace("-","");
        String majorStr = HexUtil.formatStringLenth(4,Integer.toHexString(major),'0');
        String minorStr = HexUtil.formatStringLenth(4,Integer.toHexString(minor),'0');
        String measuredPower = HexUtil.formatStringLenth(2,Integer.toHexString(-59),'0');//-59是 measuredPower,一般设备默认都是-59，这里固定了


        BleLog.e(" measuredPower:"+measuredPower +"   "+Integer.toHexString(-59));
        String dataStr=beaconType+uuid+majorStr+minorStr+measuredPower;
        BleLog.e(" dataStr:"+dataStr);
        byte[] data= HexUtil.hexStringToBytes( dataStr);
        
        AdvertiseData.Builder builder = new AdvertiseData.Builder();
        builder.addManufacturerData(0x004c, data);

        AdvertiseData adv = builder.build();
       
        return adv;
    }
}
