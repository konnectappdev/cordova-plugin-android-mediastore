package com.heartade;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import android.provider.MediaStore;
import android.os.Build;
import android.content.ContentValues;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * This class downloads photos and videos using AndroidMediaStore.
 * Adheres to device storage permission policy on Google play store for Android OS 10 and 11 / API level > 29 devices.
 */
public class CordovaAndroidMediaStore extends CordovaPlugin {

    public CallbackContext callbackContext;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        this.callbackContext = callbackContext;

        if (action.equals("store")) {
            boolean result = this.store(args.getString(0), args.getString(1), args.getString(2), args.getString(3), callbackContext);
            if (result) {
                PluginResult pluginResult = new PluginResult(PluginResult.Status.OK);
                callbackContext.sendPluginResult(pluginResult);
                callbackContext.success("true");
                return true;
            }
        }
        return false;
    }

    private boolean store(String byteString, String fileDir, String fileName, String type, CallbackContext callbackContext) {
        this.callbackContext = callbackContext;
        final CordovaInterface _cordova = cordova;

        try {

            cordova.getThreadPool().execute(new Runnable() {
                CordovaInterface cordova = _cordova;

                @Override
                public void run() {
                    if (type.equals("picture")) {
                        try {
                            byte[] byteArray = Base64.decode(byteString, Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                            Context context = this.cordova.getActivity();
                            ContentResolver contentResolver = context.getContentResolver();
                            if (Build.VERSION.SDK_INT >= 29) {
                                //todo: do not compress image into PNG -> use original JPEg instead.
                                final ContentValues contentValues = new ContentValues();
                                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
                                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, fileDir);
                                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 1);

                                Uri imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

                                OutputStream out = contentResolver.openOutputStream(imageUri);
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);

                                contentValues.clear();
                                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0);
                                contentResolver.update(imageUri, contentValues, null, null);
                            } else {
                                File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                                File dir = new File(path + "/" + fileDir);
                                dir.mkdirs();
                                File file = new File(dir, fileName);
                                FileOutputStream out = new FileOutputStream(file);
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                                Uri contentUri = Uri.fromFile(file);
                                context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, contentUri));
                            }
                            callbackContext.success();
                        } catch (RuntimeException | IOException e) {
                            e.printStackTrace();
                            callbackContext.error(e.getMessage());
                        }
                    } else if (type.equals("video")) {
                        try {
                            if (Build.VERSION.SDK_INT >= 29) {
                                byte[] byteArray = Base64.decode(byteString, Base64.DEFAULT);
                                Context context = this.cordova.getActivity();
                                ContentResolver contentResolver = context.getContentResolver();
                                final ContentValues contentValues = new ContentValues();
                                contentValues.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/" + fileDir);
                                contentValues.put(MediaStore.Video.Media.TITLE, fileName);
                                contentValues.put(MediaStore.Video.Media.DISPLAY_NAME, fileName);
                                contentValues.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
                                contentValues.put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
                                contentValues.put(MediaStore.Video.Media.DATE_TAKEN, System.currentTimeMillis());
                                contentValues.put(MediaStore.Video.Media.IS_PENDING, 1);

                                Uri videoUri = contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues);

                                OutputStream out = contentResolver.openOutputStream(videoUri);
                                out.write(byteArray);
                                out.close();

                                contentValues.clear();
                                contentValues.put(MediaStore.Video.Media.IS_PENDING, 0);
                                contentResolver.update(videoUri, contentValues, null, null);
                            } else {
                                //not implemented
                            }
                            callbackContext.success();
                        } catch (RuntimeException | IOException e) {
                            e.printStackTrace();
                            callbackContext.error(e.getMessage());
                        }
                    }
                }

            });
            return true;
        } catch(Exception e) {
            e.printStackTrace();
            callbackContext.error(e.getMessage());
            return false;
        }
    }
}
