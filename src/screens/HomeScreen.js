
import React, {
    useState,
    useEffect,
    useRef,
  } from 'react';
  import {
    SafeAreaView,
    ScrollView,
    StatusBar,
    StyleSheet,
    Text,
    useColorScheme,
    View,
    Button,
    FlatList,
  } from 'react-native';
  
import {
    Colors,
    DebugInstructions,
    Header,
    LearnMoreLinks,
    ReloadInstructions,
} from 'react-native/Libraries/NewAppScreen';
      
import {BleManager} from 'react-native-ble-plx'

import ItemListView from '../views/ItemListView'
import bleDeviceContext from '../contexts/bleContext';
import Peripherals from '../Peripherals';
import { Dropdown } from 'react-native-material-dropdown-v2';

export const manager = new BleManager();/*

import { NativeEventEmitter, NativeModules } from "react-native";
const eventEmitter = new NativeEventEmitter(NativeModules.BLEPeripheralModule);

const onDeviceConnect = (event) => {
  console.log("DEVICE_CONNECTED", event);
 };
eventEmitter.addListener('DEVICE_CONNECTED', onDeviceConnect);
*/
const HomeScreen = ({ navigation }) => {

  const peripherals = new Peripherals();
  useEffect(() => {

    const subscription = manager.onStateChange((state) => {
      if (state === 'PoweredOn') {
        console.log("Powered On State");
        subscription.remove();
        peripherals.setup("4.0");
      }
    }, true);
    return () => subscription.remove();
  }, [manager]);
      

  const bleDevices = new Map();
  const [list, setList] = useState([]);
  const [ devCount, setDevCount] = useState(0);
  const [msg, setMsg] = useState("message area");
  const [bleVersion, setBleVersion] = useState("4.0");


  const scanTime = 15; //.25 min
  const startTime = useRef(new Date());
  const endTime = useRef(new Date());
  const MOSIP_SERVICE_ID =  '408b965d-c1b3-4f29-9c47-fae9528f3382';
//408b965d-c1b3-4f29-9c47-fae9528f3382
  const scanSpecificDevices=(serviceUUID,values) => {

    startTime.current = new Date();
    values.length = 0;
    list.length = 0;
    bleDevices.clear();
    console.log('scan specific:' + serviceUUID);
    setMsg("scan specific:" +serviceUUID);

    manager.startDeviceScan(
    //  null,
      [serviceUUID],
       {allowDuplicates:false}, (error, device) => {
      if (error) {
        // Handle error (scanning will be stopped automatically)
        console.log('Scanning ... Error '+ error);
        setMsg("'Error:' " + error);
    
        return
    }
     
      if(bleDevices.get(device.id) == null){
        bleDevices.set(device.id, device);
        list.push({ data:device, isSelected: false});
       
        values.addDevice({ data:device, isSelected: false});
        console.log('Scanning..found new device:'+ device.name + ',id:'+ device.id);
        setMsg("found new device:" + device.name + ",id:"+ device.id);
        setMsg("serviceData:" +device.serviceData);
      }
      endTime.current = new Date();
      const et = (endTime.current -startTime.current  ) / 1000;
      console.log('Scanning et:'+ et);
      if(et > scanTime){
        manager.stopDeviceScan();
        console.log('scanned items #:' + list.length);
        setMsg("Scanning done: found #" + list.length);
      }
    });
  }
  const scanDevices = (values) => {
      
      
        startTime.current = new Date();
        var count =  0;  
        values.length = 0;
        list.length = 0;
        bleDevices.clear();
        manager.startDeviceScan(null, {allowDuplicates:false}, (error, device) => {
          if (error) {
              // Handle error (scanning will be stopped automatically)
              console.log('Scanning ... Error '+ error);
              return
          }
          
          console.log("Devcount#:"+ devCount);
          if(bleDevices.get(device.id) == null){
            bleDevices.set(device.id, device);
            list.push({ data:device, isSelected: false});
            values.addDevice({ data:device, isSelected: false});
            console.log('Scanning..found new device:'+ device.name + ',id:'+ device.id);
            //console.log('values #' + values.length );
            count = count + 1;
            setDevCount( count);
          }
    
           
          endTime.current = new Date();
          const et = (endTime.current -startTime.current  ) / 1000;
          console.log('Scanning et:'+ et);
          if(et > scanTime){
            manager.stopDeviceScan();
            console.log('scanned items #:' + list.length);
//            setRefresh( true);
              //values.add(list);
          }
          
    
        });
  }
      

    updateSelected = item => {
        console.log("+++++ " + item.data.id);
        var id = item.data.id;
        navigation.navigate("DeviceDetailScreen",{id});

    };
    const ItemSeparatorLine = () => {
        return (
          <View
            style={{ height: 0.5, width: "100%", backgroundColor: "#B2B7BD" }}
          />
        );
    }
    startBroadcasting = (values) =>{
      

      peripherals.setupService();
      
     // peripherals.startAdvertising("MOSIPID_1234_5678_9012");
     peripherals.startAdvertising("OPENID4VP_8520f0098930a7ab");
    }
    stopBroadcasting =(values) =>{
      peripherals.stopAdvertising();
    }
    const bleVersionData = [
      {"label":"Version 4.0","value":"4.0"},
      {"label":"Version 5.0","value":"5.0"},
      {"label":"Version 4.2","value":"4.2"},
      {"label":"5.0 Controller Legacy Mode","value":"5.0_legacy"},
      
    ]
    return (
        <bleDeviceContext.Consumer>
        { values => (
        <SafeAreaView >
        <View style={{ backgroundColor:  Colors.white,}} >
            <View style={{margin: 10}}>
              <Button 
                title={'Scan Bluetooth'}
                onPress={() => {
                  scanDevices(values);
                  
                }} 
              />
              <Text>Found # {devCount} devices</Text>
              <Button 
                title={'Scan for specific device'}
                onPress={() => {
                  scanSpecificDevices(MOSIP_SERVICE_ID,values);
                  //scanSpecificDevices(null,values);
                  
                }} 
              />
               < Dropdown 
               label='BLE Version'
              data={bleVersionData}
              onChangeText={
                (value) =>{
                  setBleVersion(value);
                  console.log("ble-version set:" +value);
                  peripherals.setup(value);
                }
              }
              />
              <Text></Text>
              <Button 
                title={'Start Advertising'}
                  onPress={() => {
                    startBroadcasting(values);
              }} 
              />   
               <Text></Text>
              <Button 
                title={'Stop Advertising'}
                  onPress={() => {
                    stopBroadcasting(values);
              }}          
              />
            </View>
        </View>  
        <FlatList
             data={list}
             ItemSeparatorComponent={ItemSeparatorLine}
              renderItem={ ({ item , index}) => (
              <View style={{ flex: 1 }}>
                <ItemListView
                  item={item}
                  index={index}
                  updateList={updateSelected}
                />
              </View>
             )}
              keyExtractor={(item,index) => item.data.id}
        /> 
        <View>
        <Text>{msg}</Text>  
        </View>   
        
      </SafeAreaView>
      )}
      </bleDeviceContext.Consumer>
    )
  };
  const styles = StyleSheet.create({
      sectionContainer: {
        marginTop: 32,
        paddingHorizontal: 24,
  },
  sectionTitle: {
        fontSize: 24,
        fontWeight: '600',
        color: Colors.white,
  },
      sectionDescription: {
        marginTop: 8,
        fontSize: 18,
        fontWeight: '400',
      },
      highlight: {
        fontWeight: '700',
      },
      selected: {
        marginLeft: 12,
        fontSize: 20,
        backgroundColor: 'lightgray',
      },
  });


  export default HomeScreen;
