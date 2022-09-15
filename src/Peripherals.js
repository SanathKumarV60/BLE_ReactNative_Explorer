
//import BLEPeripheralModule, { Service, Characteristic } from 'BLEPeripheralPackage';
import Service from './bleperipherals/Service'
import Characteristic from './bleperipherals/Characteristic'
import Manager from './bleperipherals/Manager.';
//const { BLEPeripheralModule } = ReactNative.NativeModules;


export const Peripherals = (function()  {
    const manager = new Manager();
    const dataParts = new Map();

    function copybytes(v1,v2){
        for(let n=0; n < v2.length; n++)
            v1[n]= v2[n];
    }
    function catbytearray(a1,a2){
        var buffer = new ArrayBuffer( a1.length + a2.length);
        var barr = new Uint8Array(buffer);
        copybytes(barr,a1);
        for(let n= 0; n < a2.length; n++ )
            barr[n + a1.length] = a2[n];
        return barr;
    }
    //callback for characteristic writes
    /*
    * implement multi-part write using requestId
    */
    function writeCallback(deviceName, requestId, characteristicUUID, offset, value, responseNeeded){

        console.log('Write callback:' + deviceName + ':for char:'+ characteristicUUID);
        if(dataParts.get(requestId) != null){
           var data =  dataParts.get(requestId);
           data = catbytearray(data, value);
           dataParts.set(requestId, data);
        }
        else{
            var buffer = new ArrayBuffer(value.length);
            let byteview = new Uint8Array(buffer);
            copybytes(byteview, value);
            dataParts.set(requestId, byteview);
        }
    }
    function gattcallback(eventtype, ...args){
        console.log("gattcb:"+ eventtype + ":"+args);
    }
    function bluetoothcallback(param1){
        console.log("bluetoothcb:"+ param1);
    }
    const characteristic1 = new Characteristic({
        uuid: '60d4356d-9fb9-41b6-9a53-da1744971999',
        value: '', // Base64-encoded string
        properties:2|8, // ['read', 'write'],
        permissions:1| 16, // ['readable', 'writeable'],
        description:'VerifiableCredential',
    });
    const service = new Service({
        uuid: '408b965d-c1b3-4f29-9c47-fae9528f3382',
        characteristics:[characteristic1],
        name:'MOSIP ID Data Service'
    });
    function setup(version){
        manager.setup(version);
    }
    function setupService () {
        // register GATT services that your device provides
        console.log("Service=" + service);
        
       /*
        manager.addService(service).then(() => {
            console.log('manager.addService  completeed');
        })
        .catch(error => {
            console.log("addService error "+ error)
        });
        */
        var p  =manager.addService(service, writeCallback,gattcallback);
        console.log('setupService completeed');
        return p;
        
    }
    function enableBluetooth(){
        manager.enableBluetooth(bluetoothcallback).then(()=>{})
    }
    function startAdvertising  (devName) {
        console.log('start advertising...');
        manager.startAdvertise(devName);
        /*
        manager.startAdvertising({
            name: 'MOBILEID BLE Peripheral',
            serviceUuids: ['2a851edc-165e-4b5a-b9db-0327ea541cd5'],
          }) */  
    }  
    function stopAdvertising(){
        console.log('stopping advertising');
        manager.stopAdvertising();
    }
    return {
        setupService: setupService,
        startAdvertising:startAdvertising,
        enableBluetooth:enableBluetooth,
        setup: setup,
        stopAdvertising:stopAdvertising,
      }
})

export default Peripherals;
