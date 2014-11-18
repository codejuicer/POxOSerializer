/*
 * Copyright 2014 Giuseppe Gerla. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.devtools.poxoserializer.serializers;

import com.google.devtools.poxoserializer.exception.POxOSerializerException;
import com.google.devtools.poxoserializer.io.POxOPrimitiveDecoder;
import com.google.devtools.poxoserializer.io.POxOPrimitiveEncoder;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class MapSerializer extends GenericClassSerializer {

  public MapSerializer() {
    super(true);
  }

  @Override
  public Object read(POxOPrimitiveDecoder decoder, ObjectSerializer serializer)
    throws POxOSerializerException {
    if (canBeNull) {
      byte isNull = decoder.readByte();
      if (isNull == 0x00) {
        return null;
      }
    }
    int size = decoder.readVarInt(true);

    Map<Object, Object> list = new HashMap<Object, Object>();

    for (int i = 0; i < size; i++) {
      Object key = serializer.read(decoder, serializer);
      Object value = serializer.read(decoder, serializer);
      list.put(key, value);
    }

    return list;
  }

  @Override
  public void write(POxOPrimitiveEncoder encoder, ObjectSerializer serializer, Object value)
    throws POxOSerializerException {
    Map<Object, Object> map;
    map = (Map<Object, Object>) value;
    if (canBeNull) {
      if (map == null) {
        encoder.write(0x00);
        return;
      } else {
        encoder.write(0x01);
      }
    }
    encoder.writeVarInt(map.size(), true);
    for (Entry<Object, Object> entry : map.entrySet()) {
      serializer.write(encoder, serializer, entry.getKey());
      serializer.write(encoder, serializer, entry.getValue());
    }
  }
}
