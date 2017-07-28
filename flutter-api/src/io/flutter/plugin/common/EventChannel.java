package io.flutter.plugin.common;

import android.util.Log;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public final class EventChannel {
   private static final String TAG = "EventChannel#";
   private final BinaryMessenger messenger;
   private final String name;
   private final MethodCodec codec;

   public EventChannel(BinaryMessenger messenger, String name) {
      this(messenger, name, StandardMethodCodec.INSTANCE);
   }

   public EventChannel(BinaryMessenger messenger, String name, MethodCodec codec) {
      assert messenger != null;

      assert name != null;

      assert codec != null;

      this.messenger = messenger;
      this.name = name;
      this.codec = codec;
   }

   public void setStreamHandler(EventChannel.StreamHandler handler) {
      this.messenger.setMessageHandler(this.name, handler == null?null:new EventChannel.IncomingStreamRequestHandler(handler));
   }

   private final class IncomingStreamRequestHandler implements BinaryMessenger.BinaryMessageHandler {
      private final EventChannel.StreamHandler handler;
      private final AtomicReference activeSink = new AtomicReference((Object)null);

      IncomingStreamRequestHandler(EventChannel.StreamHandler handler) {
         this.handler = handler;
      }

      public void onMessage(ByteBuffer message, BinaryMessenger.BinaryReply reply) {
         MethodCall call = EventChannel.this.codec.decodeMethodCall(message);
         if(call.method.equals("listen")) {
            this.onListen(call.arguments, reply);
         } else if(call.method.equals("cancel")) {
            this.onCancel(call.arguments, reply);
         } else {
            reply.reply((ByteBuffer)null);
         }

      }

      private void onListen(Object arguments, BinaryMessenger.BinaryReply callback) {
         EventChannel.EventSink eventSink = new EventChannel.IncomingStreamRequestHandler.EventSinkImplementation();
         if(this.activeSink.compareAndSet((Object)null, eventSink)) {
            try {
               this.handler.onListen(arguments, eventSink);
               callback.reply(EventChannel.this.codec.encodeSuccessEnvelope((Object)null));
            } catch (RuntimeException var5) {
               this.activeSink.set((Object)null);
               Log.e("EventChannel#" + EventChannel.this.name, "Failed to open event stream", var5);
               callback.reply(EventChannel.this.codec.encodeErrorEnvelope("error", var5.getMessage(), (Object)null));
            }
         } else {
            callback.reply(EventChannel.this.codec.encodeErrorEnvelope("error", "Stream already active", (Object)null));
         }

      }

      private void onCancel(Object arguments, BinaryMessenger.BinaryReply callback) {
         EventChannel.EventSink oldSink = (EventChannel.EventSink)this.activeSink.getAndSet((Object)null);
         if(oldSink != null) {
            try {
               this.handler.onCancel(arguments);
               callback.reply(EventChannel.this.codec.encodeSuccessEnvelope((Object)null));
            } catch (RuntimeException var5) {
               Log.e("EventChannel#" + EventChannel.this.name, "Failed to close event stream", var5);
               callback.reply(EventChannel.this.codec.encodeErrorEnvelope("error", var5.getMessage(), (Object)null));
            }
         } else {
            callback.reply(EventChannel.this.codec.encodeErrorEnvelope("error", "No active stream to cancel", (Object)null));
         }

      }

      private final class EventSinkImplementation implements EventChannel.EventSink {
         final AtomicBoolean hasEnded;

         private EventSinkImplementation() {
            this.hasEnded = new AtomicBoolean(false);
         }

         public void success(Object event) {
            if(!this.hasEnded.get() && IncomingStreamRequestHandler.this.activeSink.get() == this) {
               EventChannel.this.messenger.send(EventChannel.this.name, EventChannel.this.codec.encodeSuccessEnvelope(event));
            }
         }

         public void error(String errorCode, String errorMessage, Object errorDetails) {
            if(!this.hasEnded.get() && IncomingStreamRequestHandler.this.activeSink.get() == this) {
               EventChannel.this.messenger.send(EventChannel.this.name, EventChannel.this.codec.encodeErrorEnvelope(errorCode, errorMessage, errorDetails));
            }
         }

         public void endOfStream() {
            if(!this.hasEnded.getAndSet(true) && IncomingStreamRequestHandler.this.activeSink.get() == this) {
               EventChannel.this.messenger.send(EventChannel.this.name, (ByteBuffer)null);
            }
         }

         // $FF: synthetic method
         EventSinkImplementation(Object x1) {
            this();
         }
      }
   }

   public interface EventSink {
      void success(Object var1);

      void error(String var1, String var2, Object var3);

      void endOfStream();
   }

   public interface StreamHandler {
      void onListen(Object var1, EventChannel.EventSink var2);

      void onCancel(Object var1);
   }
}
