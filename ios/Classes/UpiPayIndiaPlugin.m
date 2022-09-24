#import "UpiPayIndiaPlugin.h"
#if __has_include(<upi_pay_india/upi_pay_india-Swift.h>)
#import <upi_pay_india/upi_pay_india-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "upi_pay_india-Swift.h"
#endif

@implementation UpiPayIndiaPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftUpiPayIndiaPlugin registerWithRegistrar:registrar];
}
@end
