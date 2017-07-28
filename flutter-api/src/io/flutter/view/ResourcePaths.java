package io.flutter.view;

import android.content.Context;
import java.io.File;
import java.io.IOException;

class ResourcePaths {
   public static final String TEMPORARY_RESOURCE_PREFIX = ".org.chromium.Chromium.";

   public static File createTempFile(Context context, String suffix) throws IOException {
      return File.createTempFile(".org.chromium.Chromium.", "_" + suffix, context.getCacheDir());
   }
}
