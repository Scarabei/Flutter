package io.flutter.plugin.common;

import java.nio.ByteBuffer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class JSONMethodCodec implements MethodCodec {
   public static final JSONMethodCodec INSTANCE = new JSONMethodCodec();

   public ByteBuffer encodeMethodCall(MethodCall methodCall) {
      try {
         JSONObject map = new JSONObject();
         map.put("method", methodCall.method);
         map.put("args", JSONUtil.wrap(methodCall.arguments));
         return JSONMessageCodec.INSTANCE.encodeMessage(map);
      } catch (JSONException var3) {
         throw new IllegalArgumentException("Invalid JSON", var3);
      }
   }

   public MethodCall decodeMethodCall(ByteBuffer message) {
      try {
         Object json = JSONMessageCodec.INSTANCE.decodeMessage(message);
         if(json instanceof JSONObject) {
            JSONObject map = (JSONObject)json;
            Object method = map.get("method");
            Object arguments = this.unwrapNull(map.opt("args"));
            if(method instanceof String) {
               return new MethodCall((String)method, arguments);
            }
         }

         throw new IllegalArgumentException("Invalid method call: " + json);
      } catch (JSONException var6) {
         throw new IllegalArgumentException("Invalid JSON", var6);
      }
   }

   public ByteBuffer encodeSuccessEnvelope(Object result) {
      return JSONMessageCodec.INSTANCE.encodeMessage((new JSONArray()).put(JSONUtil.wrap(result)));
   }

   public ByteBuffer encodeErrorEnvelope(String errorCode, String errorMessage, Object errorDetails) {
      return JSONMessageCodec.INSTANCE.encodeMessage((new JSONArray()).put(errorCode).put(JSONUtil.wrap(errorMessage)).put(JSONUtil.wrap(errorDetails)));
   }

   public Object decodeEnvelope(ByteBuffer envelope) {
      try {
         Object json = JSONMessageCodec.INSTANCE.decodeMessage(envelope);
         if(json instanceof JSONArray) {
            JSONArray array = (JSONArray)json;
            if(array.length() == 1) {
               return this.unwrapNull(array.opt(0));
            }

            if(array.length() == 3) {
               Object code = array.get(0);
               Object message = this.unwrapNull(array.opt(1));
               Object details = this.unwrapNull(array.opt(2));
               if(code instanceof String && (message == null || message instanceof String)) {
                  throw new FlutterException((String)code, (String)message, details);
               }
            }
         }

         throw new IllegalArgumentException("Invalid envelope: " + json);
      } catch (JSONException var7) {
         throw new IllegalArgumentException("Invalid JSON", var7);
      }
   }

   Object unwrapNull(Object value) {
      return value == JSONObject.NULL?null:value;
   }
}
