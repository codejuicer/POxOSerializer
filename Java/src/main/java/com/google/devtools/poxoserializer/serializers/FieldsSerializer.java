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

public class FieldsSerializer {

  private ObjectSerializer objectSerializer;

  private Class<?> type;

  public FieldsSerializer(Class<?> classToSerialize, ObjectSerializer objectSerializer) {
    this.objectSerializer = objectSerializer;
    this.type = classToSerialize;
  }

  public void read(POxOPrimitiveDecoder decoder, Object obj)
    throws POxOSerializerException {
    try {
      FieldSerializerUtil[] fieldsSerializerList = objectSerializer.getFieldsSerializers(type);
      for (int i = 0, n = fieldsSerializerList.length; i < n; i++)
        fieldsSerializerList[i].getField().set(obj,
            fieldsSerializerList[i].getSerializer().read(decoder));
    } catch (IllegalArgumentException | IllegalAccessException e) {
      throw new POxOSerializerException("Error during fields deserializing. ", e);
    }
  }

  public void write(POxOPrimitiveEncoder encoder, Object obj)
    throws POxOSerializerException {
    try {
      FieldSerializerUtil[] fieldsSerializerList = objectSerializer.getFieldsSerializers(type);
      for (int i = 0, n = fieldsSerializerList.length; i < n; i++)
        fieldsSerializerList[i].getSerializer().write(encoder,
            fieldsSerializerList[i].getField().get(obj));
    } catch (IllegalArgumentException | IllegalAccessException e) {
      throw new POxOSerializerException("Error during fields serializing. ", e);
    }
  }
}
