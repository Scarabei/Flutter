
package io.flutter.plugin.common;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public final class StandardMessageCodec implements MessageCodec {
	public static final StandardMessageCodec INSTANCE = new StandardMessageCodec();
	private static final boolean LITTLE_ENDIAN;
	private static final Charset UTF8;
	private static final byte NULL = 0;
	private static final byte TRUE = 1;
	private static final byte FALSE = 2;
	private static final byte INT = 3;
	private static final byte LONG = 4;
	private static final byte BIGINT = 5;
	private static final byte DOUBLE = 6;
	private static final byte STRING = 7;
	private static final byte BYTE_ARRAY = 8;
	private static final byte INT_ARRAY = 9;
	private static final byte LONG_ARRAY = 10;
	private static final byte DOUBLE_ARRAY = 11;
	private static final byte LIST = 12;
	private static final byte MAP = 13;

	@Override
	public ByteBuffer encodeMessage (final Object message) {
		if (message == null) {
			return null;
		} else {
			final StandardMessageCodec.ExposedByteArrayOutputStream stream = new StandardMessageCodec.ExposedByteArrayOutputStream();
			writeValue(stream, message);
			final ByteBuffer buffer = ByteBuffer.allocateDirect(stream.size());
			buffer.put(stream.buffer(), 0, stream.size());
			return buffer;
		}
	}

	@Override
	public Object decodeMessage (final ByteBuffer message) {
		if (message == null) {
			return null;
		} else {
			message.order(ByteOrder.nativeOrder());
			final Object value = readValue(message);
			if (message.hasRemaining()) {
				throw new IllegalArgumentException("Message corrupted");
			} else {
				return value;
			}
		}
	}

	private static void writeSize (final ByteArrayOutputStream stream, final int value) {
		assert 0 <= value;

		if (value < 254) {
			stream.write(value);
		} else if (value <= '\uffff') {
			stream.write(254);
			writeChar(stream, value);
		} else {
			stream.write(255);
			writeInt(stream, value);
		}

	}

	private static void writeChar (final ByteArrayOutputStream stream, final int value) {
		if (LITTLE_ENDIAN) {
			stream.write(value);
			stream.write(value >>> 8);
		} else {
			stream.write(value >>> 8);
			stream.write(value);
		}

	}

	private static void writeInt (final ByteArrayOutputStream stream, final int value) {
		if (LITTLE_ENDIAN) {
			stream.write(value);
			stream.write(value >>> 8);
			stream.write(value >>> 16);
			stream.write(value >>> 24);
		} else {
			stream.write(value >>> 24);
			stream.write(value >>> 16);
			stream.write(value >>> 8);
			stream.write(value);
		}

	}

	private static void writeLong (final ByteArrayOutputStream stream, final long value) {
		if (LITTLE_ENDIAN) {
			stream.write((byte)((int)value));
			stream.write((byte)((int)(value >>> 8)));
			stream.write((byte)((int)(value >>> 16)));
			stream.write((byte)((int)(value >>> 24)));
			stream.write((byte)((int)(value >>> 32)));
			stream.write((byte)((int)(value >>> 40)));
			stream.write((byte)((int)(value >>> 48)));
			stream.write((byte)((int)(value >>> 56)));
		} else {
			stream.write((byte)((int)(value >>> 56)));
			stream.write((byte)((int)(value >>> 48)));
			stream.write((byte)((int)(value >>> 40)));
			stream.write((byte)((int)(value >>> 32)));
			stream.write((byte)((int)(value >>> 24)));
			stream.write((byte)((int)(value >>> 16)));
			stream.write((byte)((int)(value >>> 8)));
			stream.write((byte)((int)value));
		}

	}

	private static void writeDouble (final ByteArrayOutputStream stream, final double value) {
		writeLong(stream, Double.doubleToLongBits(value));
	}

	private static void writeBytes (final ByteArrayOutputStream stream, final byte[] bytes) {
		writeSize(stream, bytes.length);
		stream.write(bytes, 0, bytes.length);
	}

	private static void writeAlignment (final ByteArrayOutputStream stream, final int alignment) {
		final int mod = stream.size() % alignment;
		if (mod != 0) {
			for (int i = 0; i < alignment - mod; ++i) {
				stream.write(0);
			}
		}

	}

	static void writeValue (final ByteArrayOutputStream stream, final Object value) {
		if (value == null) {
			stream.write(0);
		} else if (value == Boolean.TRUE) {
			stream.write(1);
		} else if (value == Boolean.FALSE) {
			stream.write(2);
		} else if (value instanceof Number) {
			if (!(value instanceof Integer) && !(value instanceof Short) && !(value instanceof Byte)) {
				if (value instanceof Long) {
					stream.write(4);
					writeLong(stream, ((Long)value).longValue());
				} else if (!(value instanceof Float) && !(value instanceof Double)) {
					if (!(value instanceof BigInteger)) {
						throw new IllegalArgumentException("Unsupported Number type: " + value.getClass());
					}

					stream.write(5);
					writeBytes(stream, ((BigInteger)value).toString(16).getBytes(UTF8));
				} else {
					stream.write(6);
					writeAlignment(stream, 8);
					writeDouble(stream, ((Number)value).doubleValue());
				}
			} else {
				stream.write(3);
				writeInt(stream, ((Number)value).intValue());
			}
		} else if (value instanceof String) {
			stream.write(7);
			writeBytes(stream, ((String)value).getBytes(UTF8));
		} else if (value instanceof byte[]) {
			stream.write(8);
			writeBytes(stream, ((byte[])value));
		} else {
			int var4;
			int var5;
			if (value instanceof int[]) {
				stream.write(9);
				final int[] array = ((int[])value);
				writeSize(stream, array.length);
				writeAlignment(stream, 4);
				final int[] var3 = array;
				var4 = array.length;

				for (var5 = 0; var5 < var4; ++var5) {
					final int n = var3[var5];
					writeInt(stream, n);
				}
			} else if (value instanceof long[]) {
				stream.write(10);
				final long[] array = ((long[])value);
				writeSize(stream, array.length);
				writeAlignment(stream, 8);
				final long[] var11 = array;
				var4 = array.length;

				for (var5 = 0; var5 < var4; ++var5) {
					final long n = var11[var5];
					writeLong(stream, n);
				}
			} else if (value instanceof double[]) {
				stream.write(11);
				final double[] array = ((double[])value);
				writeSize(stream, array.length);
				writeAlignment(stream, 8);
				final double[] var13 = array;
				var4 = array.length;

				for (var5 = 0; var5 < var4; ++var5) {
					final double d = var13[var5];
					writeDouble(stream, d);
				}
			} else {
				Iterator var14;
				if (value instanceof List) {
					stream.write(12);
					final List list = (List)value;
					writeSize(stream, list.size());
					var14 = list.iterator();

					while (var14.hasNext()) {
						final Object o = var14.next();
						writeValue(stream, o);
					}
				} else {
					if (!(value instanceof Map)) {
						throw new IllegalArgumentException("Unsupported value: " + value);
					}

					stream.write(13);
					final Map map = (Map)value;
					writeSize(stream, map.size());
					var14 = map.entrySet().iterator();

					while (var14.hasNext()) {
						final Entry entry = (Entry)var14.next();
						writeValue(stream, entry.getKey());
						writeValue(stream, entry.getValue());
					}
				}
			}
		}

	}

	private static int readSize (final ByteBuffer buffer) {
		if (!buffer.hasRemaining()) {
			throw new IllegalArgumentException("Message corrupted");
		} else {
			final int value = buffer.get() & 255;
			return value < 254 ? value : (value == 254 ? buffer.getChar() : buffer.getInt());
		}
	}

	private static byte[] readBytes (final ByteBuffer buffer) {
		final int length = readSize(buffer);
		final byte[] bytes = new byte[length];
		buffer.get(bytes);
		return bytes;
	}

	private static void readAlignment (final ByteBuffer buffer, final int alignment) {
		final int mod = buffer.position() % alignment;
		if (mod != 0) {
			buffer.position(buffer.position() + alignment - mod);
		}

	}

	static Object readValue (final ByteBuffer buffer) {
		if (!buffer.hasRemaining()) {
			throw new IllegalArgumentException("Message corrupted");
		} else {
			Object result;
			int size;
			int i;
			byte[] bytes;
			switch (buffer.get()) {
			case 0:
				result = null;
				break;
			case 1:
				result = Boolean.valueOf(true);
				break;
			case 2:
				result = Boolean.valueOf(false);
				break;
			case 3:
				result = Integer.valueOf(buffer.getInt());
				break;
			case 4:
				result = Long.valueOf(buffer.getLong());
				break;
			case 5:
				bytes = readBytes(buffer);
				result = new BigInteger(new String(bytes, UTF8), 16);
				break;
			case 6:
				readAlignment(buffer, 8);
				result = Double.valueOf(buffer.getDouble());
				break;
			case 7:
				bytes = readBytes(buffer);
				result = new String(bytes, UTF8);
				break;
			case 8:
				result = readBytes(buffer);
				break;
			case 9:
				size = readSize(buffer);
				final int[] array = new int[size];
				readAlignment(buffer, 4);
				buffer.asIntBuffer().get(array);
				result = array;
				buffer.position(buffer.position() + 4 * size);
				break;
			case 10:
				size = readSize(buffer);
// long[] array = new long[size];
				readAlignment(buffer, 8);
// buffer.asLongBuffer().get(array);
// result = array;
				buffer.position(buffer.position() + 8 * size);
				break;
			case 11:
				size = readSize(buffer);
// double[] array = new double[size];
				readAlignment(buffer, 8);
// buffer.asDoubleBuffer().get(array);
// result = array;
				buffer.position(buffer.position() + 8 * size);
				break;
			case 12:
				size = readSize(buffer);
				final List list = new ArrayList(size);

				for (i = 0; i < size; ++i) {
					list.add(readValue(buffer));
				}

				result = list;
				break;
			case 13:
				size = readSize(buffer);
				final Map map = new HashMap();

				for (i = 0; i < size; ++i) {
					map.put(readValue(buffer), readValue(buffer));
				}

				result = map;
				break;
			default:
				throw new IllegalArgumentException("Message corrupted");
			}

			return null;
		}
	}

	static {
		LITTLE_ENDIAN = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN;
		UTF8 = Charset.forName("UTF8");
	}

	static final class ExposedByteArrayOutputStream extends ByteArrayOutputStream {
		byte[] buffer () {
			return this.buf;
		}
	}
}
