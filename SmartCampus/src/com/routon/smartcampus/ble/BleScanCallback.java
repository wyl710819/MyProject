package com.routon.smartcampus.ble;




import java.util.List;

public interface BleScanCallback {

    public abstract void onScanStarted(boolean success);

    public abstract void onScanning(BleDevice result);

    public abstract void onScanFinished(List<BleDevice> scanResultList);

    public void onLeScan(BleDevice bleDevice);
}
