import  React,{useState, useContext, useEffect }  from 'react';
import { View, Text, FlatList, } from 'react-native';
import bleDeviceContext from '../contexts/bleContext';
import ServiceManager from '../ServiceManager';
import CharcListView from '../views/CharcListView';

export const servicemanager = new ServiceManager();
  
const CharacteristicScreen = ({route,navigation})  => {
 
  const {id} = route.params;
  const devContext = useContext(bleDeviceContext);
  const [charcList, setCharcList] = useState([]);    

  useEffect(() => {  
        loadCharacteristics(id);
  }, [])

  const findService =id =>{
    var foundItem = null;
    console.log('findService id='+ id);
    devContext.items.services.forEach( (item) => {
      console.log('inside forEach ' + item.data.id);
      if(item.data.id == id )
      {
        foundItem = item.data;
        console.log('inside foreach service: match found ' + foundItem.id);
        return foundItem;
      }
    
    })
    return foundItem;
  }
    
  function addItemToCharcList(item) {
    
    console.log('adding char to list' + item.data.id);
    setCharcList([...charcList, item]);
    charcList.push(item);
  }
  const loadCharacteristics= (serviceId) =>{
    var service = findService(serviceId);

    service.characteristics()
      .then( (characteristics) => {
            for(let c of characteristics ){
                addItemToCharcList({ data: c, isSelected: false});
            }
      })
      .catch((err) =>{
          console.log('loadCharacteristics error()'+ err);
      }); 
  }
  const getDeviceById = (devId) =>{
    var device = null;
    devContext.items.devices.forEach( (item) => {
    
      console.log("getDeviceById device:"+ item.data.id );
      console.log(" is this device?" + typeof item.data);
      if(item.data.id == devId){
              console.log("getDeviceById found device:("+ item.data.id + ")="+ item.data.uuid);
              device = item.data;
              return item.data;
      }
      
    });
    return device;
  }
  const updateSelected = (item) => {
        console.log("+++++ " + item.data.id);
        //var id = item.data.id;
        //navigation.navigate("DeviceDetailScreen",{id});
        //write some text value to the selected characteristic

        var charc = item.data;
        var  manager = new ServiceManager();
        
        console.log('updateselected device_id='+ charc.deviceID + " #devices=" + devContext.items.devices.length);
        var device = getDeviceById(charc.deviceID);
        var data = "Welecome to MOSIP BLE";
        
        manager.writeToCharacteristic(device,charc.serviceUUID, charc, data, "123456789");
    
  };
  const ItemSeparatorLine = () => {
    return (
          <View
            style={{ height: 0.5, width: "100%", backgroundColor: "#B2B7BD" }}
          />
    );
  }
  return (
                  
    <FlatList
      data={charcList}
        ItemSeparatorComponent={ItemSeparatorLine}
        renderItem={ ({ item , index}) => (
          <View style={{ flex: 1 }}>
              <CharcListView
                  item={item}
                  index={index}
                  updateList={updateSelected}
              />
          </View>
        )}
        keyExtractor={(item,index) => index}
    />    
        
  );   
}
export default CharacteristicScreen;
