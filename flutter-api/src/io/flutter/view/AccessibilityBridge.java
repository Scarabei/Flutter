package io.flutter.view;

import android.graphics.Rect;
import android.opengl.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeProvider;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

class AccessibilityBridge extends AccessibilityNodeProvider {
   private static final String TAG = "FlutterView";
   private Map mObjects;
   private FlutterView mOwner;
   private boolean mAccessibilityEnabled = false;
   private AccessibilityBridge.SemanticsObject mFocusedObject;
   private AccessibilityBridge.SemanticsObject mHoveredObject;
   private static final int SEMANTICS_ACTION_TAP = 1;
   private static final int SEMANTICS_ACTION_LONG_PRESS = 2;
   private static final int SEMANTICS_ACTION_SCROLL_LEFT = 4;
   private static final int SEMANTICS_ACTION_SCROLL_RIGHT = 8;
   private static final int SEMANTICS_ACTION_SCROLL_UP = 16;
   private static final int SEMANTICS_ACTION_SCROLL_DOWN = 32;
   private static final int SEMANTICS_ACTION_INCREASE = 64;
   private static final int SEMANTICS_ACTION_DECREASE = 128;
   private static final int SEMANTICS_ACTION_SHOW_ON_SCREEN = 256;
   private static final int SEMANTICS_ACTION_SCROLLABLE = 60;
   private static final int SEMANTICS_FLAG_HAS_CHECKED_STATE = 1;
   private static final int SEMANTICS_FLAG_IS_CHECKED = 2;
   private static final int SEMANTICS_FLAG_IS_SELECTED = 4;

   AccessibilityBridge(FlutterView owner) {
      assert owner != null;

      this.mOwner = owner;
      this.mObjects = new HashMap();
   }

   void setAccessibilityEnabled(boolean accessibilityEnabled) {
      this.mAccessibilityEnabled = accessibilityEnabled;
   }

   public AccessibilityNodeInfo createAccessibilityNodeInfo(int virtualViewId) {
      if(virtualViewId == -1) {
         AccessibilityNodeInfo result = AccessibilityNodeInfo.obtain(this.mOwner);
         this.mOwner.onInitializeAccessibilityNodeInfo(result);
         if(this.mObjects.containsKey(Integer.valueOf(0))) {
            result.addChild(this.mOwner, 0);
         }

         return result;
      } else {
         AccessibilityBridge.SemanticsObject object = (AccessibilityBridge.SemanticsObject)this.mObjects.get(Integer.valueOf(virtualViewId));
         if(object == null) {
            return null;
         } else {
            AccessibilityNodeInfo result = AccessibilityNodeInfo.obtain(this.mOwner, virtualViewId);
            result.setPackageName(this.mOwner.getContext().getPackageName());
            result.setClassName("Flutter");
            result.setSource(this.mOwner, virtualViewId);
            if(object.parent != null) {
               assert object.id > 0;

               result.setParent(this.mOwner, object.parent.id);
            } else {
               assert object.id == 0;

               result.setParent(this.mOwner);
            }

            Rect bounds = object.getGlobalRect();
            if(object.parent != null) {
               Rect parentBounds = object.parent.getGlobalRect();
               Rect boundsInParent = new Rect(bounds);
               boundsInParent.offset(-parentBounds.left, -parentBounds.top);
               result.setBoundsInParent(boundsInParent);
            } else {
               result.setBoundsInParent(bounds);
            }

            result.setBoundsInScreen(bounds);
            result.setVisibleToUser(true);
            result.setEnabled(true);
            if((object.actions & 1) != 0) {
               result.addAction(16);
               result.setClickable(true);
            }

            if((object.actions & 2) != 0) {
               result.addAction(32);
               result.setLongClickable(true);
            }

            if((object.actions & 60) != 0) {
               result.addAction(4096);
               result.addAction(8192);
               result.setScrollable(true);
               result.setClassName("android.widget.ScrollView");
            }

            if((object.actions & 64) != 0 || (object.actions & 128) != 0) {
               result.setFocusable(true);
               result.setClassName("android.widget.SeekBar");
               if((object.actions & 64) != 0) {
                  result.addAction(4096);
               }

               if((object.actions & 128) != 0) {
                  result.addAction(8192);
               }
            }

            result.setCheckable((object.flags & 1) != 0);
            result.setChecked((object.flags & 2) != 0);
            result.setSelected((object.flags & 4) != 0);
            result.setText(object.label);
            if(this.mFocusedObject != null && this.mFocusedObject.id == virtualViewId) {
               result.addAction(128);
            } else {
               result.addAction(64);
            }

            if(object.children != null) {
               Iterator var8 = object.children.iterator();

               while(var8.hasNext()) {
                  AccessibilityBridge.SemanticsObject child = (AccessibilityBridge.SemanticsObject)var8.next();
                  result.addChild(this.mOwner, child.id);
               }
            }

            return result;
         }
      }
   }

   public boolean performAction(int virtualViewId, int action, Bundle arguments) {
      AccessibilityBridge.SemanticsObject object = (AccessibilityBridge.SemanticsObject)this.mObjects.get(Integer.valueOf(virtualViewId));
      if(object == null) {
         return false;
      } else {
         switch(action) {
         case 16:
            this.mOwner.dispatchSemanticsAction(virtualViewId, 1);
            return true;
         case 32:
            this.mOwner.dispatchSemanticsAction(virtualViewId, 2);
            return true;
         case 64:
            this.sendAccessibilityEvent(virtualViewId, '耀');
            if(this.mFocusedObject == null) {
               this.mOwner.invalidate();
            }

            this.mFocusedObject = object;
            return true;
         case 128:
            this.sendAccessibilityEvent(virtualViewId, 65536);
            this.mFocusedObject = null;
            return true;
         case 4096:
            if((object.actions & 16) != 0) {
               this.mOwner.dispatchSemanticsAction(virtualViewId, 16);
            } else if((object.actions & 4) != 0) {
               this.mOwner.dispatchSemanticsAction(virtualViewId, 4);
            } else {
               if((object.actions & 64) == 0) {
                  return false;
               }

               this.mOwner.dispatchSemanticsAction(virtualViewId, 64);
            }

            return true;
         case 8192:
            if((object.actions & 32) != 0) {
               this.mOwner.dispatchSemanticsAction(virtualViewId, 32);
            } else if((object.actions & 8) != 0) {
               this.mOwner.dispatchSemanticsAction(virtualViewId, 8);
            } else {
               if((object.actions & 128) == 0) {
                  return false;
               }

               this.mOwner.dispatchSemanticsAction(virtualViewId, 128);
            }

            return true;
         case 16908342:
            this.mOwner.dispatchSemanticsAction(virtualViewId, 256);
            return true;
         default:
            return false;
         }
      }
   }

   private AccessibilityBridge.SemanticsObject getRootObject() {
      assert this.mObjects.containsKey(Integer.valueOf(0));

      return (AccessibilityBridge.SemanticsObject)this.mObjects.get(Integer.valueOf(0));
   }

   private AccessibilityBridge.SemanticsObject getOrCreateObject(int id) {
      AccessibilityBridge.SemanticsObject object = (AccessibilityBridge.SemanticsObject)this.mObjects.get(Integer.valueOf(id));
      if(object == null) {
         object = new AccessibilityBridge.SemanticsObject();
         object.id = id;
         this.mObjects.put(Integer.valueOf(id), object);
      }

      return object;
   }

   void handleTouchExplorationExit() {
      if(this.mHoveredObject != null) {
         this.sendAccessibilityEvent(this.mHoveredObject.id, 256);
         this.mHoveredObject = null;
      }

   }

   void handleTouchExploration(float x, float y) {
      if(!this.mObjects.isEmpty()) {
         AccessibilityBridge.SemanticsObject newObject = this.getRootObject().hitTest(new float[]{x, y, 0.0F, 1.0F});
         if(newObject != this.mHoveredObject) {
            if(newObject != null) {
               this.sendAccessibilityEvent(newObject.id, 128);
            }

            if(this.mHoveredObject != null) {
               this.sendAccessibilityEvent(this.mHoveredObject.id, 256);
            }

            this.mHoveredObject = newObject;
         }

      }
   }

   void updateSemantics(ByteBuffer buffer, String[] strings) {
      ArrayList updated = new ArrayList();

      while(buffer.hasRemaining()) {
         int id = buffer.getInt();
         this.getOrCreateObject(id).updateWith(buffer, strings);
         updated.add(Integer.valueOf(id));
      }

      Set visitedObjects = new HashSet();
      AccessibilityBridge.SemanticsObject rootObject = this.getRootObject();
      if(rootObject != null) {
         float[] identity = new float[16];
         Matrix.setIdentityM(identity, 0);
         rootObject.updateRecursively(identity, visitedObjects, false);
      }

      Iterator it = this.mObjects.entrySet().iterator();

      while(it.hasNext()) {
         Entry entry = (Entry)it.next();
         AccessibilityBridge.SemanticsObject object = (AccessibilityBridge.SemanticsObject)entry.getValue();
         if(!visitedObjects.contains(object)) {
            this.willRemoveSemanticsObject(object);
            it.remove();
         }
      }

      Iterator var11 = updated.iterator();

      while(var11.hasNext()) {
         Integer id = (Integer)var11.next();
         this.sendAccessibilityEvent(id.intValue(), 2048);
      }

   }

   private void sendAccessibilityEvent(int virtualViewId, int eventType) {
      if(this.mAccessibilityEnabled) {
         if(virtualViewId == 0) {
            this.mOwner.sendAccessibilityEvent(eventType);
         } else {
            AccessibilityEvent event = AccessibilityEvent.obtain(eventType);
            event.setPackageName(this.mOwner.getContext().getPackageName());
            event.setSource(this.mOwner, virtualViewId);
            this.mOwner.getParent().requestSendAccessibilityEvent(this.mOwner, event);
         }

      }
   }

   private void willRemoveSemanticsObject(AccessibilityBridge.SemanticsObject object) {
      assert this.mObjects.containsKey(Integer.valueOf(object.id));

      assert this.mObjects.get(Integer.valueOf(object.id)) == object;

      object.parent = null;
      if(this.mFocusedObject == object) {
         this.sendAccessibilityEvent(this.mFocusedObject.id, 65536);
         this.mFocusedObject = null;
      }

      if(this.mHoveredObject == object) {
         this.mHoveredObject = null;
      }

   }

   void reset() {
      this.mObjects.clear();
      if(this.mFocusedObject != null) {
         this.sendAccessibilityEvent(this.mFocusedObject.id, 65536);
      }

      this.mFocusedObject = null;
      this.mHoveredObject = null;
      this.sendAccessibilityEvent(0, 2048);
   }

   private class SemanticsObject {
      int id = -1;
      int flags;
      int actions;
      String label;
      private float left;
      private float top;
      private float right;
      private float bottom;
      private float[] transform;
      AccessibilityBridge.SemanticsObject parent;
      List children;
      private boolean inverseTransformDirty = true;
      private float[] inverseTransform;
      private boolean globalGeometryDirty = true;
      private float[] globalTransform;
      private Rect globalRect;

      void log(String indent) {
         Log.i("FlutterView", indent + "SemanticsObject id=" + this.id + " label=" + this.label + " actions=" + this.actions + " flags=" + this.flags + "\n" + indent + "  +-- rect.ltrb=(" + this.left + ", " + this.top + ", " + this.right + ", " + this.bottom + ")\n" + indent + "  +-- transform=" + Arrays.toString(this.transform) + "\n");
         if(this.children != null) {
            String childIndent = indent + "  ";
            Iterator var3 = this.children.iterator();

            while(var3.hasNext()) {
               AccessibilityBridge.SemanticsObject child = (AccessibilityBridge.SemanticsObject)var3.next();
               child.log(childIndent);
            }
         }

      }

      void updateWith(ByteBuffer buffer, String[] strings) {
         this.flags = buffer.getInt();
         this.actions = buffer.getInt();
         int stringIndex = buffer.getInt();
         if(stringIndex == -1) {
            this.label = null;
         } else {
            this.label = strings[stringIndex];
         }

         this.left = buffer.getFloat();
         this.top = buffer.getFloat();
         this.right = buffer.getFloat();
         this.bottom = buffer.getFloat();
         if(this.transform == null) {
            this.transform = new float[16];
         }

         int childCount;
         for(childCount = 0; childCount < 16; ++childCount) {
            this.transform[childCount] = buffer.getFloat();
         }

         this.inverseTransformDirty = true;
         this.globalGeometryDirty = true;
         childCount = buffer.getInt();
         if(childCount == 0) {
            this.children = null;
         } else {
            if(this.children == null) {
               this.children = new ArrayList(childCount);
            } else {
               this.children.clear();
            }

            for(int i = 0; i < childCount; ++i) {
               AccessibilityBridge.SemanticsObject child = AccessibilityBridge.this.getOrCreateObject(buffer.getInt());
               child.parent = this;
               this.children.add(child);
            }
         }

      }

      private void ensureInverseTransform() {
         if(this.inverseTransformDirty) {
            this.inverseTransformDirty = false;
            if(this.inverseTransform == null) {
               this.inverseTransform = new float[16];
            }

            if(!Matrix.invertM(this.inverseTransform, 0, this.transform, 0)) {
               Arrays.fill(this.inverseTransform, 0.0F);
            }

         }
      }

      Rect getGlobalRect() {
         assert !this.globalGeometryDirty;

         return this.globalRect;
      }

      AccessibilityBridge.SemanticsObject hitTest(float[] point) {
         float w = point[3];
         float x = point[0] / w;
         float y = point[1] / w;
         if(x >= this.left && x < this.right && y >= this.top && y < this.bottom) {
            if(this.children != null) {
               float[] transformedPoint = new float[4];

               for(int i = this.children.size() - 1; i >= 0; --i) {
                  AccessibilityBridge.SemanticsObject child = (AccessibilityBridge.SemanticsObject)this.children.get(i);
                  child.ensureInverseTransform();
                  Matrix.multiplyMV(transformedPoint, 0, child.inverseTransform, 0, point, 0);
                  AccessibilityBridge.SemanticsObject result = child.hitTest(transformedPoint);
                  if(result != null) {
                     return result;
                  }
               }
            }

            return this;
         } else {
            return null;
         }
      }

      void updateRecursively(float[] ancestorTransform, Set visitedObjects, boolean forceUpdate) {
         visitedObjects.add(this);
         if(this.globalGeometryDirty) {
            forceUpdate = true;
         }

         if(forceUpdate) {
            if(this.globalTransform == null) {
               this.globalTransform = new float[16];
            }

            Matrix.multiplyMM(this.globalTransform, 0, ancestorTransform, 0, this.transform, 0);
            float[] sample = new float[]{0.0F, 0.0F, 0.0F, 1.0F};
            float[] point1 = new float[4];
            float[] point2 = new float[4];
            float[] point3 = new float[4];
            float[] point4 = new float[4];
            sample[0] = this.left;
            sample[1] = this.top;
            this.transformPoint(point1, this.globalTransform, sample);
            sample[0] = this.right;
            sample[1] = this.top;
            this.transformPoint(point2, this.globalTransform, sample);
            sample[0] = this.right;
            sample[1] = this.bottom;
            this.transformPoint(point3, this.globalTransform, sample);
            sample[0] = this.left;
            sample[1] = this.bottom;
            this.transformPoint(point4, this.globalTransform, sample);
            if(this.globalRect == null) {
               this.globalRect = new Rect();
            }

            this.globalRect.set(Math.round(this.min(point1[0], point2[0], point3[0], point4[0])), Math.round(this.min(point1[1], point2[1], point3[1], point4[1])), Math.round(this.max(point1[0], point2[0], point3[0], point4[0])), Math.round(this.max(point1[1], point2[1], point3[1], point4[1])));
            this.globalGeometryDirty = false;
         }

         assert this.globalTransform != null;

         assert this.globalRect != null;

         if(this.children != null) {
            for(int i = 0; i < this.children.size(); ++i) {
               ((AccessibilityBridge.SemanticsObject)this.children.get(i)).updateRecursively(this.globalTransform, visitedObjects, forceUpdate);
            }
         }

      }

      private void transformPoint(float[] result, float[] transform, float[] point) {
         Matrix.multiplyMV(result, 0, transform, 0, point, 0);
         float w = result[3];
         result[0] /= w;
         result[1] /= w;
         result[2] /= w;
         result[3] = 0.0F;
      }

      private float min(float a, float b, float c, float d) {
         return Math.min(a, Math.min(b, Math.min(c, d)));
      }

      private float max(float a, float b, float c, float d) {
         return Math.max(a, Math.max(b, Math.max(c, d)));
      }
   }
}
