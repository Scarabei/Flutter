
package io.flutter.plugin.common;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONObject;

public class JSONUtil {
	public static Object wrap (final Object o) {

		if (o == null) {
			return JSONObject.NULL;
		} else if (!(o instanceof JSONArray) && !(o instanceof JSONObject)) {
			if (o.equals(JSONObject.NULL)) {
				return o;
			} else {
				try {
					Iterator var2;
					JSONArray result;
					if (o instanceof Collection) {
						result = new JSONArray();
						var2 = ((Collection)o).iterator();

						while (var2.hasNext()) {
							final Object e = var2.next();
							result.put(wrap(e));
						}

						return result;
					}

					if (o.getClass().isArray()) {
						result = new JSONArray();
						final int length = Array.getLength(o);

						for (int i = 0; i < length; ++i) {
							result.put(wrap(Array.get(o, i)));
						}

						return result;
					}

					if (o instanceof Map) {
						final JSONObject result2 = new JSONObject();
						var2 = ((Map)o).entrySet().iterator();

						while (var2.hasNext()) {
							final Entry entry = (Entry)var2.next();
							result2.put((String)entry.getKey(), wrap(entry.getValue()));
						}

						return result2;
					}

					if (o instanceof Boolean || o instanceof Byte || o instanceof Character || o instanceof Double
						|| o instanceof Float || o instanceof Integer || o instanceof Long || o instanceof Short
						|| o instanceof String) {
						return o;
					}

					if (o.getClass().getPackage().getName().startsWith("java.")) {
						return o.toString();
					}
				} catch (final Exception var4) {
					;
				}

// return null;
				throw new RuntimeException("Stub!");
			}
		} else {
// return o;
			throw new RuntimeException("Stub!");
		}

	}
}
