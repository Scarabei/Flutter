package io.flutter.plugin.editing;

import android.text.Editable;
import android.text.Selection;
import android.view.KeyEvent;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.InputMethodManager;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.view.FlutterView;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;

class InputConnectionAdaptor extends BaseInputConnection {
   private final FlutterView mFlutterView;
   private final int mClient;
   private final MethodChannel mFlutterChannel;
   private final Editable mEditable;
   private int mBatchCount;
   private InputMethodManager mImm;

   public InputConnectionAdaptor(FlutterView view, int client, MethodChannel flutterChannel, Editable editable) {
      super(view, true);
      this.mFlutterView = view;
      this.mClient = client;
      this.mFlutterChannel = flutterChannel;
      this.mEditable = editable;
      this.mBatchCount = 0;
      this.mImm = (InputMethodManager)view.getContext().getSystemService("input_method");
   }

   private void updateEditingState() {
      if(this.mBatchCount <= 0) {
         int selectionStart = Selection.getSelectionStart(this.mEditable);
         int selectionEnd = Selection.getSelectionEnd(this.mEditable);
         int composingStart = BaseInputConnection.getComposingSpanStart(this.mEditable);
         int composingEnd = BaseInputConnection.getComposingSpanEnd(this.mEditable);
         this.mImm.updateSelection(this.mFlutterView, selectionStart, selectionEnd, composingStart, composingEnd);
         HashMap state = new HashMap();
         state.put("text", this.mEditable.toString());
         state.put("selectionBase", Integer.valueOf(selectionStart));
         state.put("selectionExtent", Integer.valueOf(selectionEnd));
         state.put("composingBase", Integer.valueOf(composingStart));
         state.put("composingExtent", Integer.valueOf(composingEnd));
         this.mFlutterChannel.invokeMethod("TextInputClient.updateEditingState", Arrays.asList(new Serializable[]{Integer.valueOf(this.mClient), state}));
      }
   }

   public Editable getEditable() {
      return this.mEditable;
   }

   public boolean beginBatchEdit() {
      ++this.mBatchCount;
      return super.beginBatchEdit();
   }

   public boolean endBatchEdit() {
      boolean result = super.endBatchEdit();
      --this.mBatchCount;
      this.updateEditingState();
      return result;
   }

   public boolean commitText(CharSequence text, int newCursorPosition) {
      boolean result = super.commitText(text, newCursorPosition);
      this.updateEditingState();
      return result;
   }

   public boolean deleteSurroundingText(int beforeLength, int afterLength) {
      boolean result = super.deleteSurroundingText(beforeLength, afterLength);
      this.updateEditingState();
      return result;
   }

   public boolean setComposingRegion(int start, int end) {
      boolean result = super.setComposingRegion(start, end);
      this.updateEditingState();
      return result;
   }

   public boolean setComposingText(CharSequence text, int newCursorPosition) {
      boolean result;
      if(text.length() == 0) {
         result = super.commitText(text, newCursorPosition);
      } else {
         result = super.setComposingText(text, newCursorPosition);
      }

      this.updateEditingState();
      return result;
   }

   public boolean setSelection(int start, int end) {
      boolean result = super.setSelection(start, end);
      this.updateEditingState();
      return result;
   }

   public boolean sendKeyEvent(KeyEvent event) {
      boolean result = super.sendKeyEvent(event);
      if(event.getAction() == 1) {
         int selStart;
         if(event.getKeyCode() == 67) {
            selStart = Selection.getSelectionStart(this.mEditable);
            int selEnd = Selection.getSelectionEnd(this.mEditable);
            if(selEnd > selStart) {
               Selection.setSelection(this.mEditable, selStart);
               this.deleteSurroundingText(0, selEnd - selStart);
            } else if(selStart > 0) {
               Selection.setSelection(this.mEditable, selStart - 1);
               this.deleteSurroundingText(0, 1);
            }
         } else {
            selStart = event.getUnicodeChar();
            if(selStart != 0) {
               this.commitText(String.valueOf((char)selStart), 1);
            }
         }
      }

      return result;
   }

   public boolean performEditorAction(int actionCode) {
      this.mFlutterChannel.invokeMethod("TextInputClient.performAction", Arrays.asList(new Serializable[]{Integer.valueOf(this.mClient), "TextInputAction.done"}));
      return true;
   }
}
