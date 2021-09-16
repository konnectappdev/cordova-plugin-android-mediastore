var exec = require('cordova/exec');

exports.store = function (byteString, fileDir, fileName, type, success, error) {
    exec(success, error,
      'CordovaAndroidMediaStore',
      'store',
      [byteString, fileDir, fileName, type]);
};
