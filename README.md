# cordova-plugin-android-mediastore
This simple Cordova plugin converts a base64 `image/png` bytestring to an image file and adds it to the gallery.

The image file part was tested on Android APIs 22(5.1 Lollipop), 29(10 Q) and 30(11 R).

Useful when capturing an HTML canvas.


Support was added for saving a base64 `video/mp4` bytestring as a .mp4 video.

The use of the Android MediaStore Api is intended to supports the scoped storage permission policy on google play store for Android 10 devices and higher (API >= 29) 

## API
The plugin exports only one function:

```javascript
//CordovaAndroidMediaStore.js

/**
 * @param byteString the bytestring part of the base64 dataURI (excludes the MIME part, see the example)
 * @param fileDir the relative directory to save the file to.
 * @param fileName
 * @param type - 'picture' or 'video'
 */
exports.store = function (byteString, fileDir, fileName, type, success, error) {
    exec(success, error, 
        'CordovaAndroidMediaStore', 
        'store', 
        [byteString, fileDir, fileName, type]);
};
```

## example
```typescript
let dataURItoGallery: (dataURI: string) => Blob = (dataURI) => {
    // Get bytestring part of the image dataURI
    let byteString = dataURI.split(",")[1];
    cordova.plugins.CordovaAndroidMediaStore.store(byteString, "Pictures", `${Date.now()}.png`, "picture");
};
```

## Use in Ionic

You can use this non-ionic-native plugin in ionic by including (next the the staring import statements)
```javascript
declare var cordova: any;
```
and then calling the plugin in your main code via
```javascript
await cordova.plugins.CordovaAndroidMediaStore.store
```
(found on https://github.com/ionic-team/ionic-native/issues/525#issuecomment-245130928 and https://forum.ionicframework.com/t/how-to-use-non-native-plugins-on-ionic/63936/53)

Example use in Ionic:

```typescript
await cordova.plugins.CordovaAndroidMediaStore.store(byteString,
  'Videos',
  moment(new Date()).format('YYYY-MM-DD') + ext,
  'video');
```
