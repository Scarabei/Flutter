
package io.flutter.view;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import io.flutter.util.PathUtils;

public class FlutterMain {
	private static final String TAG = "FlutterMain";
	private static final String AOT_SNAPSHOT_PATH_KEY = "aot-snapshot-path";
	private static final String AOT_VM_SNAPSHOT_DATA_KEY = "vm-snapshot-data";
	private static final String AOT_VM_SNAPSHOT_INSTR_KEY = "vm-snapshot-instr";
	private static final String AOT_ISOLATE_SNAPSHOT_DATA_KEY = "isolate-snapshot-data";
	private static final String AOT_ISOLATE_SNAPSHOT_INSTR_KEY = "isolate-snapshot-instr";
	private static final String FLX_KEY = "flx";
	public static final String PUBLIC_AOT_VM_SNAPSHOT_DATA_KEY = FlutterMain.class.getName() + '.' + "vm-snapshot-data";
	public static final String PUBLIC_AOT_VM_SNAPSHOT_INSTR_KEY = FlutterMain.class.getName() + '.' + "vm-snapshot-instr";
	public static final String PUBLIC_AOT_ISOLATE_SNAPSHOT_DATA_KEY = FlutterMain.class.getName() + '.' + "isolate-snapshot-data";
	public static final String PUBLIC_AOT_ISOLATE_SNAPSHOT_INSTR_KEY = FlutterMain.class.getName() + '.'
		+ "isolate-snapshot-instr";
	public static final String PUBLIC_FLX_KEY = FlutterMain.class.getName() + '.' + "flx";
	private static final String DEFAULT_AOT_VM_SNAPSHOT_DATA = "vm_snapshot_data";
	private static final String DEFAULT_AOT_VM_SNAPSHOT_INSTR = "vm_snapshot_instr";
	private static final String DEFAULT_AOT_ISOLATE_SNAPSHOT_DATA = "isolate_snapshot_data";
	private static final String DEFAULT_AOT_ISOLATE_SNAPSHOT_INSTR = "isolate_snapshot_instr";
	private static final String DEFAULT_FLX = "app.flx";
	private static final String MANIFEST = "flutter.yaml";
	private static final Set SKY_RESOURCES = FlutterMain.ImmutableSetBuilder.newInstance().add("icudtl.dat").add("flutter.yaml")
		.build();
	private static String sAotVmSnapshotData = "vm_snapshot_data";
	private static String sAotVmSnapshotInstr = "vm_snapshot_instr";
	private static String sAotIsolateSnapshotData = "isolate_snapshot_data";
	private static String sAotIsolateSnapshotInstr = "isolate_snapshot_instr";
	private static String sFlx = "app.flx";
	private static boolean sInitialized = false;
	private static ResourceExtractor sResourceExtractor;
	private static boolean sIsPrecompiled;
	private static FlutterMain.Settings sSettings;

	public static void startInitialization (final Context applicationContext) {
		startInitialization(applicationContext, new FlutterMain.Settings());
	}

	public static void startInitialization (final Context applicationContext, final FlutterMain.Settings settings) {
		sSettings = settings;
		final long initStartTimestampMillis = SystemClock.uptimeMillis();
		initConfig(applicationContext);
		initResources(applicationContext);
		System.loadLibrary("flutter");
		initAot(applicationContext);
		final long initTimeMillis = SystemClock.uptimeMillis() - initStartTimestampMillis;
		nativeRecordStartTimestamp(initTimeMillis);
	}

	public static void ensureInitializationComplete (final Context applicationContext, final String[] args) {
		if (!sInitialized) {
			try {
				sResourceExtractor.waitForCompletion();
				final List shellArgs = new ArrayList();
				shellArgs.add("--icu-data-file-path=" + new File(PathUtils.getDataDirectory(applicationContext), "icudtl.dat"));
				if (args != null) {
					Collections.addAll(shellArgs, args);
				}

				if (sIsPrecompiled) {
					shellArgs.add("--aot-snapshot-path=" + PathUtils.getDataDirectory(applicationContext));
					shellArgs.add("--vm-snapshot-data=" + sAotVmSnapshotData);
					shellArgs.add("--vm-snapshot-instr=" + sAotVmSnapshotInstr);
					shellArgs.add("--isolate-snapshot-data=" + sAotIsolateSnapshotData);
					shellArgs.add("--isolate-snapshot-instr=" + sAotIsolateSnapshotInstr);
				} else {
					shellArgs.add("--cache-dir-path=" + PathUtils.getCacheDirectory(applicationContext));
				}

				if (sSettings.getLogTag() != null) {
					shellArgs.add("--log-tag=" + sSettings.getLogTag());
				}

				nativeInit(applicationContext, (String[])shellArgs.toArray(new String[0]));
				sInitialized = true;
			} catch (final Exception var3) {
				Log.e("FlutterMain", "Flutter initialization failed.", var3);
				throw new RuntimeException(var3);
			}
		}
	}

	private static native void nativeInit (Context var0, String[] var1);

	private static native void nativeRecordStartTimestamp (long var0);

	private static void initConfig (final Context applicationContext) {
		try {
			final Bundle metadata = applicationContext.getPackageManager().getApplicationInfo(applicationContext.getPackageName(),
				128).metaData;
			if (metadata != null) {
				sAotVmSnapshotData = metadata.getString(PUBLIC_AOT_VM_SNAPSHOT_DATA_KEY, "vm_snapshot_data");
				sAotVmSnapshotInstr = metadata.getString(PUBLIC_AOT_VM_SNAPSHOT_INSTR_KEY, "vm_snapshot_instr");
				sAotIsolateSnapshotData = metadata.getString(PUBLIC_AOT_ISOLATE_SNAPSHOT_DATA_KEY, "isolate_snapshot_data");
				sAotIsolateSnapshotInstr = metadata.getString(PUBLIC_AOT_ISOLATE_SNAPSHOT_INSTR_KEY, "isolate_snapshot_instr");
				sFlx = metadata.getString(PUBLIC_FLX_KEY, "app.flx");
			}

		} catch (final NameNotFoundException var2) {
			throw new RuntimeException(var2);
		}
	}

	private static void initResources (final Context applicationContext) {
		(new ResourceCleaner(applicationContext)).start();
		sResourceExtractor = (new ResourceExtractor(applicationContext)).addResources(SKY_RESOURCES).addResource(sAotVmSnapshotData)
			.addResource(sAotVmSnapshotInstr).addResource(sAotIsolateSnapshotData).addResource(sAotIsolateSnapshotInstr)
			.addResource(sFlx).start();
	}

	private static Set listRootAssets (final Context applicationContext) {
		final AssetManager manager = applicationContext.getResources().getAssets();

		try {
			return FlutterMain.ImmutableSetBuilder.newInstance().add((Object[])manager.list("")).build();
		} catch (final IOException var3) {
			Log.e("FlutterMain", "Unable to list assets", var3);
			throw new RuntimeException(var3);
		}
	}

	private static void initAot (final Context applicationContext) {
		final Set assets = listRootAssets(applicationContext);
		sIsPrecompiled = assets.containsAll(Arrays
			.asList(new String[] {sAotVmSnapshotData, sAotVmSnapshotInstr, sAotIsolateSnapshotData, sAotIsolateSnapshotInstr}));
	}

	public static boolean isRunningPrecompiledCode () {
		return sIsPrecompiled;
	}

	public static String findAppBundlePath (final Context applicationContext) {
		final String dataDirectory = PathUtils.getDataDirectory(applicationContext);
		final File appBundle = new File(dataDirectory, sFlx);
		return appBundle.exists() ? appBundle.getPath() : null;
	}

	public static class Settings {
		private String logTag;

		public String getLogTag () {
			return this.logTag;
		}

		public void setLogTag (final String tag) {
			this.logTag = tag;
		}
	}

	{
		if (1 == 1) {
			throw new RuntimeException("Stub!");
		}
	}

	private static final class ImmutableSetBuilder {
		HashSet set = new HashSet();

		static FlutterMain.ImmutableSetBuilder newInstance () {
			return new FlutterMain.ImmutableSetBuilder();
		}

		FlutterMain.ImmutableSetBuilder add (final Object element) {
			this.set.add(element);
			return this;
		}

		@SafeVarargs
		final FlutterMain.ImmutableSetBuilder add (final Object... elements) {
			final Object[] var2 = elements;
			final int var3 = elements.length;

			for (int var4 = 0; var4 < var3; ++var4) {
				final Object element = var2[var4];
				this.set.add(element);
			}

			return this;
		}

		Set build () {
			return Collections.unmodifiableSet(this.set);
		}
	}
}
