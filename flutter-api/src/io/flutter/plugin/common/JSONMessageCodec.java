package io.flutter.plugin.common;

import java.nio.ByteBuffer;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public final class JSONMessageCodec implements MessageCodec {
   public static final JSONMessageCodec INSTANCE = new JSONMessageCodec();

   public ByteBuffer encodeMessage(Object message) {
      if(message == null) {
         return null;
      } else {
         Object wrapped = JSONUtil.wrap(message);
         return wrapped instanceof String?StringCodec.INSTANCE.encodeMessage(JSONObject.quote((String)wrapped)):StringCodec.INSTANCE.encodeMessage(wrapped.toString());
      }
   }

   public Object decodeMessage(ByteBuffer message) {
      if(message == null) {
         return null;
      } else {
         try {
            String json = StringCodec.INSTANCE.decodeMessage(message);
            JSONTokener tokener = new JSONTokener(json);
            Object value = tokener.nextValue();
            if(tokener.more()) {
               throw new IllegalArgumentException("Invalid JSON");
            } else {
               return value;
            }
         } catch (JSONException var5) {
            throw new IllegalArgumentException("Invalid JSON", var5);
         }
      }
   }
}
