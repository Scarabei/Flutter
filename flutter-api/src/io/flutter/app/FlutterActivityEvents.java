package io.flutter.app;

import android.content.ComponentCallbacks2;
import android.content.Intent;
import android.os.Bundle;
import io.flutter.plugin.common.PluginRegistry;

public interface FlutterActivityEvents extends ComponentCallbacks2, PluginRegistry.ActivityResultListener, PluginRegistry.RequestPermissionResultListener {
   void onCreate(Bundle var1);

   void onNewIntent(Intent var1);

   void onPause();

   void onResume();

   void onPostResume();

   void onDestroy();

   boolean onBackPressed();

   void onUserLeaveHint();
}
