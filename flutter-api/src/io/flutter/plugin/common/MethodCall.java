
package io.flutter.plugin.common;

import java.util.Map;

import org.json.JSONObject;

public final class MethodCall {
	public final String method;
	public final Object arguments;

	public MethodCall (final String method, final Object arguments) {
		assert method != null;

		this.method = method;
		this.arguments = arguments;
		throw new RuntimeException("Stub!");
	}

	public Object arguments () {
		return this.arguments;
	}

	public Object argument (final String key) {
		if (this.arguments == null) {
			return null;
		} else if (this.arguments instanceof Map) {
			return ((Map)this.arguments).get(key);
		} else if (this.arguments instanceof JSONObject) {
			return ((JSONObject)this.arguments).opt(key);
		} else {
			throw new ClassCastException();
		}
	}

	public boolean hasArgument (final String key) {
		if (this.arguments == null) {
			return false;
		} else if (this.arguments instanceof Map) {
			return ((Map)this.arguments).containsKey(key);
		} else if (this.arguments instanceof JSONObject) {
			return ((JSONObject)this.arguments).has(key);
		} else {
			throw new ClassCastException();
		}
	}
}
