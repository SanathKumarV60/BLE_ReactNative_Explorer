import {
    NativeEventEmitter,
    NativeModules,
    EventSubscription,
    
  } from 'react-native'
  import Service from './Service'
  import Characteristic from './Characteristic'
 
  import BLEPeripheral from './BLEPeripheral';
  //const { BLEPeripheralModule1 } = NativeModules
  const EventEmitter = new NativeEventEmitter(BLEPeripheral)
  
  export default class Manager {
    private characteristics: { [uuid: string]: Characteristic } = {}
    private readRequestListener?: EventSubscription
    private subscribeListener?: EventSubscription
    private unsubscribeListener?: EventSubscription
    private writeRequestListener?: EventSubscription

    async setup(version:string){
      await BLEPeripheral.setup(version)
    }
    async enableBluetooth(bluetoothcallback:(param1:string)=>void){
      if(BLEPeripheral == null)
        console.log('Unable to get Module BLEPeripheral Module');
      await BLEPeripheral.enableBluetooth(bluetoothcallback)
  
    }
    /**
     * Add service, along with its characteristics and nested services, to the peripheral.
     */

    async addService(service: Service, 
      
      callback: (deviceName: string,requestId: number, characteristicUUID:string, offset:number,
         value: any, responseNeeded: boolean) =>void,
      gatt_callback: (eventtype: string, param1: string, param2: string)
       => void
       ): Promise<void> {
      if(BLEPeripheral == null)
        console.log('Unable to get Module BLEPeripheral Module');
      await BLEPeripheral.addService(service.name,service.uuid, callback)
      for(let  c of service.characteristics ){
        await BLEPeripheral.addCharacteristicToService(service.uuid, c.uuid, c.properties, c.permissions,c.description)
       
      }
      /*this.characteristics = {
        ...this.characteristics,
        ...service.characteristicsByUuid(),
      }*/
    }
  
    /**
     * Removes a specified p./................................................................................,./.'././ublished service from the local GATT database.
     */
    async removeService(service: Service): Promise<void> {
      await BLEPeripheral.removeService(service)
  
      Object.keys(service.characteristicsByUuid()).forEach(
        chUuid => delete this.characteristics[chUuid]
      )
    }
  
    /**
     * Removes all published services from the local GATT database.
     *
     * Use this when you want to remove all services you’ve previously published, for example, if your app has a toggle button to expose GATT services.
     */
    async removeAllServices(): Promise<void> {
      await BLEPeripheral.removeAllServices()
  
      this.characteristics = {}
    }
  
    /**
     * Advertise peripheral manager data. This will enable BLE central devices to discover this peripheral.
     *
     * _[iOS]_ Core Bluetooth advertises data on a “best effort” basis, due to limited space and because there may be multiple apps advertising simultaneously. While in the foreground, your app can use up to 28 bytes of space in the initial advertisement data for any combination of the supported advertising data keys. If no space remains, there’s an additional 10 bytes of space in the scan response, usable only for the `name`. Note that these sizes don’t include the 2 bytes of header information required for each new data type.
     * Any service UUIDs contained in the value of the `serviceUuids` key that don’t fit in the allotted space go to a special “overflow” area. These services are discoverable only by an iOS device explicitly scanning for them.
     * While your app is in the background, the local name isn’t advertised and all service UUIDs are in the overflow area.
     *
     * _[Android]_ An advertiser can broadcast up to 31 bytes of advertisement data.
     */
  
    async startAdvertise(devName:string){

      await BLEPeripheral.startAdvertising(devName);
  
    }
    async stopAdvertising(){
        await BLEPeripheral.stopAdvertising();
    }
    async startAdvertising(data: {
      /** Local name of the device to be advertised. */
      name: string
      /** A list of service UUIDs. */
      serviceUuids: string[]
    }): Promise<void> {
      await BLEPeripheral.startAdvertising(data)
  
      this.readRequestListener = EventEmitter.addListener(
        BLEPeripheral.READ_REQUEST,
        (params: {
          requestId: string
          characteristicUuid: string
          offset?: number
        }) => {
          const ch = this.characteristics[params.characteristicUuid.toLowerCase()]
  
          if (!ch)
            return BLEPeripheral.respond(
              params.requestId,
              'invalidHandle',
              null
            )
  
          ch.onReadRequest(params.offset).then(value =>
            BLEPeripheral.respond(params.requestId, 'success', value)
          )
        }
      )
  
      this.writeRequestListener = EventEmitter.addListener(
        BLEPeripheral.WRITE_REQUEST,
        (params: {
          requestId: string
          characteristicUuid: string
          value: string
          offset?: number
        }) => {
          const ch = this.characteristics[params.characteristicUuid.toLowerCase()]
  
          if (!ch)
            return BLEPeripheral.respond(
              params.requestId,
              'invalidHandle',
              null
            )
  
          ch.onWriteRequest(params.value, params.offset).then(() =>
          BLEPeripheral.respond(params.requestId, 'success', null)
          )
        }
      )
  
      this.subscribeListener = EventEmitter.addListener(
        BLEPeripheral.SUBSCRIBED,
        (params: { characteristicUuid: string; centralUuid: string }) => {
          const ch = this.characteristics[params.characteristicUuid.toLowerCase()]
          if (ch) ch.onSubscribe()
        }
      )
  
      this.unsubscribeListener = EventEmitter.addListener(
        BLEPeripheral.UNSUBSCRIBED,
        (params: { characteristicUuid: string; centralUuid: string }) => {
          const ch = this.characteristics[params.characteristicUuid.toLowerCase()]
          if (ch) ch.onUnsubscribe()
        }
      )
    }
  
  
    
    /**
     * A boolean value that indicates whether the peripheral is advertising data.
     *
     * The value is `true` if the peripheral is advertising data as a result of successfully calling the `startAdvertising` method, and `false` if the peripheral is no longer advertising its data.
     */
    isAdvertising(): Promise<boolean> {
      return BLEPeripheral.isAdvertising()
    }
  
    /**
     * Implement this method to ensure that Bluetooth low energy is available to use on the local peripheral device.
     *
     * Issue commands to the peripheral manager only when it's in the `poweredOn` state.
     *
     * _[iOS]_ If the state moves below `poweredOff`, advertising has stopped and you must explicitly restart it. In addition, the `poweredOff` state clears the local database; in this case you must explicitly re-add all services.
     */
    onStateChanged(listener: (state: ManagerState) => void) {
      BLEPeripheral.getState().then(listener)
      return EventEmitter.addListener(BLEPeripheral.STATE_CHANGED, listener)
    }
  }
  
  export type ManagerState =
    /** A state that indicates Bluetooth is currently powered off. */
    | 'poweredOff'
    /** A state that indicates Bluetooth is currently powered on and available to use. */
    | 'poweredOn'
    /** A state that indicates the connection with the system service was momentarily lost. */
    | 'resetting'
    /** A state that indicates the application isn’t authorized to use the Bluetooth low energy role. */
    | 'unauthorized'
    /** The manager’s state is unknown. */
    | 'unknown'
    /** A state that indicates this device doesn’t support the Bluetooth low energy central or client role. */
    | 'unsupported'