import  React,{useState, useContext, useEffect }  from 'react';
import { View, Text,FlatList, } from 'react-native';
import bleDeviceContext from '../contexts/bleContext';

import ServiceListView from '../views/ServiceListView';
import ServiceManager from '../ServiceManager';

export const servicemanager = new ServiceManager();
  
const DeviceDetailScreen = ({route,navigation})  => {
  const {id} = route.params;
  const devContext = useContext(bleDeviceContext);
  const [serviceList, setServiceList] = useState([]);

  useEffect(() => {
    connectDevice(id);
  }, [])

  /*
  function addItemToServiceList(item) {
    
    console.log('adding item to list' + item);
    setServiceList([...serviceList, item]);
  }*/
  const findDevice =(id) =>{
    var foundItem = null;
  
    devContext.items.devices.forEach( (item) => {
      console.log('inside forEach ' + item.data.id);
      if(item.data.id == id )
      {
        foundItem = item.data;
        console.log('inside foreach: match found ' + foundItem.name);
        return foundItem;
      }

    })
    return foundItem;
  }
  const onGetService = (device, services) =>{
    services.forEach( (item) => {
      console.log('pushing service '+ item.uuid + ','+ item.id);
      //serviceList.add({data:item, isSelected: false});

      serviceList.push( {data:item, isSelected: false});
      setServiceList([...serviceList,{data:item, isSelected: false}]);
      devContext.items.services.push({data:item, isSelected: false} );
    })
    return serviceList;
  }
  const connectDevice= (deviceId) =>{
    var device = findDevice(deviceId);
    
    servicemanager.connectAndDiscoverServices (device, onGetService);
    return serviceList;
  }
  const updateSelected = item => {
    console.log("selected service Id +++++ " + item.data.id);
    var id = item.data.id;
    navigation.navigate("CharacteristicScreen",{id});

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
        data={ serviceList }
        ItemSeparatorComponent={ItemSeparatorLine}
        renderItem={ ({ item , index}) => (
          <View style={{ flex: 1 }}>
            <ServiceListView
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

export default DeviceDetailScreen;


