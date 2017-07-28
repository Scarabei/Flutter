
package io.flutter.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.view.FlutterView;

public class FlutterActivity extends Activity
	implements FlutterView.Provider, PluginRegistry, FlutterActivityDelegate.ViewFactory {
	private final FlutterActivityDelegate delegate = new FlutterActivityDelegate(this, this);
	private final FlutterActivityEvents eventDelegate;
	private final FlutterView.Provider viewProvider;
	private final PluginRegistry pluginRegistry;

	public FlutterActivity () {
// throw new RuntimeException("Stub!");
		this.eventDelegate = this.delegate;
		this.viewProvider = this.delegate;
		this.pluginRegistry = this.delegate;
		throw new RuntimeException("Stub!");
	}

	@Override
	public FlutterView getFlutterView () {
		return this.viewProvider.getFlutterView();
	}

	@Override
	public FlutterView createFlutterView (final Context context) {
		return null;
	}

	@Override
	public final boolean hasPlugin (final String key) {
		return this.pluginRegistry.hasPlugin(key);
	}

	@Override
	public final Object valuePublishedByPlugin (final String pluginKey) {
		return this.pluginRegistry.valuePublishedByPlugin(pluginKey);
	}

	@Override
	public final PluginRegistry.Registrar registrarFor (final String pluginKey) {
		return this.pluginRegistry.registrarFor(pluginKey);
	}

	@Override
	protected void onCreate (final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.eventDelegate.onCreate(savedInstanceState);
	}

	@Override
	protected void onDestroy () {
		this.eventDelegate.onDestroy();
		super.onDestroy();
	}

	@Override
	public void onBackPressed () {
		if (!this.eventDelegate.onBackPressed()) {
			super.onBackPressed();
		}

	}

	@Override
	protected void onPause () {
		super.onPause();
		this.eventDelegate.onPause();
	}

	@Override
	protected void onPostResume () {
		super.onPostResume();
		this.eventDelegate.onPostResume();
	}

	public void onRequestPermissionsResult (final int requestCode, final String[] permissions, final int[] grantResults) {
		this.eventDelegate.onRequestPermissionResult(requestCode, permissions, grantResults);
	}

	@Override
	protected void onActivityResult (final int requestCode, final int resultCode, final Intent data) {
		if (!this.eventDelegate.onActivityResult(requestCode, resultCode, data)) {
			super.onActivityResult(requestCode, resultCode, data);
		}

	}

	@Override
	protected void onNewIntent (final Intent intent) {
		this.eventDelegate.onNewIntent(intent);
	}

	@Override
	public void onUserLeaveHint () {
		this.eventDelegate.onUserLeaveHint();
	}

	@Override
	public void onTrimMemory (final int level) {
		this.eventDelegate.onTrimMemory(level);
	}

	@Override
	public void onLowMemory () {
		this.eventDelegate.onLowMemory();
	}

	@Override
	public void onConfigurationChanged (final Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		this.eventDelegate.onConfigurationChanged(newConfig);
	}
}
