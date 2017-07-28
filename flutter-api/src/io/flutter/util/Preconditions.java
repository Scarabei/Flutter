package io.flutter.util;

public final class Preconditions {
   public static Object checkNotNull(Object reference) {
      if(reference == null) {
         throw new NullPointerException();
      } else {
         return reference;
      }
   }
}
