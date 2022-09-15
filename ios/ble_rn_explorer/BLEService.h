#import <Foundation/Foundation.h>

@interface BLEService : NSObject 
{
    NSString *name;
    NSString *uuid;
    NSMutableArray *characteristicList;
}

@property(nonatomic, retain) NSString *name;
@property(nonatomic, retain) NSString *uuid;
@property (nonatomic, strong) NSMutableArray *characteristicList;
@end