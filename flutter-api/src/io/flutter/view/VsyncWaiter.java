package io.flutter.view;

import android.view.Choreographer;
import android.view.Choreographer.FrameCallback;

public class VsyncWaiter {
   public static void asyncWaitForVsync(final long cookie) {
      Choreographer.getInstance().postFrameCallback(new FrameCallback() {
         public void doFrame(long frameTimeNanos) {
            VsyncWaiter.nativeOnVsync(frameTimeNanos, cookie);
         }
      });
   }

   private static native void nativeOnVsync(long var0, long var2);
}
