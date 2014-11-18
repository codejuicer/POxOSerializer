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

import java.util.Date;

public class DateSerializer extends GenericClassSerializer {

  public DateSerializer() {
    super(true);
  }

  @Override
  public void write(POxOPrimitiveEncoder encoder, ObjectSerializer serializer, Object value)
    throws POxOSerializerException {
    if (canBeNull) {
      if (value == null) {
        encoder.write(0x00);
        return;
      } else {
        encoder.write(0x01);
      }
    }

    long longValue = ((Date) value).getTime();
    encoder.writeLong(longValue, true);
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
    Date value = new Date(decoder.readLong(true));
    return value;
  }
}
