package com.routon.smartcampus.ble;


import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import java.util.List;
import java.util.UUID;

import com.routon.smartcampus.ble.BleBroadcastManager;





@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BleScanner {

    public static BleScanner getInstance() {
        return BleScannerHolder.sBleScanner;
    }

    private static class BleScannerHolder {
        private static final BleScanner sBleScanner = new BleScanner();
    }

    private BleScanPresenter bleScanPresenter;
    private BleScanState scanState = BleScanState.STATE_IDLE;

    public void scan(UUID[] serviceUuids, String[] names, String mac, boolean fuzzy,
                     long timeOut, final BleScanCallback callback) {

        startLeScan(serviceUuids, new BleScanPresenter(names, mac, fuzzy, false, timeOut) {
            @Override
            public void onScanStarted(boolean success) {
                if (callback != null) {
                    callback.onScanStarted(success);
                }
            }

            @Override
            public void onLeScan(BleDevice bleDevice) {
                if (callback != null) {
                    callback.onLeScan(bleDevice);
                }
            }

            @Override
            public void onScanning(BleDevice result) {
                if (callback != null) {
                    callback.onScanning(result);
                }
            }

            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {
                if (callback != null) {
                    callback.onScanFinished(scanResultList);
                }
            }
        });
    }

   
    private synchronized void startLeScan(UUID[] serviceUuids, BleScanPresenter presenter) {
        if (presenter == null)
            return;

        this.bleScanPresenter = presenter;
       
        
        
        boolean success = BleBroadcastManager.getInstance().getBluetoothAdapter().startLeScan(serviceUuids, bleScanPresenter);
        scanState = success ? BleScanState.STATE_SCANNING : BleScanState.STATE_IDLE;
        bleScanPresenter.notifyScanStarted(success);
    }

    public synchronized void stopLeScan() {
        if (bleScanPresenter == null)
            return;
        if (BleBroadcastManager.getInstance().getBluetoothAdapter() == null)
        	return;

        BleBroadcastManager.getInstance().getBluetoothAdapter().stopLeScan(bleScanPresenter);
        scanState = BleScanState.STATE_IDLE;
        bleScanPresenter.notifyScanStopped();
    }

    public BleScanState getScanState() {
        return scanState;
    }


}
