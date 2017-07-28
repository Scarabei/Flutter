
package io.flutter.plugin.common;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public final class StringCodec implements MessageCodec {
	private static final Charset UTF8 = Charset.forName("UTF8");
	public static final StringCodec INSTANCE = new StringCodec();

	public ByteBuffer encodeMessage (final String message) {
		if (message == null) {
			return null;
		} else {
			final byte[] bytes = message.getBytes(UTF8);
			final ByteBuffer buffer = ByteBuffer.allocateDirect(bytes.length);
			buffer.put(bytes);
			return buffer;
		}
	}

	@Override
	public String decodeMessage (final ByteBuffer message) {
		if (message == null) {
			return null;
		} else {
			final int length = message.remaining();
			byte[] bytes;
			int offset;
			if (message.hasArray()) {
				bytes = message.array();
				offset = message.arrayOffset();
			} else {
				bytes = new byte[length];
				message.get(bytes);
				offset = 0;
			}

			return new String(bytes, offset, length, UTF8);
		}
	}

	@Override
	public ByteBuffer encodeMessage (final Object var1) {
		return null;
	}
}
