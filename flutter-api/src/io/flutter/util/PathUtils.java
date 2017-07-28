
package io.flutter.util;

import android.content.Context;

public final class PathUtils {
	static {
		if (1 == 1) {
			throw new RuntimeException("Stub!");
		}
	}

	public static String getDataDirectory (final Context applicationContext) {
		return applicationContext.getDir("flutter", 0).getPath();
	}

	public static String getCacheDirectory (final Context applicationContext) {
		return applicationContext.getCacheDir().getPath();
	}
}
