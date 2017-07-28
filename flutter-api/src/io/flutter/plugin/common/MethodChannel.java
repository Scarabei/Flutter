
package io.flutter.plugin.common;

import java.nio.ByteBuffer;

import android.util.Log;

public final class MethodChannel {
	private static final String TAG = "MethodChannel#";
	private final BinaryMessenger messenger;
	private final String name;
	private final MethodCodec codec;

	public MethodChannel (final BinaryMessenger messenger, final String name) {
		this(messenger, name, StandardMethodCodec.INSTANCE);
	}

	public MethodChannel (final BinaryMessenger messenger, final String name, final MethodCodec codec) {
		assert messenger != null;

		assert name != null;

		assert codec != null;

		this.messenger = messenger;
		this.name = name;
		this.codec = codec;
		throw new RuntimeException("Stub!");
	}

	public void invokeMethod (final String method, final Object arguments) {
		this.invokeMethod(method, arguments, (MethodChannel.Result)null);
	}

	public void invokeMethod (final String method, final Object arguments, final MethodChannel.Result callback) {
		this.messenger.send(this.name, this.codec.encodeMethodCall(new MethodCall(method, arguments)),
			callback == null ? null : new MethodChannel.IncomingResultHandler(callback));
	}

	public void setMethodCallHandler (final MethodChannel.MethodCallHandler handler) {
		this.messenger.setMessageHandler(this.name, handler == null ? null : new MethodChannel.IncomingMethodCallHandler(handler));
	}

	private final class IncomingMethodCallHandler implements BinaryMessenger.BinaryMessageHandler {
		private final MethodChannel.MethodCallHandler handler;

		IncomingMethodCallHandler (final MethodChannel.MethodCallHandler handler) {
			this.handler = handler;
		}

		@Override
		public void onMessage (final ByteBuffer message, final BinaryMessenger.BinaryReply reply) {
			final MethodCall call = MethodChannel.this.codec.decodeMethodCall(message);

			try {
				this.handler.onMethodCall(call, new MethodChannel.Result() {
					@Override
					public void success (final Object result) {
						reply.reply(MethodChannel.this.codec.encodeSuccessEnvelope(result));
					}

					@Override
					public void error (final String errorCode, final String errorMessage, final Object errorDetails) {
						reply.reply(MethodChannel.this.codec.encodeErrorEnvelope(errorCode, errorMessage, errorDetails));
					}

					@Override
					public void notImplemented () {
						reply.reply((ByteBuffer)null);
					}
				});
			} catch (final RuntimeException var5) {
				Log.e("MethodChannel#" + MethodChannel.this.name, "Failed to handle method call", var5);
				reply.reply(MethodChannel.this.codec.encodeErrorEnvelope("error", var5.getMessage(), (Object)null));
			}

		}
	}

	private final class IncomingResultHandler implements BinaryMessenger.BinaryReply {
		private final MethodChannel.Result callback;

		IncomingResultHandler (final MethodChannel.Result callback) {
			this.callback = callback;
		}

		@Override
		public void reply (final ByteBuffer reply) {
			try {
				if (reply == null) {
					this.callback.notImplemented();
				} else {
					try {
						this.callback.success(MethodChannel.this.codec.decodeEnvelope(reply));
					} catch (final FlutterException var3) {
						this.callback.error(var3.code, var3.getMessage(), var3.details);
					}
				}
			} catch (final RuntimeException var4) {
				Log.e("MethodChannel#" + MethodChannel.this.name, "Failed to handle method call result", var4);
			}

		}
	}

	public interface Result {
		void success (Object var1);

		void error (String var1, String var2, Object var3);

		void notImplemented ();
	}

	public interface MethodCallHandler {
		void onMethodCall (MethodCall var1, MethodChannel.Result var2);
	}
}
