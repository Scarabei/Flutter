
package io.flutter.view;

import android.content.Context;

class ResourceCleaner {
	private static final String TAG = "ResourceCleaner";
	private static final long DELAY_MS = 5000L;
	private final Context mContext;

	ResourceCleaner (final Context context) {
		this.mContext = context;
	}

	void start () {
// final File cacheDir = this.mContext.getCacheDir();
// if (cacheDir != null) {
// final ResourceCleaner.CleanTask task = new ResourceCleaner.CleanTask(cacheDir.listFiles(new FilenameFilter() {
// @Override
// public boolean accept (final File dir, final String name) {
// final boolean result = name.startsWith(".org.chromium.Chromium.");
// return result;
// }
// }));
// if (task.hasFilesToDelete()) {
// (new Handler()).postDelayed(new Runnable() {
// @Override
// public void run () {
// task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
// }
// }, 5000L);
// }
// }
	}

// private class CleanTask extends AsyncTask {
// private final File[] mFilesToDelete;
//
// CleanTask(File[] filesToDelete) {
// this.mFilesToDelete = filesToDelete;
// }
//
// boolean hasFilesToDelete() {
// return this.mFilesToDelete.length > 0;
// }
//
// protected Void doInBackground(Void... unused) {
// Log.i("ResourceCleaner", "Cleaning " + this.mFilesToDelete.length + " resources.");
// File[] var2 = this.mFilesToDelete;
// int var3 = var2.length;
//
// for(int var4 = 0; var4 < var3; ++var4) {
// File file = var2[var4];
// if(file.exists()) {
// this.deleteRecursively(file);
// }
// }
//
// return null;
// }
//
// private void deleteRecursively(File parent) {
// if(parent.isDirectory()) {
// File[] var2 = parent.listFiles();
// int var3 = var2.length;
//
// for(int var4 = 0; var4 < var3; ++var4) {
// File child = var2[var4];
// this.deleteRecursively(child);
// }
// }
//
// parent.delete();
// }
// }
}
