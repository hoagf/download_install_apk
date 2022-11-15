# download_install_apk

[![pub package](https://img.shields.io/pub/v/download_install_apk.svg)](https://pub.dev/packages/download_install_apk)

Download and install apk file

> Note: Only support Android

## Usage

To use this plugin, add `download_install_apk` as a [dependency in your pubspec.yaml file](https://flutter.io/platform-plugins/).

### Example


#### Add install permission to AndroidManifest.xml


```xml
<manifest>
    <uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />
    <uses-permission  android:name="android.permission.INSTALL_PACKAGES"
        tools:ignore="ProtectedPermissions" />
   
</manifest>
```

#### Install apk
```dart
DownloadInstallApk()
    .install(apkFilePath, )
    .listen((event) {
            log(event);
        }
    );
```


#### Download and Install apk
```dart
DownloadInstallApk()
    .execute('url')
    .listen((event) {
            logger.d(event);
        }
    );
```
