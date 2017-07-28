
package io.flutter.view;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Build.VERSION;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityManager.AccessibilityStateChangeListener;
import android.view.accessibility.AccessibilityNodeProvider;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import io.flutter.plugin.common.ActivityLifecycleListener;
import io.flutter.plugin.common.BasicMessageChannel;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.JSONMessageCodec;
import io.flutter.plugin.common.JSONMethodCodec;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.StringCodec;
import io.flutter.plugin.editing.TextInputPlugin;
import io.flutter.plugin.platform.PlatformPlugin;

public class FlutterView extends SurfaceView implements BinaryMessenger, AccessibilityStateChangeListener {
	private static final String TAG = "FlutterView";
	private static final String ACTION_DISCOVER = "io.flutter.view.DISCOVER";
	private final TextInputPlugin mTextInputPlugin;
	private final Map mMessageHandlers;
	private final Callback mSurfaceCallback;
	private final FlutterView.ViewportMetrics mMetrics;
	private final AccessibilityManager mAccessibilityManager;
	private final MethodChannel mFlutterLocalizationChannel;
	private final MethodChannel mFlutterNavigationChannel;
	private final BasicMessageChannel mFlutterKeyEventChannel;
	private final BasicMessageChannel mFlutterLifecycleChannel;
	private final BasicMessageChannel mFlutterSystemChannel;
	private final BroadcastReceiver mDiscoveryReceiver;
	private final List mActivityLifecycleListeners;
	private long mNativePlatformView;
	private boolean mIsSoftwareRenderingEnabled;
	private static final int kPointerChangeCancel = 0;
	private static final int kPointerChangeAdd = 1;
	private static final int kPointerChangeRemove = 2;
	private static final int kPointerChangeHover = 3;
	private static final int kPointerChangeDown = 4;
	private static final int kPointerChangeMove = 5;
	private static final int kPointerChangeUp = 6;
	private static final int kPointerDeviceKindTouch = 0;
	private static final int kPointerDeviceKindMouse = 1;
	private static final int kPointerDeviceKindStylus = 2;
	private static final int kPointerDeviceKindInvertedStylus = 3;
	private int mNextReplyId;
	private final Map mPendingReplies;
	private boolean mAccessibilityEnabled;
	private boolean mTouchExplorationEnabled;
// private FlutterView.TouchExplorationListener mTouchExplorationListener;
	private AccessibilityBridge mAccessibilityNodeProvider;

	public FlutterView (final Context context) {
		this(context, (AttributeSet)null);
		throw new RuntimeException("Stub!");
	}

	{
		if (1 == 1) {
			throw new RuntimeException("Stub!");
		}
	}

	public FlutterView (final Context context, final AttributeSet attrs) {
		super(context, attrs);
		this.mIsSoftwareRenderingEnabled = false;
		this.mNextReplyId = 1;
		this.mPendingReplies = new HashMap();
		this.mAccessibilityEnabled = false;
		this.mTouchExplorationEnabled = false;
		this.mIsSoftwareRenderingEnabled = nativeGetIsSoftwareRenderingEnabled();
		this.mMetrics = new FlutterView.ViewportMetrics();
		this.mMetrics.devicePixelRatio = context.getResources().getDisplayMetrics().density;
		this.setFocusable(true);
		this.setFocusableInTouchMode(true);
		this.attach();

		assert this.mNativePlatformView != 0L;

		final int color = -16777216;
		final TypedValue typedValue = new TypedValue();
		context.getTheme().resolveAttribute(16842801, typedValue, true);
		if (typedValue.type >= 28 && typedValue.type <= 31) {
// color = typedValue.data;
		}

		this.mSurfaceCallback = new Callback() {
			@Override
			public void surfaceCreated (final SurfaceHolder holder) {
				assert FlutterView.this.mNativePlatformView != 0L;

				FlutterView.nativeSurfaceCreated(FlutterView.this.mNativePlatformView, holder.getSurface(), color);
			}

			@Override
			public void surfaceChanged (final SurfaceHolder holder, final int format, final int width, final int height) {
				assert FlutterView.this.mNativePlatformView != 0L;

				FlutterView.nativeSurfaceChanged(FlutterView.this.mNativePlatformView, width, height);
			}

			@Override
			public void surfaceDestroyed (final SurfaceHolder holder) {
				assert FlutterView.this.mNativePlatformView != 0L;

				FlutterView.nativeSurfaceDestroyed(FlutterView.this.mNativePlatformView);
			}
		};
		this.getHolder().addCallback(this.mSurfaceCallback);
		this.mAccessibilityManager = (AccessibilityManager)this.getContext().getSystemService("accessibility");
		this.mMessageHandlers = new HashMap();
		this.mActivityLifecycleListeners = new ArrayList();
		this.mFlutterLocalizationChannel = new MethodChannel(this, "flutter/localization", JSONMethodCodec.INSTANCE);
		this.mFlutterNavigationChannel = new MethodChannel(this, "flutter/navigation", JSONMethodCodec.INSTANCE);
		this.mFlutterKeyEventChannel = new BasicMessageChannel(this, "flutter/keyevent", JSONMessageCodec.INSTANCE);
		this.mFlutterLifecycleChannel = new BasicMessageChannel(this, "flutter/lifecycle", StringCodec.INSTANCE);
		this.mFlutterSystemChannel = new BasicMessageChannel(this, "flutter/system", JSONMessageCodec.INSTANCE);
		final PlatformPlugin platformPlugin = new PlatformPlugin((Activity)this.getContext());
		final MethodChannel flutterPlatformChannel = new MethodChannel(this, "flutter/platform", JSONMethodCodec.INSTANCE);
		flutterPlatformChannel.setMethodCallHandler(platformPlugin);
		this.addActivityLifecycleListener(platformPlugin);
		this.mTextInputPlugin = new TextInputPlugin(this);
		this.setLocale(this.getResources().getConfiguration().locale);
		if ((context.getApplicationInfo().flags & 2) != 0) {
			this.mDiscoveryReceiver = new FlutterView.DiscoveryReceiver(null);
			context.registerReceiver(this.mDiscoveryReceiver, new IntentFilter("io.flutter.view.DISCOVER"));
		} else {
			this.mDiscoveryReceiver = null;
		}

	}

	private void encodeKeyEvent (final KeyEvent event, final Map message) {
		message.put("flags", Integer.valueOf(event.getFlags()));
		message.put("codePoint", Integer.valueOf(event.getUnicodeChar()));
		message.put("keyCode", Integer.valueOf(event.getKeyCode()));
		message.put("scanCode", Integer.valueOf(event.getScanCode()));
		message.put("metaState", Integer.valueOf(event.getMetaState()));
	}

	@Override
	public boolean onKeyUp (final int keyCode, final KeyEvent event) {
		if (!this.isAttached()) {
			return super.onKeyUp(keyCode, event);
		} else {
			final Map message = new HashMap();
			message.put("type", "keyup");
			message.put("keymap", "android");
			this.encodeKeyEvent(event, message);
			this.mFlutterKeyEventChannel.send(message);
			return super.onKeyUp(keyCode, event);
		}
	}

	@Override
	public boolean onKeyDown (final int keyCode, final KeyEvent event) {
		if (!this.isAttached()) {
			return super.onKeyDown(keyCode, event);
		} else {
			final Map message = new HashMap();
			message.put("type", "keydown");
			message.put("keymap", "android");
			this.encodeKeyEvent(event, message);
			this.mFlutterKeyEventChannel.send(message);
			return super.onKeyDown(keyCode, event);
		}
	}

	public void addActivityLifecycleListener (final ActivityLifecycleListener listener) {
		this.mActivityLifecycleListeners.add(listener);
	}

	public void onPause () {
		this.mFlutterLifecycleChannel.send("AppLifecycleState.paused");
	}

	public void onPostResume () {
		final Iterator var1 = this.mActivityLifecycleListeners.iterator();

		while (var1.hasNext()) {
			final ActivityLifecycleListener listener = (ActivityLifecycleListener)var1.next();
			listener.onPostResume();
		}

		this.mFlutterLifecycleChannel.send("AppLifecycleState.resumed");
	}

	public void onStop () {
		this.mFlutterLifecycleChannel.send("AppLifecycleState.suspending");
	}

	public void onMemoryPressure () {
		final Map message = new HashMap(1);
		message.put("type", "memoryPressure");
		this.mFlutterSystemChannel.send(message);
	}

	public void setInitialRoute (final String route) {
		this.mFlutterNavigationChannel.invokeMethod("setInitialRoute", route);
	}

	public void pushRoute (final String route) {
		this.mFlutterNavigationChannel.invokeMethod("pushRoute", route);
	}

	public void popRoute () {
		this.mFlutterNavigationChannel.invokeMethod("popRoute", (Object)null);
	}

	private void setLocale (final Locale locale) {
		this.mFlutterLocalizationChannel.invokeMethod("setLocale",
			Arrays.asList(new String[] {locale.getLanguage(), locale.getCountry()}));
	}

	@Override
	protected void onConfigurationChanged (final Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		this.setLocale(newConfig.locale);
	}

	float getDevicePixelRatio () {
		return this.mMetrics.devicePixelRatio;
	}

	public void destroy () {
		if (this.mDiscoveryReceiver != null) {
			this.getContext().unregisterReceiver(this.mDiscoveryReceiver);
		}

		this.getHolder().removeCallback(this.mSurfaceCallback);
		nativeDetach(this.mNativePlatformView);
		this.mNativePlatformView = 0L;
	}

	@Override
	public InputConnection onCreateInputConnection (final EditorInfo outAttrs) {
		try {
			return this.mTextInputPlugin.createInputConnection(this, outAttrs);
		} catch (final JSONException var3) {
			Log.e("FlutterView", "Failed to create input connection", var3);
			return null;
		}
	}

	private int getPointerChangeForAction (final int maskedAction) {
		return maskedAction == 0 ? 4
			: (maskedAction == 1 ? 6
				: (maskedAction == 5 ? 4 : (maskedAction == 6 ? 6 : (maskedAction == 2 ? 5 : (maskedAction == 3 ? 0 : -1)))));
	}

	private int getPointerDeviceTypeForToolType (final int toolType) {
		switch (toolType) {
		case 1:
			return 0;
		case 2:
			return 2;
		case 3:
			return 1;
		default:
			return -1;
		}
	}

	private void addPointerForIndex (final MotionEvent event, final int pointerIndex, final ByteBuffer packet) {
		final int pointerChange = this.getPointerChangeForAction(event.getActionMasked());
		if (pointerChange != -1) {
			final int pointerKind = event.getToolType(pointerIndex);
			if (pointerKind != -1) {
				final long timeStamp = event.getEventTime() * 1000L;
				packet.putLong(timeStamp);
				packet.putLong(pointerChange);
				packet.putLong(pointerKind);
				packet.putLong(event.getPointerId(pointerIndex));
				packet.putDouble(event.getX(pointerIndex));
				packet.putDouble(event.getY(pointerIndex));
				if (pointerKind == 1) {
					packet.putLong(event.getButtonState() & 31);
				} else if (pointerKind == 2) {
					packet.putLong(event.getButtonState() >> 4 & 15);
				} else {
					packet.putLong(0L);
				}

				packet.putLong(0L);
				packet.putDouble(event.getPressure(pointerIndex));
				packet.putDouble(0.0D);
				packet.putDouble(1.0D);
				if (pointerKind == 2) {
					packet.putDouble(event.getAxisValue(24, pointerIndex));
					packet.putDouble(0.0D);
				} else {
					packet.putDouble(0.0D);
					packet.putDouble(0.0D);
				}

				packet.putDouble(event.getToolMajor(pointerIndex));
				packet.putDouble(event.getToolMinor(pointerIndex));
				packet.putDouble(0.0D);
				packet.putDouble(0.0D);
				packet.putDouble(event.getAxisValue(8, pointerIndex));
				if (pointerKind == 2) {
					packet.putDouble(event.getAxisValue(25, pointerIndex));
				} else {
					packet.putDouble(0.0D);
				}

			}
		}
	}

	@Override
	public boolean onTouchEvent (final MotionEvent event) {
		if (!this.isAttached()) {
			return false;
		} else {
			if (VERSION.SDK_INT >= 21) {
// this.requestUnbufferedDispatch(event);
			}

// final int kPointerDataFieldCount = true;
// final int kBytePerField = true;
			final int pointerCount = event.getPointerCount();
			final ByteBuffer packet = ByteBuffer.allocateDirect(pointerCount * 19 * 8);
			packet.order(ByteOrder.LITTLE_ENDIAN);
			final int maskedAction = event.getActionMasked();
			if (maskedAction != 1 && maskedAction != 6 && maskedAction != 0 && maskedAction != 5) {
				for (int p = 0; p < pointerCount; ++p) {
					this.addPointerForIndex(event, p, packet);
				}
			} else {
				this.addPointerForIndex(event, event.getActionIndex(), packet);
			}

			assert packet.position() % 152 == 0;

			nativeDispatchPointerDataPacket(this.mNativePlatformView, packet, packet.position());
			return true;
		}
	}

	@Override
	public boolean onHoverEvent (final MotionEvent event) {
		if (!this.isAttached()) {
			return false;
		} else {
			final boolean handled = this.handleAccessibilityHoverEvent(event);
			if (!handled) {
				;
			}

			return handled;
		}
	}

	@Override
	protected void onSizeChanged (final int width, final int height, final int oldWidth, final int oldHeight) {
		this.mMetrics.physicalWidth = width;
		this.mMetrics.physicalHeight = height;
		this.updateViewportMetrics();
		super.onSizeChanged(width, height, oldWidth, oldHeight);
	}

// public final WindowInsets onApplyWindowInsets (final WindowInsets insets) {
// this.mMetrics.physicalPaddingTop = insets.getSystemWindowInsetTop();
// this.mMetrics.physicalPaddingRight = insets.getSystemWindowInsetRight();
// this.mMetrics.physicalPaddingBottom = insets.getSystemWindowInsetBottom();
// this.mMetrics.physicalPaddingLeft = insets.getSystemWindowInsetLeft();
// this.updateViewportMetrics();
// return super.onApplyWindowInsets(insets);
// }

	@Override
	protected boolean fitSystemWindows (final Rect insets) {
		if (VERSION.SDK_INT <= 19) {
			this.mMetrics.physicalPaddingTop = insets.top;
			this.mMetrics.physicalPaddingRight = insets.right;
			this.mMetrics.physicalPaddingBottom = insets.bottom;
			this.mMetrics.physicalPaddingLeft = insets.left;
			this.updateViewportMetrics();
			return true;
		} else {
			return super.fitSystemWindows(insets);
		}
	}

	private void attach () {
		this.mNativePlatformView = nativeAttach(this);
	}

	private boolean isAttached () {
		return this.mNativePlatformView != 0L;
	}

	private void preRun () {
		this.resetAccessibilityTree();
	}

	private void postRun () {
	}

	public void runFromBundle (final String bundlePath, final String snapshotOverride) {
		this.preRun();
		nativeRunBundleAndSnapshot(this.mNativePlatformView, bundlePath, snapshotOverride);
		this.postRun();
	}

	private void runFromSource (final String assetsDirectory, final String main, final String packages) {
		final Runnable runnable = new Runnable() {
			@Override
			public void run () {
				FlutterView.this.preRun();
				FlutterView.nativeRunBundleAndSource(FlutterView.this.mNativePlatformView, assetsDirectory, main, packages);
				FlutterView.this.postRun();
				synchronized (this) {
					this.notify();
				}
			}
		};

		try {
			synchronized (runnable) {
				this.post(runnable);
				runnable.wait();
			}
		} catch (final InterruptedException var8) {
			Log.e("FlutterView", "Thread got interrupted waiting for RunFromSourceRunnable to finish", var8);
		}

	}

	public Bitmap getBitmap () {
		return nativeGetBitmap(this.mNativePlatformView);
	}

	private static native long nativeAttach (FlutterView var0);

	private static native String nativeGetObservatoryUri ();

	private static native void nativeDetach (long var0);

	private static native void nativeSurfaceCreated (long var0, Surface var2, int var3);

	private static native void nativeSurfaceChanged (long var0, int var2, int var3);

	private static native void nativeSurfaceDestroyed (long var0);

	private static native void nativeRunBundleAndSnapshot (long var0, String var2, String var3);

	private static native void nativeRunBundleAndSource (long var0, String var2, String var3, String var4);

	private static native void nativeSetViewportMetrics (long var0, float var2, int var3, int var4, int var5, int var6, int var7,
		int var8);

	private static native Bitmap nativeGetBitmap (long var0);

	private static native void nativeDispatchPlatformMessage (long var0, String var2, ByteBuffer var3, int var4, int var5);

	private static native void nativeDispatchEmptyPlatformMessage (long var0, String var2, int var3);

	private static native void nativeDispatchPointerDataPacket (long var0, ByteBuffer var2, int var3);

	private static native void nativeDispatchSemanticsAction (long var0, int var2, int var3);

	private static native void nativeSetSemanticsEnabled (long var0, boolean var2);

	private static native void nativeInvokePlatformMessageResponseCallback (long var0, int var2, ByteBuffer var3, int var4);

	private static native void nativeInvokePlatformMessageEmptyResponseCallback (long var0, int var2);

	private static native boolean nativeGetIsSoftwareRenderingEnabled ();

	private void updateViewportMetrics () {
		nativeSetViewportMetrics(this.mNativePlatformView, this.mMetrics.devicePixelRatio, this.mMetrics.physicalWidth,
			this.mMetrics.physicalHeight, this.mMetrics.physicalPaddingTop, this.mMetrics.physicalPaddingRight,
			this.mMetrics.physicalPaddingBottom, this.mMetrics.physicalPaddingLeft);
	}

	private void handlePlatformMessage (final String channel, final byte[] message, final int replyId) {
		final BinaryMessenger.BinaryMessageHandler handler = (BinaryMessenger.BinaryMessageHandler)this.mMessageHandlers
			.get(channel);
		if (handler != null) {
			try {
				final ByteBuffer buffer = message == null ? null : ByteBuffer.wrap(message);
				handler.onMessage(buffer, new BinaryMessenger.BinaryReply() {
					private final AtomicBoolean done = new AtomicBoolean(false);

					@Override
					public void reply (final ByteBuffer reply) {
						if (this.done.getAndSet(true)) {
							throw new IllegalStateException("Reply already submitted");
						} else {
							if (reply == null) {
								FlutterView.nativeInvokePlatformMessageEmptyResponseCallback(FlutterView.this.mNativePlatformView,
									replyId);
							} else {
								FlutterView.nativeInvokePlatformMessageResponseCallback(FlutterView.this.mNativePlatformView, replyId,
									reply, reply.position());
							}

						}
					}
				});
			} catch (final Exception var6) {
				Log.e("FlutterView", "Uncaught exception in binary message listener", var6);
				nativeInvokePlatformMessageEmptyResponseCallback(this.mNativePlatformView, replyId);
			}

		} else {
			nativeInvokePlatformMessageEmptyResponseCallback(this.mNativePlatformView, replyId);
		}
	}

	private void handlePlatformMessageResponse (final int replyId, final byte[] reply) {
		final BinaryMessenger.BinaryReply callback = (BinaryMessenger.BinaryReply)this.mPendingReplies
			.remove(Integer.valueOf(replyId));
		if (callback != null) {
			try {
				callback.reply(reply == null ? null : ByteBuffer.wrap(reply));
			} catch (final Exception var5) {
				Log.e("FlutterView", "Uncaught exception in binary message reply handler", var5);
			}
		}

	}

	private void updateSemantics (final ByteBuffer buffer, final String[] strings) {
		try {
			if (this.mAccessibilityNodeProvider != null) {
				buffer.order(ByteOrder.LITTLE_ENDIAN);
				this.mAccessibilityNodeProvider.updateSemantics(buffer, strings);
			}
		} catch (final Exception var4) {
			Log.e("FlutterView", "Uncaught exception while updating semantics", var4);
		}

	}

	protected void dispatchSemanticsAction (final int id, final int action) {
		nativeDispatchSemanticsAction(this.mNativePlatformView, id, action);
	}

	@Override
	protected void onAttachedToWindow () {
		super.onAttachedToWindow();
		this.mAccessibilityEnabled = this.mAccessibilityManager.isEnabled();
		this.mTouchExplorationEnabled = this.mAccessibilityManager.isTouchExplorationEnabled();
		if (this.mAccessibilityEnabled || this.mTouchExplorationEnabled) {
			this.ensureAccessibilityEnabled();
		}

		this.resetWillNotDraw();
		this.mAccessibilityManager.addAccessibilityStateChangeListener(this);
		if (VERSION.SDK_INT >= 19) {
// if (this.mTouchExplorationListener == null) {
// this.mTouchExplorationListener = new FlutterView.TouchExplorationListener();
// }

// this.mAccessibilityManager.addTouchExplorationStateChangeListener(this.mTouchExplorationListener);
		}

	}

	@Override
	protected void onDetachedFromWindow () {
		super.onDetachedFromWindow();
		this.mAccessibilityManager.removeAccessibilityStateChangeListener(this);
		if (VERSION.SDK_INT >= 19) {
// this.mAccessibilityManager.removeTouchExplorationStateChangeListener(this.mTouchExplorationListener);
		}

	}

	private void resetWillNotDraw () {
		if (!this.mIsSoftwareRenderingEnabled) {
			this.setWillNotDraw(!this.mAccessibilityEnabled && !this.mTouchExplorationEnabled);
		} else {
			this.setWillNotDraw(false);
		}

	}

	@Override
	public void onAccessibilityStateChanged (final boolean enabled) {
		if (enabled) {
			this.ensureAccessibilityEnabled();
		} else {
			this.mAccessibilityEnabled = false;
			if (this.mAccessibilityNodeProvider != null) {
				this.mAccessibilityNodeProvider.setAccessibilityEnabled(false);
			}
		}

		this.resetWillNotDraw();
	}

	@Override
	public AccessibilityNodeProvider getAccessibilityNodeProvider () {
		this.ensureAccessibilityEnabled();
		return this.mAccessibilityNodeProvider;
	}

	void ensureAccessibilityEnabled () {
		this.mAccessibilityEnabled = true;
		if (this.mAccessibilityNodeProvider == null) {
			this.mAccessibilityNodeProvider = new AccessibilityBridge(this);
			nativeSetSemanticsEnabled(this.mNativePlatformView, true);
		}

		this.mAccessibilityNodeProvider.setAccessibilityEnabled(true);
	}

	void resetAccessibilityTree () {
		if (this.mAccessibilityNodeProvider != null) {
			this.mAccessibilityNodeProvider.reset();
		}

	}

	private boolean handleAccessibilityHoverEvent (final MotionEvent event) {
		if (!this.mTouchExplorationEnabled) {
			return false;
		} else {
			if (event.getAction() != 9 && event.getAction() != 7) {
				if (event.getAction() != 10) {
					Log.d("flutter", "unexpected accessibility hover event: " + event);
					return false;
				}

				this.mAccessibilityNodeProvider.handleTouchExplorationExit();
			} else {
				this.mAccessibilityNodeProvider.handleTouchExploration(event.getX(), event.getY());
			}

			return true;
		}
	}

	@Override
	public void send (final String channel, final ByteBuffer message) {
		this.send(channel, message, (BinaryMessenger.BinaryReply)null);
	}

	@Override
	public void send (final String channel, final ByteBuffer message, final BinaryMessenger.BinaryReply callback) {
		int replyId = 0;
		if (callback != null) {
			replyId = this.mNextReplyId++;
			this.mPendingReplies.put(Integer.valueOf(replyId), callback);
		}

		if (message == null) {
			nativeDispatchEmptyPlatformMessage(this.mNativePlatformView, channel, replyId);
		} else {
			nativeDispatchPlatformMessage(this.mNativePlatformView, channel, message, message.position(), replyId);
		}

	}

	@Override
	public void setMessageHandler (final String channel, final BinaryMessenger.BinaryMessageHandler handler) {
		if (handler == null) {
			this.mMessageHandlers.remove(channel);
		} else {
			this.mMessageHandlers.put(channel, handler);
		}

	}

	private class DiscoveryReceiver extends BroadcastReceiver {
		private DiscoveryReceiver () {
		}

		@Override
		public void onReceive (final Context context, final Intent intent) {
			final URI observatoryUri = URI.create(FlutterView.nativeGetObservatoryUri());
			final JSONObject discover = new JSONObject();

			try {
				discover.put("id", FlutterView.this.getContext().getPackageName());
				discover.put("observatoryPort", observatoryUri.getPort());
				Log.i("FlutterView", "DISCOVER: " + discover);
			} catch (final JSONException var6) {
				;
			}

		}

		// $FF: synthetic method
		DiscoveryReceiver (final Object x1) {
			this();
		}
	}

// class TouchExplorationListener implements TouchExplorationStateChangeListener {
// public void onTouchExplorationStateChanged (final boolean enabled) {
// if (enabled) {
// FlutterView.this.mTouchExplorationEnabled = true;
// FlutterView.this.ensureAccessibilityEnabled();
// } else {
// FlutterView.this.mTouchExplorationEnabled = false;
// if (FlutterView.this.mAccessibilityNodeProvider != null) {
// FlutterView.this.mAccessibilityNodeProvider.handleTouchExplorationExit();
// }
// }
//
// FlutterView.this.resetWillNotDraw();
// }
// }

	static final class ViewportMetrics {
		float devicePixelRatio = 1.0F;
		int physicalWidth = 0;
		int physicalHeight = 0;
		int physicalPaddingTop = 0;
		int physicalPaddingRight = 0;
		int physicalPaddingBottom = 0;
		int physicalPaddingLeft = 0;
	}

	public interface Provider {
		FlutterView getFlutterView ();
	}
}
