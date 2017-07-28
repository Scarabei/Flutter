
package io.flutter.app;

import android.app.Application;
import io.flutter.view.FlutterMain;

public class FlutterApplication extends Application {
	@Override
	public void onCreate () {
		super.onCreate();
		FlutterMain.startInitialization(this);
		throw new RuntimeException("Stub!");
	}
}
