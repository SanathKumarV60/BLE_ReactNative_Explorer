//
//  RCTBLEPeripheralModule.m
//  ble_rn_explorer
//
//  Created by Sanath Varambally on 11/08/22.
//

#import <Foundation/Foundation.h>
#import <React/RCTLog.h>
#import "RCTBLEPeripheralModule.h"
@implementation RCTBLEPeripheralModule
RCT_EXPORT_MODULE(BLEPeripheralModuleHack);


- (NSArray<NSString *> *)supportedEvents
{
  return @[@"RCTBLEPeripheralModuleDeviceFound", @"RCTBLEPeripheralModuleStatus"];
}

//RCT_EXPORT_BLOCKING_SYNCHRONOUS_METHOD
RCT_EXPORT_METHOD(setup)
{
  if(!self.peripheralManager )
    self.peripheralManager =  [[CBPeripheralManager alloc] initWithDelegate:self queue:nil];
  self.serviceList = [[NSMutableArray alloc] init]; 
}
RCT_EXPORT_METHOD( addService:(NSString *)serviceName serviceUUID:(NSString *) serviceUUID Callback:(RCTResponseSenderBlock ) charecteristicWriteCallback)
{
  RCTLogInfo(@"adding service %@ at %@", serviceName, serviceUUID);
    BLEService *service = [[BLEService alloc] init];
    service.name = serviceName;
    service.uuid = serviceUUID;
    [self.serviceList addObject:service];
}
 

RCT_EXPORT_METHOD(addCharacteristicToService:(NSString *)serviceUUID charUUID:(NSString *) charUUID property:(NSNumber * _Nonnull) prop permission:(NSNumber * _Nonnull) perm
                  description: (NSString *) description)
{
  
  for (int i = 0; i < [self.serviceList count]; i++) {
    BLEService* service = [self.serviceList objectAtIndex: i];
    if(service.uuid == serviceUUID){
      BLECharacteristic *c = [ [BLECharacteristic alloc] init ];
      c.uuid = charUUID;
      c.prop = prop;
      c.permission = perm;
      if(!service.characteristicList )
        service.characteristicList = [[NSMutableArray alloc] init]; 
      [service.characteristicList addObject:c];
      RCTLogInfo(@"addCharacteristicToService %@ at %@", serviceUUID, description);

      break;
    }
     
  }
}

RCT_EXPORT_METHOD(enableBluetooth: (RCTResponseSenderBlock ) callback)
{
  RCTLogInfo(@"Pretending to enableBluetooth");

}
            
RCT_EXPORT_METHOD(sendWriteResponse:(NSString *) deviceAlias requestId:(double) requestId status:(double)
                  status characteristicUUID: (NSString *) characteristicUUID offset: (double) offset)
{
  RCTLogInfo(@"Pretending to sendWriteResponse %@ at %@", deviceAlias, characteristicUUID);

}

RCT_EXPORT_METHOD(startAdvertising)
{

  RCTLogInfo(@"Pretending to startAdvertising ");
  
  if (self.peripheralManager.state != CBPeripheralManagerStatePoweredOn) {
      RCTLogInfo(@"Peripheral is not powered on");
      return;
  }
  for (int i = 0; i < [self.serviceList count]; i++) {
    BLEService* service = [self.serviceList objectAtIndex: i];
    CBMutableService *mService = [[CBMutableService alloc] initWithType:[CBUUID UUIDWithString:service.uuid] primary:YES];
    for(int i=0; i < [ service.characteristicList count]; i++){
      BLECharacteristic *bleC = [ service.characteristicList objectAtIndex: i];

      CBMutableCharacteristic *mc = [[CBMutableCharacteristic alloc] initWithType:[CBUUID UUIDWithString:bleC.uuid] properties:CBCharacteristicPropertyRead value:nil permissions:CBAttributePermissionsReadable];
      //[ mService.characteristics ad: mc];
      mService.characteristics =@[mc];
    }
    
    [self.peripheralManager addService: mService];

    [self.peripheralManager startAdvertising:@{ CBAdvertisementDataServiceUUIDsKey : @[[CBUUID UUIDWithString:service.uuid]] }];
  }
  RCTLogInfo(@"Started");  
}

- (void)peripheralManagerDidUpdateState:(CBPeripheralManager *)peripheral {
 
    RCTLogInfo(@" peripheralManagerDidUpdateState %@", peripheral);

}
- (void)peripheralManager:(CBPeripheralManager *)peripheral didAddService:(CBService *)service error:(NSError *)error
{
  // log centralManager state
  RCTLogInfo(@"peripheralManager didAddService peripheral %@", peripheral);
  RCTLogInfo(@"peripheralManager didAddService service %@", service);
  RCTLogInfo(@"peripheralManager didAddService error %@", error);
}

- (void)peripheralManagerDidStartAdvertising:(CBPeripheralManager *)peripheral error:(NSError *)error
{
  RCTLogInfo(@"peripheralManagerDidStartAdvertising peripheral %@", peripheral);
  RCTLogInfo(@"peripheralManagerDidStartAdvertising error %@", error);
}



@end
