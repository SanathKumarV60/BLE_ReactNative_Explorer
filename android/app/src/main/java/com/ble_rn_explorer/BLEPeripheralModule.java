package com.ble_rn_explorer;

import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import java.util.Map;
import java.util.UUID;
import java.util.HashMap;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertisingSetCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import 	android.bluetooth.le.AdvertisingSetParameters;
import android.bluetooth.le.AdvertisingSet;
import android.bluetooth.le.AdvertisingSetParameters.Builder;
import android.bluetooth.le.PeriodicAdvertisingParameters;


//import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.util.Log;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothDevice;
import 	android.bluetooth.le.ScanResult;
import 	android.bluetooth.le.ScanRecord;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.os.ParcelUuid;
import android.os.Handler;
import android.os.Looper;
import java.util.List;
import java.util.ArrayList;

import com.ble_rn_explorer.BLEService;
import com.facebook.react.bridge.Callback;
import androidx.activity.result.*;
import androidx.core.app.*;
import androidx.core.app.ActivityCompat.*;
import androidx.core.content.ContextCompat;
import android.content.pm.PackageManager;
import static android.Manifest.permission.*;


public class BLEPeripheralModule extends ReactContextBaseJavaModule {
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothManager bluetoothManager;
    private BluetoothLeAdvertiser leAdvertiser;
    private Callback enableBluetoothCallback;
    private AdvertiseSettings advSetting;
    private Context context;
    private static final int ENABLE_REQUEST = 539;
    public static final String LOG_TAG = "BLEPeripheralModule";
    public static final int ADV_TIME = 2 * 60 * 1000;  // 2 minutes  -milli seconds  

    private static ReactApplicationContext currentReactContext;

    private List<BLEService> services = new ArrayList<BLEService>();
    private static final int PERMISSION_REQUEST_CODE = 200;

    private String bleVersion;

    BLEPeripheralModule(ReactApplicationContext context) {
        super(context);
        this.context = context;
        currentReactContext = context;
    }
    private boolean checkPermission() {
        //int result = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION);
        int result1 = ContextCompat.checkSelfPermission(currentReactContext.getApplicationContext(), BLUETOOTH_CONNECT);
        int result2 = ContextCompat.checkSelfPermission(currentReactContext.getApplicationContext(), BLUETOOTH_ADVERTISE);
        
        return result1 == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED;
    }
    private void requestPermission() {

        ActivityCompat.requestPermissions(currentReactContext.getCurrentActivity(), new String[]{BLUETOOTH_CONNECT, BLUETOOTH_ADVERTISE}, PERMISSION_REQUEST_CODE);

    }

    public static void sendNotification(String eventName, String []paramNames, String [] paramValues){
        
        Log.i(LOG_TAG,"sendNotification "+ eventName);
        WritableMap payload = Arguments.createMap();
        for(int i=0; i < paramNames.length; i++)
            payload.putString(paramNames[i], paramValues[i]);
        
        currentReactContext.getJSModule(
          DeviceEventManagerModule.RCTDeviceEventEmitter.class)      
        .emit(eventName, payload);
    }
    private final AdvertiseCallback advertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartFailure(int errorCode) {

            Log.e(LOG_TAG, "Not broadcasting: " + errorCode);

            switch (errorCode) {
                case ADVERTISE_FAILED_ALREADY_STARTED:

                    Log.w(LOG_TAG, "App was already advertising");
                    sendNotification("AdvertiseCallback",
                        new String[]{"errorCode", "errorMessage"},
                        new String[]{String.valueOf(errorCode), "App was already advertising"}
                    );
                
                    break;
                case ADVERTISE_FAILED_DATA_TOO_LARGE:
                    Log.e(LOG_TAG, "Advertise failed - data too large");
                    sendNotification("AdvertiseCallback",
                        new String[]{"errorCode", "errorMessage"},
                        new String[]{String.valueOf(errorCode), "Advertise failed - data too large"}
                    );
                    break;
                case ADVERTISE_FAILED_FEATURE_UNSUPPORTED:
                    Log.e(LOG_TAG, "Advertise failed - feature not supported");
                    sendNotification("AdvertiseCallback",
                        new String[]{"errorCode", "errorMessage"},
                        new String[]{String.valueOf(errorCode), "Advertise failed - feature not supported"}
                    );
     
                    break;
                case ADVERTISE_FAILED_INTERNAL_ERROR:
                    Log.e(LOG_TAG, "Advertise failed - Internal error");
                    sendNotification("AdvertiseCallback",
                        new String[]{"errorCode", "errorMessage"},
                        new String[]{String.valueOf(errorCode), "Advertise failed - Internal error"}
                     );

                    break;
                case ADVERTISE_FAILED_TOO_MANY_ADVERTISERS:
                    Log.e(LOG_TAG, "Advertise failed - too many advertisers");
                    sendNotification("AdvertiseCallback",
                    new String[]{"errorCode", "errorMessage"},
                    new String[]{String.valueOf(errorCode), "Advertise failed - Too many advertisers"}
                     );
                    break;
                default:

                    Log.e(LOG_TAG, "Unhandled error: " + errorCode);
                    sendNotification("AdvertiseCallback",
                    new String[]{"errorCode", "errorMessage"},
                    new String[]{String.valueOf(errorCode), "Advertise failed - unknown error"}
                     );
            }
        }
      

        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            Log.i(LOG_TAG, "on Start Success:" + settingsInEffect.toString());
            sendNotification("onAdvertiseStartSuccess",
                    new String[]{"settingsInEffect"},
                    new String[]{settingsInEffect.toString()}
            );
        }


    };

    private final AdvertisingSetCallback advSetCallback = new AdvertisingSetCallback() {
        @Override
        public void onAdvertisingSetStarted(AdvertisingSet advertisingSet, int txPower, int status) {
            String msg = "";
            Log.i(LOG_TAG, "onAdvertisingSetStarted(): txPower:" + txPower + " , status: "
                    + status);

            switch (status) {
                case AdvertisingSetCallback.ADVERTISE_FAILED_ALREADY_STARTED:
                    msg ="ADVERTISE_FAILED_ALREADY_STARTED";
                    break;
                case AdvertisingSetCallback.ADVERTISE_FAILED_FEATURE_UNSUPPORTED:
                    msg =  "ADVERTISE_FAILED_FEATURE_UNSUPPORTED";
                    break;
                case AdvertisingSetCallback.ADVERTISE_FAILED_DATA_TOO_LARGE:
                    msg =  "ADVERTISE_FAILED_DATA_TOO_LARGE";
                    break;
                case AdvertisingSetCallback.ADVERTISE_FAILED_INTERNAL_ERROR:
                    msg = "ADVERTISE_FAILED_INTERNAL_ERROR";
                    break;
                case AdvertisingSetCallback.ADVERTISE_FAILED_TOO_MANY_ADVERTISERS:
                    msg = "ADVERTISE_FAILED_TOO_MANY_ADVERTISERS";
                    break;
                case AdvertisingSetCallback.ADVERTISE_SUCCESS:
                    msg = "ADVERTISE_SUCCESS";
                    break;
            }
            msg = "NULL";
            if(advertisingSet != null)
                msg = advertisingSet.toString();
            sendNotification("AdvertisingSetCallback",
                new String[]{"status", "advertisingSet"},
                new String[]{String.valueOf(status), msg}
            );
        }

        @Override
        public void onAdvertisingSetStopped(AdvertisingSet advertisingSet) {
            Log.i(LOG_TAG, "onAdvertisingSetStopped():");
            sendNotification("AdvertisingSetCallback",
                new String[]{"status", "advertisingSet"},
                new String[]{ "onAdvertisingSetStopped", advertisingSet.toString()}
            );
        }
    };
    @Override
    public String getName() {
        return "BLEPeripheralModuleHack";

    }

    private BluetoothAdapter getBluetoothAdapter() {
        if (bluetoothAdapter == null) {
            BluetoothManager manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            bluetoothAdapter = manager.getAdapter();
        }
        return bluetoothAdapter;
    }

    private BluetoothManager getBluetoothManager() {
        if (bluetoothManager == null) {
            bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        }
        return bluetoothManager;
    }

    @ReactMethod
    public void setup(String bleVersion){
        Log.d(LOG_TAG,"Setup called");
        this.bleVersion = bleVersion;

        if(!checkPermission())
            requestPermission();
        getBluetoothAdapter();

        boolean bSupported = false;
        bSupported = bluetoothAdapter.isLe2MPhySupported();
        
        Log.i(LOG_TAG,"isLe2MPhySupported ?:" + ( bSupported ? "True":"False"));
        bSupported = bluetoothAdapter.isLeCodedPhySupported();
        Log.i(LOG_TAG,"isLeCodedPhySupported ?:" + ( bSupported ? "True":"False"));
        bSupported = bluetoothAdapter.isLeExtendedAdvertisingSupported();
        Log.i(LOG_TAG,"isLeExtendedAdvertisingSupported ?:" + ( bSupported ? "True":"False"));
        bSupported = bluetoothAdapter.isLePeriodicAdvertisingSupported();
        Log.i(LOG_TAG,"isLePeriodicAdvertisingSupported ?:" + ( bSupported ? "True":"False"));
        
    }
    @ReactMethod
    public void enableBluetooth(Callback callback) {
        if (getBluetoothAdapter() == null) {
            Log.d(LOG_TAG, "No bluetooth support");
            if (callback != null)
                callback.invoke("No bluetooth support");
            return;
        }
        if (!getBluetoothAdapter().isEnabled()) {
            enableBluetoothCallback = callback;
            Intent intentEnable = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (getCurrentActivity() == null) {
                if (callback != null)
                    callback.invoke("Current activity not available");
            } else
                getCurrentActivity().startActivityForResult(intentEnable, ENABLE_REQUEST);
        } else {
            if (callback != null)
                callback.invoke("Internal Error???");
        }
    }

    private AdvertiseSettings createAdvSettings() {
        AdvertiseSettings advSettings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
                .setConnectable(true)
                .setTimeout(0) //0- disbale timeout ADV_TIME)
                .build();
        return advSettings;
    }

    
    byte[] experimentData1 = {(byte)0x48, (byte)0x6E };// (byte) 0xDD, (byte)0x2A};// (byte)0x40, (byte) 0xA6 };// (byte) 0xF0, 0x07, 0x05, 0x00, 0x00, 0x00, 0x00, 0x00};
    byte[] experimentData2 = {0x49, 0x6B, (byte) 0xDA, 0x2B , 0x42, (byte) 0xA4, (byte) 0xF1, 0x08 , 0x06, 0x01, 0x02, 0x03 };//, 0x04, 0x05};
    byte[] experimentData3 = {0x49, 0x6B, (byte) 0xDA, 0x2B , 0x42, (byte) 0xA4, (byte) 0xF1, 0x08 , 0x06, 0x01, 0x02, 0x03};//, 0x04, 0x05};
    
    private AdvertiseData createAdvData(String serviceUUID) {

        ParcelUuid puuid = ParcelUuid.fromString(serviceUUID);
        Log.i(LOG_TAG, "createAdvData: puuid:" + puuid.toString());

        AdvertiseData advData = new AdvertiseData.Builder()
            .setIncludeTxPowerLevel(false)
            .setIncludeDeviceName(false)
           .addServiceUuid(puuid)
           // .addServiceData(puuid, experimentData1) //- ERROR - Data too large
            .build();
        return advData;
    }

    private AdvertiseData createAdvDataOld(String serviceUUID) {

        ParcelUuid puuid = ParcelUuid.fromString(serviceUUID);
        Log.i(LOG_TAG, "createAdvData: puuid:" + puuid.toString());

        AdvertiseData advData = new AdvertiseData.Builder()
            .setIncludeTxPowerLevel(false)
            .setIncludeDeviceName(true)
          //  .addManufacturerData(0xa1a2,experimentData1)
         //  .addServiceUuid(puuid)
           // .addServiceData(puuid, experimentData1) //- ERROR - Data too large
            .build();
        return advData;
    }

    private AdvertiseData createAdvDataNew(String serviceUUID) {

        ParcelUuid puuid = ParcelUuid.fromString(serviceUUID);
        Log.i(LOG_TAG, "createAdvData: puuid:" + puuid.toString());

        AdvertiseData advData = new AdvertiseData.Builder()
            .setIncludeTxPowerLevel(false)
            .setIncludeDeviceName(true)
           .addServiceUuid(puuid)
           .addServiceData(puuid, experimentData1) 
            .build();
        return advData;
    }
    private AdvertiseData createScanResponse(String serviceUUID) {

       
        ParcelUuid puuid = ParcelUuid.fromString(serviceUUID);
        Log.i(LOG_TAG, "createAdvData: puuid:" + puuid.toString());
        AdvertiseData advResp = new AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .addServiceData(puuid, experimentData2) // MAX of 12 bytes
              // .addServiceUuid(ParcelUuid.fromString(serviceUUID))
            .build();
        return advResp;
    }
    private AdvertiseData createScanResponseOld(String serviceUUID) {

       
        ParcelUuid puuid = ParcelUuid.fromString(serviceUUID);
        Log.i(LOG_TAG, "createAdvData: puuid:" + puuid.toString());
        AdvertiseData advResp = new AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .addServiceData(puuid, experimentData3) // MAX of 16 bytes
              // .addServiceUuid(ParcelUuid.fromString(serviceUUID))
             // .addManufacturerData(0xa1a2,experimentData1)
            .build();
        return advResp;
    }
    private BLEService findService(String uuid) {
        for (BLEService s : services) {
            if (s.getUUID().equals(uuid))
                return s;
        }
        return null;
    }

    @ReactMethod
    public void addService(String serviceName, String serviceUUID,
     Callback charecteristicWriteCallback /*  Callback gattCallback */) {
        BLEService svc = new BLEService(serviceName, serviceUUID);
        svc.setBluetoothManager(getBluetoothManager());
        svc.setContext(context);
        svc.setWriteCallback(charecteristicWriteCallback);
       // svc.setGattServerCallback(gattCallback);
        services.add(svc);
    }

    @ReactMethod
    public void addCharacteristicToService(String serviceUUID, String charUUID, int prop, int perm,
            String description) {
        BLEService service = findService(serviceUUID);
        if (service != null) {
            Log.i(LOG_TAG, "Adding char to service: " + service.getName());
            BLECharacteristic ch = new BLECharacteristic(charUUID, prop, perm, description);
            service.addCharacteristic(ch);
        }
    }

    private BluetoothDevice findDevice(String devAlias) {
        for (BLEService s : services) {
            for (BluetoothDevice d : s.getDevices()) {
                if (d.getAlias().equals(devAlias))
                    return d;
            }
        }
        return null;
    }

    private BLEService findServiceByCharId(String charUUID) {
        for (BLEService s : services) {
            for (BLECharacteristic c : s.getCharacteristics()) {
                if (c.getUUID().equals(charUUID))
                    return s;
            }
        }
        return null;
    }

    @ReactMethod
    public void sendWriteResponse(String deviceAlias, int requestId, int status, String characteristicUUID,
            int offset) {
        Log.i(LOG_TAG, "sendWriteResponse for:"+ deviceAlias);        
        BluetoothDevice device = findDevice(deviceAlias);
        BLEService svc = findServiceByCharId(characteristicUUID);
        if (device != null && svc != null) {
            svc.sendResponse(device, requestId, status, offset);
        }
        else
        Log.i(LOG_TAG, "sendWriteResponse invalid Device/ characteristic:");        
        
    }
    private  PeriodicAdvertisingParameters createPeriodicAdvertisingParams(){
        PeriodicAdvertisingParameters parameters =  (new PeriodicAdvertisingParameters.Builder())
        .setIncludeTxPower(false)
        .setInterval(AdvertisingSetParameters.INTERVAL_HIGH)
        .build();
        return parameters;
    }
    private AdvertisingSetParameters createAdvertisingSetParams(){ 
    
        AdvertisingSetParameters parameters = (new AdvertisingSetParameters.Builder())
            .setLegacyMode(false)
            .setConnectable(true)
            .setInterval(AdvertisingSetParameters.INTERVAL_HIGH)
            .setTxPowerLevel(AdvertisingSetParameters.TX_POWER_MEDIUM)
            .setPrimaryPhy(BluetoothDevice.PHY_LE_1M)
            .setSecondaryPhy(BluetoothDevice.PHY_LE_2M)
            .build();
            return parameters;
    }
    
    public void startAdvertisingNew(String devName) {
        Log.i(LOG_TAG, "startAdvertisingNew");
        getBluetoothAdapter();
        if (bluetoothAdapter.isMultipleAdvertisementSupported()) {
            Log.i(LOG_TAG, "startAdvertising:isMultipleAdvertisementSupported = TRUE");

            leAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
            //bluetoothAdapter.setName("OPENID4VP_8520f0098930a7ab");//ddcb43ef75a");
           // bluetoothAdapter.setName("OPENID4VP_8520f0098930a754748b7ddcb43ef75a");
       //     bluetoothAdapter.setName("OPENID4VP_8520f0098930a7ab");//ddcb43ef75a");
            bluetoothAdapter.setName(devName);
        
            for (BLEService s : services) {
                Log.i(LOG_TAG, "startAdvertising:setting up service:" + s.getName());
                s.setupService();
                Log.i(LOG_TAG, "bleAdvertiser.startAdvertisingSet");
            
                leAdvertiser.startAdvertisingSet(
                    createAdvertisingSetParams(), 
                    createAdvDataNew(s.getUUID()),
                    createScanResponse(s.getUUID()),
                    createPeriodicAdvertisingParams(), // periodic advertising params
                    createAdvData(s.getUUID()), //periodic adv data
                    advSetCallback);
            }
           
        }
        else{
            Log.i(LOG_TAG, "startAdvertising:isMultipleAdvertisementSupported = FALSE");
        }

    }   
    private AdvertisingSet currentAdvertisingSet;

    AdvertisingSetCallback callback5 = new AdvertisingSetCallback() {
        @Override
      public void onAdvertisingSetStarted(AdvertisingSet advertisingSet, int txPower, int status) {
          Log.i(LOG_TAG, "onAdvertisingSetStarted(): txPower:" + txPower + " , status: "
            + status);
            
        ParcelUuid puuid = ParcelUuid.fromString(services.get(0).getUUID());
     
        currentAdvertisingSet = advertisingSet;
        currentAdvertisingSet.setScanResponseData(new
          AdvertiseData.Builder()
              .addServiceUuid(puuid)
              .build()
         );
 
      }

      @Override
      public void onAdvertisingDataSet(AdvertisingSet advertisingSet, int status) {
          Log.i(LOG_TAG, "onAdvertisingDataSet() :status:" + status);
      }

      @Override
      public void onScanResponseDataSet(AdvertisingSet advertisingSet, int status) {
          Log.i(LOG_TAG, "onScanResponseDataSet(): status:" + status);
      }

      @Override
      public void onAdvertisingSetStopped(AdvertisingSet advertisingSet) {
          Log.i(LOG_TAG, "onAdvertisingSetStopped():");
      }
    };

    private void startAdvertisingWith_5_Controller(String devName){

        Log.i(LOG_TAG, "startAdvertisingWith_5_Controller()");
        BluetoothLeAdvertiser advertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
        //bluetoothAdapter.setName("OPENID4VP");//ddcb43ef75a");
        bluetoothAdapter.setName(devName);
        AdvertisingSetParameters parameters = (new AdvertisingSetParameters.Builder())
           .setLegacyMode(false) // True by default, but set here as a reminder.
           .setConnectable(true)
           .setInterval(AdvertisingSetParameters.INTERVAL_HIGH)
           .setTxPowerLevel(AdvertisingSetParameters.TX_POWER_MEDIUM)
           .build();

        
       
        AdvertiseData data = (new AdvertiseData.Builder())
            .setIncludeDeviceName(true)
            .setIncludeTxPowerLevel(false)
            .build();

    
        advertiser.startAdvertisingSet(parameters,
             data, null, null, null, callback5);

        // After onAdvertisingSetStarted callback is called, you can modify the
        // advertising data and scan response data:
        
        // Wait for onAdvertisingDataSet callback...
       
    }
    @ReactMethod
    public void startAdvertising(String devName){

        if(bleVersion.equals("4.0")){
            startAdvertisingOld(devName);
        }
        else
        if(bleVersion.equals("5.0_legacy")){
            startAdvertisingWith_5_Controller(devName);
        }
        else{
            startAdvertisingNew(devName);
        }
    }
    @ReactMethod
    public void stopAdvertising(){
    
        Log.i(LOG_TAG, "Stopping Advertising..");
        BluetoothLeAdvertiser advertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
        advertiser.stopAdvertising(advertiseCallback);
    }
    /*
     * 
     * name: OPENID4VP_8520f0098930a7ab - 26 - Adv data
     * GUID - 16 + 12 = 28 - Scan Data
     */
    private void startAdvertisingOld(String devName) {

        Log.i(LOG_TAG, "startAdvertising 4.0");
        
        getBluetoothAdapter();

        if (bluetoothAdapter.isMultipleAdvertisementSupported()) {
            Log.i(LOG_TAG, "startAdvertising:isMultipleAdvertisementSupported = TRUE");
            leAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
            bluetoothAdapter.setName(devName);
            //bluetoothAdapter.setName("OPENID4VP_8520f0098930a7ab");//ddcb43ef75a");
            for (BLEService s : services) {
                Log.i(LOG_TAG, "startAdvertising:setting up service:" + s.getName());
                s.setupService();
                Log.i(LOG_TAG, "bleAdvertiser.startAdvertising");
                leAdvertiser.startAdvertising(
                        createAdvSettings(),
                        createAdvDataOld(s.getUUID()),
                        createScanResponseOld(s.getUUID()),
                        advertiseCallback);
            }
        } else {
            Log.i(LOG_TAG, "startAdvertising:isMultipleAdvertisementSupported = FALSE");

        }
    }

    /*
     * Set of functions to experiment the scan part with data transfer
     */
    private boolean scanning = false;
    private Handler handler = new Handler(Looper.getMainLooper());

    @ReactMethod
    public void startDeviceScan(String [] filterServiceUUIDs, int scanForSecs){

        Log.i(LOG_TAG,"startDeviceScan started...");
        BluetoothLeScanner bleScanner = getBluetoothAdapter().getBluetoothLeScanner();

        //ScanFilter.Builder builder = new ScanFilter.Builder();
       // if(filterServiceUUIDs != null){
       //     builder.setServiceUuid()
       // }
       // ScanFilter scanFilter = builder.build();

        
        // Stops scanning after specified seconds.
        long SCAN_PERIOD = scanForSecs * 1000;

        
        if (!scanning) {
        // Stops scanning after a predefined scan period.
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanning = false;
                    bleScanner.stopScan(leScanCallback);
                    Log.i(LOG_TAG,"startDeviceScan stopped...");
                }
            }, SCAN_PERIOD);

            scanning = true;
            bleScanner.startScan(leScanCallback);
        }else {
            scanning = false;
            bleScanner.stopScan(leScanCallback);
        }

    }
    // Device scan callback.
    private ScanCallback leScanCallback =
        new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            ScanRecord rec = result.getScanRecord();
            Map<ParcelUuid, byte[]> devData = rec.getServiceData();
            Log.i(LOG_TAG,"found Device:" + devData.toString() );
        }
    };
}
