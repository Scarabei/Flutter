
package io.flutter.app;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.util.Preconditions;
import io.flutter.view.FlutterMain;
import io.flutter.view.FlutterView;

public final class FlutterActivityDelegate implements FlutterActivityEvents, FlutterView.Provider, PluginRegistry {
	private final Activity activity;
	private final FlutterActivityDelegate.ViewFactory viewFactory;
	private final Map pluginMap = new LinkedHashMap(0);
	private final List requestPermissionResultListeners = new ArrayList(0);
	private final List activityResultListeners = new ArrayList(0);
	private final List newIntentListeners = new ArrayList(0);
	private final List userLeaveHintListeners = new ArrayList(0);
	private FlutterView flutterView;

	public FlutterActivityDelegate (final Activity activity, final FlutterActivityDelegate.ViewFactory viewFactory) {
		this.activity = (Activity)Preconditions.checkNotNull(activity);
		this.viewFactory = (FlutterActivityDelegate.ViewFactory)Preconditions.checkNotNull(viewFactory);
	}

	@Override
	public FlutterView getFlutterView () {
		return this.flutterView;
	}

	@Override
	public boolean hasPlugin (final String key) {
		return this.pluginMap.containsKey(key);
	}

	@Override
	public Object valuePublishedByPlugin (final String pluginKey) {
		return this.pluginMap.get(pluginKey);
	}

	@Override
	public PluginRegistry.Registrar registrarFor (final String pluginKey) {
		if (this.pluginMap.containsKey(pluginKey)) {
			throw new IllegalStateException("Plugin key " + pluginKey + " is already in use");
		} else {
			this.pluginMap.put(pluginKey, (Object)null);
			return new FlutterActivityDelegate.FlutterRegistrar(pluginKey);
		}
	}

	@Override
	public boolean onRequestPermissionResult (final int requestCode, final String[] permissions, final int[] grantResults) {
		final Iterator var4 = this.requestPermissionResultListeners.iterator();

		PluginRegistry.RequestPermissionResultListener listener;
		do {
			if (!var4.hasNext()) {
				return false;
			}

			listener = (PluginRegistry.RequestPermissionResultListener)var4.next();
		} while (!listener.onRequestPermissionResult(requestCode, permissions, grantResults));

		return true;
	}

	@Override
	public boolean onActivityResult (final int requestCode, final int resultCode, final Intent data) {
		final Iterator var4 = this.activityResultListeners.iterator();

		PluginRegistry.ActivityResultListener listener;
		do {
			if (!var4.hasNext()) {
				return false;
			}

			listener = (PluginRegistry.ActivityResultListener)var4.next();
		} while (!listener.onActivityResult(requestCode, resultCode, data));

		return true;
	}

	@Override
	public void onCreate (final Bundle savedInstanceState) {
		if (VERSION.SDK_INT >= 21) {
			final Window window = this.activity.getWindow();
			window.addFlags(Integer.MIN_VALUE);
// window.setStatusBarColor(1073741824);
			window.getDecorView().setSystemUiVisibility(1280);
		}

		final String[] args = getArgsFromIntent(this.activity.getIntent());
		FlutterMain.ensureInitializationComplete(this.activity.getApplicationContext(), args);
		this.flutterView = this.viewFactory.createFlutterView(this.activity);
		if (this.flutterView == null) {
			this.flutterView = new FlutterView(this.activity);
			this.flutterView.setLayoutParams(new LayoutParams(-1, -1));
			this.activity.setContentView(this.flutterView);
		}

		if (!this.loadIntent(this.activity.getIntent())) {
			final String appBundlePath = FlutterMain.findAppBundlePath(this.activity.getApplicationContext());
			if (appBundlePath != null) {
				this.flutterView.runFromBundle(appBundlePath, (String)null);
			}

		}
	}

	@Override
	public void onNewIntent (final Intent intent) {
		if (!this.isDebuggable() || !this.loadIntent(intent)) {
			final Iterator var2 = this.newIntentListeners.iterator();

			while (var2.hasNext()) {
				final PluginRegistry.NewIntentListener listener = (PluginRegistry.NewIntentListener)var2.next();
				if (listener.onNewIntent(intent)) {
					return;
				}
			}
		}

	}

	private boolean isDebuggable () {
		return (this.activity.getApplicationInfo().flags & 2) != 0;
	}

	@Override
	public void onPause () {
		if (this.flutterView != null) {
			this.flutterView.onPause();
		}

	}

	@Override
	public void onResume () {
	}

	@Override
	public void onPostResume () {
		if (this.flutterView != null) {
			this.flutterView.onPostResume();
		}

	}

	@Override
	public void onDestroy () {
		if (this.flutterView != null) {
			this.flutterView.destroy();
		}

	}

	@Override
	public boolean onBackPressed () {
		if (this.flutterView != null) {
			this.flutterView.popRoute();
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void onUserLeaveHint () {
	}

	@Override
	public void onTrimMemory (final int level) {
		if (level == 10) {
			this.flutterView.onMemoryPressure();
		}

	}

	static {
		if (1 == 1) {
			throw new RuntimeException("Stub!");
		}
	}

	@Override
	public void onLowMemory () {
		this.flutterView.onMemoryPressure();
	}

	@Override
	public void onConfigurationChanged (final Configuration newConfig) {
	}

	private static String[] getArgsFromIntent (final Intent intent) {
		final ArrayList args = new ArrayList();
		if (intent.getBooleanExtra("trace-startup", false)) {
			args.add("--trace-startup");
		}

		if (intent.getBooleanExtra("start-paused", false)) {
			args.add("--start-paused");
		}

		if (intent.getBooleanExtra("use-test-fonts", false)) {
			args.add("--use-test-fonts");
		}

		if (intent.getBooleanExtra("enable-dart-profiling", false)) {
			args.add("--enable-dart-profiling");
		}

		if (intent.getBooleanExtra("enable-software-rendering", false)) {
			args.add("--enable-software-rendering");
		}

		if (!args.isEmpty()) {
			final String[] argsArray = new String[args.size()];
			return (String[])args.toArray(argsArray);
		} else {
			return null;
		}
	}

	private boolean loadIntent (final Intent intent) {
		final String action = intent.getAction();
		if ("android.intent.action.RUN".equals(action)) {
			final String route = intent.getStringExtra("route");
			String appBundlePath = intent.getDataString();
			if (appBundlePath == null) {
				appBundlePath = FlutterMain.findAppBundlePath(this.activity.getApplicationContext());
			}

			if (route != null) {
				this.flutterView.setInitialRoute(route);
			}

			this.flutterView.runFromBundle(appBundlePath, intent.getStringExtra("snapshot"));
			return true;
		} else {
			return false;
		}
	}

	private class FlutterRegistrar implements PluginRegistry.Registrar {
		private final String pluginKey;

		FlutterRegistrar (final String pluginKey) {
			this.pluginKey = pluginKey;
		}

		@Override
		public Activity activity () {
			return FlutterActivityDelegate.this.activity;
		}

		@Override
		public BinaryMessenger messenger () {
			return FlutterActivityDelegate.this.flutterView;
		}

		@Override
		public FlutterView view () {
			return FlutterActivityDelegate.this.flutterView;
		}

		@Override
		public PluginRegistry.Registrar publish (final Object value) {
			FlutterActivityDelegate.this.pluginMap.put(this.pluginKey, value);
			return this;
		}

		@Override
		public PluginRegistry.Registrar addRequestPermissionResultListener (
			final PluginRegistry.RequestPermissionResultListener listener) {
			FlutterActivityDelegate.this.requestPermissionResultListeners.add(listener);
			return this;
		}

		@Override
		public PluginRegistry.Registrar addActivityResultListener (final PluginRegistry.ActivityResultListener listener) {
			FlutterActivityDelegate.this.activityResultListeners.add(listener);
			return this;
		}

		@Override
		public PluginRegistry.Registrar addNewIntentListener (final PluginRegistry.NewIntentListener listener) {
			FlutterActivityDelegate.this.newIntentListeners.add(listener);
			return this;
		}

		@Override
		public PluginRegistry.Registrar addUserLeaveHintListener (final PluginRegistry.UserLeaveHintListener listener) {
			FlutterActivityDelegate.this.userLeaveHintListeners.add(listener);
			return this;
		}
	}

	public interface ViewFactory {
		FlutterView createFlutterView (Context var1);
	}
}
