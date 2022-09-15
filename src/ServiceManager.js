import  React,{ useContext }  from 'react';
//import { View, Text } from 'react-native';
//import bleDeviceContext from './contexts/bleContext';

import {BleManager} from 'react-native-ble-plx'
import { Buffer } from "buffer"

export const manager = new BleManager();

const ServiceManager = () =>{
   // var list = []; // device list
  
  const getDevice = (devName) =>{
        for (let d of list) {
          if(d.name == devName){
            return d;
          }
        }
        return null;
  }
  const getDeviceById = (devId,list) =>{
    
    for (let i=0; i <list.length; i++) {
          console.log("getDeviceById device:"+ list[i].id );
          console.log(" is this device?" + typeof list[i]);
            if(list[i].id == devId){
              console.log("getDeviceById found device:("+ list[i].id + ")="+ list[i].uuid);
              return list[i];
            }
          }
        return null;
  }
  const connectAndDiscoverServices = (device, cbFunction) =>{

        manager.connectToDevice(device.id,{autoconnect:true})
        .then(() => {
            console.log('device connected:'+ device.name);
            device.discoverAllServicesAndCharacteristics().then( () =>{
                device.services().then ( (services) =>{
                    console.log('found services:'+ services.length);
                    cbFunction(device,services);
                })
            })
        })
        .catch( (error) => {
            console.log('catch err:'+ error);
            cbFunction(device,null);
        })
    }
    const connectDevice = (devName, serviceId) => {
        var d = getDevice(list);
        var foundService = null;
        manager.connectToDevice(d.id,{autoconnect:true})
        .then(() => {
            console.log('device connected:'+ d.name);
            d.discoverAllServicesAndCharacteristics().then( () =>{
                d.services().then ( (services) =>{
                    for(let s  of services){
                    console.log("service discovered:" + s.uuid);
                    if(s.uuid == serviceId){
                        foundService = s;
                        break;
                    }          
                }
                });
                
            });
            
        });
        return foundService;
    }

    const findCharacteristic =(service, characteristicId) =>{
        var foundChar = null;
        service.characteristics().then( (characteristics) => {
          for(let c of characteristics ){
            console.log("ch:" + c.uuid);
            if(c.uuid == characteristicId)
              foundChar = c;
          }
        });
        return foundChar;
      }
      const readCharacteristic = (device, serviceId, characteristic) =>{
        var value = null;
        if(characteristic.readable){
          console.log("reading ch:" + characteristic.uuid);
          device.readCharacteristicForService(
            serviceId,
            characteristic.uuid).then( (characteristic) =>{
              console.log("value:" + characteristic.value);
              value = characteristic.value;
          });
        }
        return value;
      }
      const writeToCharacteristic =(device,serviceUUID,characteristic, data,transactionId) =>{
        
          if(characteristic.isWritableWithResponse){
            console.log("writing ch:" + characteristic.uuid);
            const dataBuffer = Buffer.alloc(data.length);
            dataBuffer.write(data, 0, data.length, 'utf8');
            const dataSend = dataBuffer.toString('base64');
            
            device.writeCharacteristicWithResponseForService(
              serviceUUID,
              characteristic.uuid,
              dataSend,
              transactionId
            ).then( (ch) =>{
              console.log("after write value:" + ch.value);
            });
          }
          
      }
    return {
        connectAndDiscoverServices:connectAndDiscoverServices,
        findCharacteristic:findCharacteristic,
        writeToCharacteristic:writeToCharacteristic,
        readCharacteristic:readCharacteristic,
        getDeviceById:getDeviceById,
    }
}
export default ServiceManager;