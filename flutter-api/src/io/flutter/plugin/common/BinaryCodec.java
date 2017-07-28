
package io.flutter.plugin.common;

import java.nio.ByteBuffer;

public final class BinaryCodec implements MessageCodec {
	public static final BinaryCodec INSTANCE = new BinaryCodec();

	public ByteBuffer encodeMessage (final ByteBuffer message) {
		return message;
	}

	@Override
	public ByteBuffer decodeMessage (final ByteBuffer message) {
		return message;
	}

	@Override
	public ByteBuffer encodeMessage (final Object var1) {
		return null;
	}
}
