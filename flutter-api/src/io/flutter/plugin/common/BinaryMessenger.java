package io.flutter.plugin.common;

import java.nio.ByteBuffer;

public interface BinaryMessenger {
   void send(String var1, ByteBuffer var2);

   void send(String var1, ByteBuffer var2, BinaryMessenger.BinaryReply var3);

   void setMessageHandler(String var1, BinaryMessenger.BinaryMessageHandler var2);

   public interface BinaryReply {
      void reply(ByteBuffer var1);
   }

   public interface BinaryMessageHandler {
      void onMessage(ByteBuffer var1, BinaryMessenger.BinaryReply var2);
   }
}
