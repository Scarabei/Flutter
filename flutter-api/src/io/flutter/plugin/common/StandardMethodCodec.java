package io.flutter.plugin.common;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class StandardMethodCodec implements MethodCodec {
   public static final StandardMethodCodec INSTANCE = new StandardMethodCodec();

   public ByteBuffer encodeMethodCall(MethodCall methodCall) {
      StandardMessageCodec.ExposedByteArrayOutputStream stream = new StandardMessageCodec.ExposedByteArrayOutputStream();
      StandardMessageCodec.writeValue(stream, methodCall.method);
      StandardMessageCodec.writeValue(stream, methodCall.arguments);
      ByteBuffer buffer = ByteBuffer.allocateDirect(stream.size());
      buffer.put(stream.buffer(), 0, stream.size());
      return buffer;
   }

   public MethodCall decodeMethodCall(ByteBuffer methodCall) {
      methodCall.order(ByteOrder.nativeOrder());
      Object method = StandardMessageCodec.readValue(methodCall);
      Object arguments = StandardMessageCodec.readValue(methodCall);
      if(method instanceof String && !methodCall.hasRemaining()) {
         return new MethodCall((String)method, arguments);
      } else {
         throw new IllegalArgumentException("Method call corrupted");
      }
   }

   public ByteBuffer encodeSuccessEnvelope(Object result) {
      StandardMessageCodec.ExposedByteArrayOutputStream stream = new StandardMessageCodec.ExposedByteArrayOutputStream();
      stream.write(0);
      StandardMessageCodec.writeValue(stream, result);
      ByteBuffer buffer = ByteBuffer.allocateDirect(stream.size());
      buffer.put(stream.buffer(), 0, stream.size());
      return buffer;
   }

   public ByteBuffer encodeErrorEnvelope(String errorCode, String errorMessage, Object errorDetails) {
      StandardMessageCodec.ExposedByteArrayOutputStream stream = new StandardMessageCodec.ExposedByteArrayOutputStream();
      stream.write(1);
      StandardMessageCodec.writeValue(stream, errorCode);
      StandardMessageCodec.writeValue(stream, errorMessage);
      StandardMessageCodec.writeValue(stream, errorDetails);
      ByteBuffer buffer = ByteBuffer.allocateDirect(stream.size());
      buffer.put(stream.buffer(), 0, stream.size());
      return buffer;
   }

   public Object decodeEnvelope(ByteBuffer envelope) {
      envelope.order(ByteOrder.nativeOrder());
      byte flag = envelope.get();
      Object code;
      switch(flag) {
      case 0:
         code = StandardMessageCodec.readValue(envelope);
         if(!envelope.hasRemaining()) {
            return code;
         }
      case 1:
         code = StandardMessageCodec.readValue(envelope);
         Object message = StandardMessageCodec.readValue(envelope);
         Object details = StandardMessageCodec.readValue(envelope);
         if(code instanceof String && (message == null || message instanceof String) && !envelope.hasRemaining()) {
            throw new FlutterException((String)code, (String)message, details);
         }
      default:
         throw new IllegalArgumentException("Envelope corrupted");
      }
   }
}
