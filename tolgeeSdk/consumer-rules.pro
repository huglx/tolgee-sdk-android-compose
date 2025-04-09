## Tolgee SDK consumer rules
## These rules will be applied to applications that include this SDK
#
## Keep SDK public API classes
#-keep class cz.fit.cvut.sdk.TolgeeSdk { *; }
#-keep class cz.fit.cvut.sdk.TolgeeSdkApi { *; }
#-keep class cz.fit.cvut.sdk.utils.TolgeeSdkMode { *; }
#-keep class cz.fit.cvut.sdk.TolgeeSdkProviderKt { *; }
#
## Keep DSL function
#-keep class cz.fit.cvut.sdk.TolgeeSdkKt { *; }
#
## Keep Room database schema
#-keep class cz.fit.cvut.feature.**.entity.** { *; }
#-keep class cz.fit.cvut.feature.**.dao.** { *; }
#
## Prevent warnings about SDK internal classes
#-dontwarn cz.fit.cvut.core.**
#-dontwarn cz.fit.cvut.feature.**
