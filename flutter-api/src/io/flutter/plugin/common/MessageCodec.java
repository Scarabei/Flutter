package io.flutter.plugin.common;

import java.nio.ByteBuffer;

public interface MessageCodec {
   ByteBuffer encodeMessage(Object var1);

   Object decodeMessage(ByteBuffer var1);
}
