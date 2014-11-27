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

import java.util.ArrayList;
import java.util.List;

public class ListSerializer extends GenericClassSerializer {

  private Class<?> objectClass;

  private GenericClassSerializer nestedSerializer;

  public ListSerializer(Class<?> objectClass, GenericClassSerializer nestedSerializer) {
    super(true);
    this.objectClass = objectClass;
    this.nestedSerializer = nestedSerializer;
  }

  @Override
  public void write(POxOPrimitiveEncoder encoder, Object value) throws POxOSerializerException {
    List<?> list = (List<?>) value;
    if (canBeNull) {
      if (list == null) {
        encoder.write(0x00);
        return;
      } else {
        encoder.write(0x01);
      }
    }
    encoder.writeVarInt(list.size(), true);
    for (Object o : list) {
      nestedSerializer.write(encoder, o);
    }
  }

  @Override
  public Object read(POxOPrimitiveDecoder decoder) throws POxOSerializerException {
    if (canBeNull) {
      byte isNull = decoder.readByte();
      if (isNull == 0x00) {
        return null;
      }
    }
    
    return createAndFillListOfType(decoder, objectClass);
  }

  @SuppressWarnings("unchecked")
  private <T> List<T> createAndFillListOfType(POxOPrimitiveDecoder decoder, Class<T> type)
    throws POxOSerializerException {
    List<T> list = new ArrayList<T>();

    int size = decoder.readVarInt(true);

    for (int i = 0; i < size; i++) {
      T o = (T) nestedSerializer.read(decoder);
      list.add(o);
    }
    return list;
  }
}
