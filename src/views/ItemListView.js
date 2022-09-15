import React, { Component } from "react";
import {
  StyleSheet,
  Text,
  View,
  Alert,
  TouchableOpacity,
  Dimensions
} from "react-native";
const screenWidth = Math.round(Dimensions.get("window").width);

export default class ItemListView extends Component {
    constructor(props) {
      super(props);
  
      this.GetListItem = this.GetListItem.bind(this);
      this.state = {
        item: this.props.item,
        index: this.props.index
      };
    }
  //This function will update state and update UI and call function from Main call to update original list
  GetListItem(item) {
    item.isSelected = !this.state.item.isSelected;

    this.setState({ item: item });
    this.props.updateList(this.state.item);
  }

  renderCustItem (item) {
    //const color = item.connected ? 'green' : '#fff';
    return (
   
      <View 
      style={{
        margin: 5,
        width: screenWidth - 10,
        backgroundColor: item.isSelected ? "#74B0F7" : "#FFF"
      }}>
      
          <Text style={{fontSize: 14, textAlign: 'left', color: '#333333', padding: 2}}>
            RSSI: {item.data.rssi}</Text>
          <Text style={{fontSize: 14, textAlign: 'left', color: '#333333', padding: 2}}>
            Name:{item.data.name == null ? item.data.localName : item.data.name}</Text>
          <Text style={{fontSize: 14, textAlign: 'left', color: '#333333', padding: 2}}>
            Id:{item.data.id}</Text>
          <Text style={{fontSize: 14, textAlign: 'left', color: '#333333', padding: 2}}>
            Connectable?:{ item.data.isConnectable ? 'True': 'False'}</Text>
          <Text style={{fontSize: 14, textAlign: 'left', color: '#333333', padding: 2,
           paddingBottom: 20}}>Connected?:{ item.data.isConnected() ? 'True': 'False'}</Text>
          
           
        </View>
      
    );
  }
  render() {
    return (
      <View style={styles.container}>
        <TouchableOpacity
          style={{ flex: 1 }}
          activeOpacity={0.9}
          onPress={this.GetListItem.bind(this, this.state.item)}
        >
        { this.renderCustItem(this.state.item)}        

        </TouchableOpacity>
      </View>
    );
  }
}
const styles = StyleSheet.create({
    container: {
      justifyContent: "center",
      alignItems: "center"
    },
    welcome: {
      flex: 1,
      fontSize: 20,
      textAlign: "center",
      margin: 5
    },
    welcomeHeader: {
      flex: 1,
      fontSize: 60,
      textAlign: "center",
      margin: 5
    },
    instructions: {
        textAlign: "center",
        color: "#333333",
        marginBottom: 5
      }
    });