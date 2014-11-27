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

  private Class<?> keyObjectClass;
  private Class<?> valueObjectClass;

  private GenericClassSerializer keyNestedSerializer;
  private GenericClassSerializer valueNestedSerializer;

  public MapSerializer(Class<?> keyObjectClass, Class<?> valueObjectClass,
      GenericClassSerializer keyNestedSerializer, GenericClassSerializer valueNestedSerializer) {
    super(true);
    this.keyObjectClass = keyObjectClass;
    this.valueObjectClass = valueObjectClass;
    this.keyNestedSerializer = keyNestedSerializer;
    this.valueNestedSerializer = valueNestedSerializer;
  }

  @Override
  public Object read(POxOPrimitiveDecoder decoder) throws POxOSerializerException {
    if (canBeNull) {
      byte isNull = decoder.readByte();
      if (isNull == 0x00) {
        return null;
      }
    }
    
    return createAndFillMapOfType(decoder, keyObjectClass, valueObjectClass);
  }

  @Override
  public void write(POxOPrimitiveEncoder encoder, Object value) throws POxOSerializerException {
    Map<?, ?> map = (Map<?, ?>) value;
    if (canBeNull) {
      if (map == null) {
        encoder.write(0x00);
        return;
      } else {
        encoder.write(0x01);
      }
    }
    encoder.writeVarInt(map.size(), true);
    for (Entry<?, ?> entry : map.entrySet()) {
      keyNestedSerializer.write(encoder, entry.getKey());
      valueNestedSerializer.write(encoder, entry.getValue());
    }
  }

  @SuppressWarnings("unchecked")
  private <K, V> Map<K, V> createAndFillMapOfType(POxOPrimitiveDecoder decoder, Class<K> keyType,
      Class<V> valueType) throws POxOSerializerException {
    int size = decoder.readVarInt(true);

    Map<K, V> map = new HashMap<K, V>();

    for (int i = 0; i < size; i++) {
      K key = (K) keyNestedSerializer.read(decoder);
      V value = (V) valueNestedSerializer.read(decoder);
      map.put(key, value);
    }
    
    return map;
  }
}
