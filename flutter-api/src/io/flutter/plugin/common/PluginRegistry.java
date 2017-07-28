package io.flutter.plugin.common;

import android.app.Activity;
import android.content.Intent;
import io.flutter.view.FlutterView;

public interface PluginRegistry {
   PluginRegistry.Registrar registrarFor(String var1);

   boolean hasPlugin(String var1);

   Object valuePublishedByPlugin(String var1);

   public interface UserLeaveHintListener {
      void onUserLeaveHint();
   }

   public interface NewIntentListener {
      boolean onNewIntent(Intent var1);
   }

   public interface ActivityResultListener {
      boolean onActivityResult(int var1, int var2, Intent var3);
   }

   public interface RequestPermissionResultListener {
      boolean onRequestPermissionResult(int var1, String[] var2, int[] var3);
   }

   public interface Registrar {
      Activity activity();

      BinaryMessenger messenger();

      FlutterView view();

      PluginRegistry.Registrar publish(Object var1);

      PluginRegistry.Registrar addRequestPermissionResultListener(PluginRegistry.RequestPermissionResultListener var1);

      PluginRegistry.Registrar addActivityResultListener(PluginRegistry.ActivityResultListener var1);

      PluginRegistry.Registrar addNewIntentListener(PluginRegistry.NewIntentListener var1);

      PluginRegistry.Registrar addUserLeaveHintListener(PluginRegistry.UserLeaveHintListener var1);
   }
}
