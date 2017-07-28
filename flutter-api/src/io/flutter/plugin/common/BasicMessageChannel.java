
package io.flutter.plugin.common;

import java.nio.ByteBuffer;

import android.util.Log;

public final class BasicMessageChannel {
	private static final String TAG = "BasicMessageChannel#";
	private final BinaryMessenger messenger;
	private final String name;
	private final MessageCodec codec;

	public BasicMessageChannel (final BinaryMessenger messenger, final String name, final MessageCodec codec) {
		assert messenger != null;

		assert name != null;

		assert codec != null;

		this.messenger = messenger;
		this.name = name;
		this.codec = codec;
		throw new RuntimeException("Stub!");
	}

	public void send (final Object message) {
		this.send(message, (BasicMessageChannel.Reply)null);
	}

	public void send (final Object message, final BasicMessageChannel.Reply callback) {
		this.messenger.send(this.name, this.codec.encodeMessage(message),
			callback == null ? null : new BasicMessageChannel.IncomingReplyHandler(callback));
	}

	public void setMessageHandler (final BasicMessageChannel.MessageHandler handler) {
		this.messenger.setMessageHandler(this.name,
			handler == null ? null : new BasicMessageChannel.IncomingMessageHandler(handler));
	}

	private final class IncomingMessageHandler implements BinaryMessenger.BinaryMessageHandler {
		private final BasicMessageChannel.MessageHandler handler;

		private IncomingMessageHandler (final BasicMessageChannel.MessageHandler handler) {
			this.handler = handler;
		}

		@Override
		public void onMessage (final ByteBuffer message, final BinaryMessenger.BinaryReply callback) {
			try {
				this.handler.onMessage(BasicMessageChannel.this.codec.decodeMessage(message), new BasicMessageChannel.Reply() {
					@Override
					public void reply (final Object reply) {
						callback.reply(BasicMessageChannel.this.codec.encodeMessage(reply));
					}
				});
			} catch (final RuntimeException var4) {
				Log.e("BasicMessageChannel#" + BasicMessageChannel.this.name, "Failed to handle message", var4);
				callback.reply((ByteBuffer)null);
			}

		}

		// $FF: synthetic method
		IncomingMessageHandler (final BasicMessageChannel.MessageHandler x1, final Object x2) {
			this(x1);
		}
	}

	private final class IncomingReplyHandler implements BinaryMessenger.BinaryReply {
		private final BasicMessageChannel.Reply callback;

		private IncomingReplyHandler (final BasicMessageChannel.Reply callback) {
			this.callback = callback;
		}

		@Override
		public void reply (final ByteBuffer reply) {
			try {
				this.callback.reply(BasicMessageChannel.this.codec.decodeMessage(reply));
			} catch (final RuntimeException var3) {
				Log.e("BasicMessageChannel#" + BasicMessageChannel.this.name, "Failed to handle message reply", var3);
			}

		}

		// $FF: synthetic method
		IncomingReplyHandler (final BasicMessageChannel.Reply x1, final Object x2) {
			this(x1);
		}
	}

	public interface Reply {
		void reply (Object var1);
	}

	public interface MessageHandler {
		void onMessage (Object var1, BasicMessageChannel.Reply var2);
	}
}
