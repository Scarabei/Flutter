package io.flutter.plugin.common;

import java.nio.ByteBuffer;

public interface MethodCodec {
   ByteBuffer encodeMethodCall(MethodCall var1);

   MethodCall decodeMethodCall(ByteBuffer var1);

   ByteBuffer encodeSuccessEnvelope(Object var1);

   ByteBuffer encodeErrorEnvelope(String var1, String var2, Object var3);

   Object decodeEnvelope(ByteBuffer var1);
}
