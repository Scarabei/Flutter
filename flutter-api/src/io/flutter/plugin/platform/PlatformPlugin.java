
package io.flutter.plugin.platform;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Build.VERSION;
import android.view.View;
import io.flutter.plugin.common.ActivityLifecycleListener;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

public class PlatformPlugin implements MethodChannel.MethodCallHandler, ActivityLifecycleListener {
	private final Activity mActivity;
	public static final int DEFAULT_SYSTEM_UI = 1280;
	private static final String kTextPlainFormat = "text/plain";
	private int mEnabledOverlays;

	public PlatformPlugin (final Activity activity) {
		this.mActivity = activity;
		this.mEnabledOverlays = 1280;
	}

	@Override
	public void onMethodCall (final MethodCall call, final MethodChannel.Result result) {
		final String method = call.method;
		final Object arguments = call.arguments;

		try {
			if (method.equals("SystemSound.play")) {
				this.playSystemSound((String)arguments);
				result.success((Object)null);
			} else if (method.equals("HapticFeedback.vibrate")) {
				this.vibrateHapticFeedback();
				result.success((Object)null);
			} else if (method.equals("SystemChrome.setPreferredOrientations")) {
				this.setSystemChromePreferredOrientations((JSONArray)arguments);
				result.success((Object)null);
			} else if (method.equals("SystemChrome.setApplicationSwitcherDescription")) {
				this.setSystemChromeApplicationSwitcherDescription((JSONObject)arguments);
				result.success((Object)null);
			} else if (method.equals("SystemChrome.setEnabledSystemUIOverlays")) {
				this.setSystemChromeEnabledSystemUIOverlays((JSONArray)arguments);
				result.success((Object)null);
			} else if (method.equals("SystemChrome.setSystemUIOverlayStyle")) {
				this.setSystemChromeSystemUIOverlayStyle((String)arguments);
				result.success((Object)null);
			} else if (method.equals("SystemNavigator.pop")) {
				this.popSystemNavigator();
				result.success((Object)null);
			} else if (method.equals("Clipboard.getData")) {
				result.success(this.getClipboardData((String)arguments));
			} else if (method.equals("Clipboard.setData")) {
				this.setClipboardData((JSONObject)arguments);
				result.success((Object)null);
			} else {
				result.notImplemented();
			}
		} catch (final JSONException var6) {
			result.error("error", "JSON error: " + var6.getMessage(), (Object)null);
		}

	}

	private void playSystemSound (final String soundType) {
		if (soundType.equals("SystemSoundType.click")) {
			final View view = this.mActivity.getWindow().getDecorView();
			view.playSoundEffect(0);
		}

	}

	private void vibrateHapticFeedback () {
		final View view = this.mActivity.getWindow().getDecorView();
		view.performHapticFeedback(0);
	}

	private void setSystemChromePreferredOrientations (final JSONArray orientations) throws JSONException {
		byte androidOrientation;
		if (orientations.length() == 0) {
			androidOrientation = -1;
		} else if (orientations.getString(0).equals("DeviceOrientation.portraitUp")) {
			androidOrientation = 1;
		} else if (orientations.getString(0).equals("DeviceOrientation.landscapeLeft")) {
			androidOrientation = 0;
		} else if (orientations.getString(0).equals("DeviceOrientation.portraitDown")) {
			androidOrientation = 9;
		} else {
			if (!orientations.getString(0).equals("DeviceOrientation.landscapeRight")) {
				return;
			}

			androidOrientation = 8;
		}

		this.mActivity.setRequestedOrientation(androidOrientation);
	}

	static {
		if (1 == 1) {
			throw new RuntimeException("Stub!");
		}
	}

	private void setSystemChromeApplicationSwitcherDescription (final JSONObject description) throws JSONException {
		if (VERSION.SDK_INT >= 21) {
			int color = description.getInt("primaryColor");
			if (color != 0) {
				color |= -16777216;
			}

// this.mActivity.setTaskDescription(new TaskDescription(description.getString("label"), (Bitmap)null, color));
		}
	}

	private void setSystemChromeEnabledSystemUIOverlays (final JSONArray overlays) throws JSONException {
		int enabledOverlays = 1798;
		if (overlays.length() == 0) {
			enabledOverlays |= 4096;
		}

		for (int i = 0; i < overlays.length(); ++i) {
			final String overlay = overlays.getString(i);
			if (overlay.equals("SystemUiOverlay.top")) {
				enabledOverlays &= -5;
			} else if (overlay.equals("SystemUiOverlay.bottom")) {
				enabledOverlays &= -513;
				enabledOverlays &= -3;
			}
		}

		this.mEnabledOverlays = enabledOverlays;
		this.updateSystemUiOverlays();
	}

	private void updateSystemUiOverlays () {
		this.mActivity.getWindow().getDecorView().setSystemUiVisibility(this.mEnabledOverlays);
	}

	private void setSystemChromeSystemUIOverlayStyle (final String style) {
	}

	private void popSystemNavigator () {
		this.mActivity.finish();
	}

	private JSONObject getClipboardData (final String format) throws JSONException {
		final ClipboardManager clipboard = (ClipboardManager)this.mActivity.getSystemService("clipboard");
		final ClipData clip = clipboard.getPrimaryClip();
		if (clip == null) {
			return null;
		} else if ((format == null || format.equals("text/plain")) && clip.getDescription().hasMimeType("text/plain")) {
			final JSONObject result = new JSONObject();
			result.put("text", clip.getItemAt(0).getText().toString());
			return result;
		} else {
			return null;
		}
	}

	private void setClipboardData (final JSONObject data) throws JSONException {
		final ClipboardManager clipboard = (ClipboardManager)this.mActivity.getSystemService("clipboard");
		final ClipData clip = ClipData.newPlainText("text label?", data.getString("text"));
		clipboard.setPrimaryClip(clip);
	}

	@Override
	public void onPostResume () {
		this.updateSystemUiOverlays();
	}
}
