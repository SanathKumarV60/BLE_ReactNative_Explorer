#import <Foundation/Foundation.h>

@interface BLECharacteristic : NSObject 
{
NSString *uuid;
NSNumber *prop;
NSNumber *permission;

}
@property(nonatomic, retain) NSString *uuid;
@property(nonatomic, retain) NSNumber *prop;
@property(nonatomic, retain) NSNumber *permission;

@end