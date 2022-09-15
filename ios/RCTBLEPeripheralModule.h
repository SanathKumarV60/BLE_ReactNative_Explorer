//
//  RCTBLEPeripheralModule.h
//  ble_rn_explorer
//
//  Created by Sanath Varambally on 11/08/22.
//

#ifndef RCTBLEPeripheralModule_h

#define RCTBLEPeripheralModule_h
#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>
#import "BLECharacteristic.h"
#import "BLEService.h"
@import CoreBluetooth;
@import QuartzCore;
@interface RCTBLEPeripheralModule : 
     RCTEventEmitter <RCTBridgeModule,  CBPeripheralManagerDelegate, CBPeripheralDelegate>

@property (nonatomic, strong) CBPeripheralManager *peripheralManager;
@property (nonatomic, strong) CBMutableCharacteristic *pkCharacteristic;
@property (nonatomic, strong) NSMutableArray *serviceList;
@end

#endif /* RCTBLEPeripheralModule_h */
