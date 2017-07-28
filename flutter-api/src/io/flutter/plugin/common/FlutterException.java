package io.flutter.plugin.common;

public class FlutterException extends RuntimeException {
   public final String code;
   public final Object details;

   FlutterException(String code, String message, Object details) {
      super(message);

      assert code != null;

      this.code = code;
      this.details = details;
   }
}
