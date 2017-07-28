
package io.flutter.plugin.editing;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.Editable;
import android.text.Editable.Factory;
import android.text.Selection;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import io.flutter.plugin.common.JSONMethodCodec;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.view.FlutterView;

public class TextInputPlugin implements MethodChannel.MethodCallHandler {
	static {
		if (1 == 1) {
			throw new RuntimeException("Stub!");
		}
	}
	private final FlutterView mView;
	private final InputMethodManager mImm;
	private final MethodChannel mFlutterChannel;
	private int mClient = 0;
	private JSONObject mConfiguration;
	private Editable mEditable;
	private boolean mRestartInputPending;

	public TextInputPlugin (final FlutterView view) {
		this.mView = view;
		this.mImm = (InputMethodManager)view.getContext().getSystemService("input_method");
		this.mFlutterChannel = new MethodChannel(view, "flutter/textinput", JSONMethodCodec.INSTANCE);
		this.mFlutterChannel.setMethodCallHandler(this);
	}

	@Override
	public void onMethodCall (final MethodCall call, final MethodChannel.Result result) {
		final String method = call.method;
		final Object args = call.arguments;

		try {
			if (method.equals("TextInput.show")) {
				this.showTextInput(this.mView);
				result.success((Object)null);
			} else if (method.equals("TextInput.hide")) {
				this.hideTextInput(this.mView);
				result.success((Object)null);
			} else if (method.equals("TextInput.setClient")) {
				final JSONArray argumentList = (JSONArray)args;
				this.setTextInputClient(this.mView, argumentList.getInt(0), argumentList.getJSONObject(1));
				result.success((Object)null);
			} else if (method.equals("TextInput.setEditingState")) {
				this.setTextInputEditingState(this.mView, (JSONObject)args);
				result.success((Object)null);
			} else if (method.equals("TextInput.clearClient")) {
				this.clearTextInputClient();
				result.success((Object)null);
			} else {
				result.notImplemented();
			}
		} catch (final JSONException var6) {
			result.error("error", "JSON error: " + var6.getMessage(), (Object)null);
		}

	}

	private static int inputTypeFromTextInputType (final String inputType, final boolean obscureText, final boolean autocorrect) {
		if (inputType.equals("TextInputType.datetime")) {
			return 4;
		} else if (inputType.equals("TextInputType.number")) {
			return 2;
		} else if (inputType.equals("TextInputType.phone")) {
			return 3;
		} else {
			int textType = 1;
			if (inputType.equals("TextInputType.emailAddress")) {
				textType |= 32;
			} else if (inputType.equals("TextInputType.url")) {
				textType |= 16;
			}

			if (obscureText) {
				textType |= 524288;
				textType |= 128;
			} else if (autocorrect) {
				textType |= '耀';
			}

			return textType;
		}
	}

	public InputConnection createInputConnection (final FlutterView view, final EditorInfo outAttrs) throws JSONException {
		if (this.mClient == 0) {
			return null;
		} else {
			outAttrs.inputType = inputTypeFromTextInputType(this.mConfiguration.getString("inputType"),
				this.mConfiguration.optBoolean("obscureText"), this.mConfiguration.optBoolean("autocorrect", true));
			if (!this.mConfiguration.isNull("actionLabel")) {
				outAttrs.actionLabel = this.mConfiguration.getString("actionLabel");
			}

			outAttrs.imeOptions = 33554438;
			final InputConnectionAdaptor connection = new InputConnectionAdaptor(view, this.mClient, this.mFlutterChannel,
				this.mEditable);
			outAttrs.initialSelStart = Math.max(Selection.getSelectionStart(this.mEditable), 0);
			outAttrs.initialSelEnd = Math.max(Selection.getSelectionEnd(this.mEditable), 0);
			return connection;
		}
	}

	private void showTextInput (final FlutterView view) {
		this.mImm.showSoftInput(view, 0);
	}

	private void hideTextInput (final FlutterView view) {
		this.mImm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
	}

	private void setTextInputClient (final FlutterView view, final int client, final JSONObject configuration) {
		this.mClient = client;
		this.mConfiguration = configuration;
		this.mEditable = Factory.getInstance().newEditable("");
		this.mRestartInputPending = true;
	}

	private void applyStateToSelection (final JSONObject state) throws JSONException {
		final int selStart = state.getInt("selectionBase");
		final int selEnd = state.getInt("selectionExtent");
		if (selStart >= 0 && selStart <= this.mEditable.length() && selEnd >= 0 && selEnd <= this.mEditable.length()) {
			Selection.setSelection(this.mEditable, selStart, selEnd);
		} else {
			Selection.removeSelection(this.mEditable);
		}

	}

	private void setTextInputEditingState (final FlutterView view, final JSONObject state) throws JSONException {
		if (!this.mRestartInputPending && state.getString("text").equals(this.mEditable.toString())) {
			this.applyStateToSelection(state);
			this.mImm.updateSelection(this.mView, Math.max(Selection.getSelectionStart(this.mEditable), 0),
				Math.max(Selection.getSelectionEnd(this.mEditable), 0), BaseInputConnection.getComposingSpanStart(this.mEditable),
				BaseInputConnection.getComposingSpanEnd(this.mEditable));
		} else {
			this.mEditable.replace(0, this.mEditable.length(), state.getString("text"));
			this.applyStateToSelection(state);
			this.mImm.restartInput(view);
			this.mRestartInputPending = false;
		}

	}

	private void clearTextInputClient () {
		this.mClient = 0;
	}
}
