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

package com.github.ggerla.poxoserializer.serializers;

import com.github.ggerla.poxoserializer.exception.POxOSerializerException;
import com.github.ggerla.poxoserializer.io.POxOPrimitiveDecoder;
import com.github.ggerla.poxoserializer.io.POxOPrimitiveEncoder;

@SuppressWarnings("rawtypes")
public class EnumSerializer extends GenericClassSerializer {

  private Class<?> classToSerialize;

  public EnumSerializer(Class<?> classToSerialize) {
    super(false);
    this.classToSerialize = classToSerialize;
  }

  @Override
  public void write(POxOPrimitiveEncoder encoder, Object obj) throws POxOSerializerException {
    encoder.writeVarInt(((Enum) obj).ordinal() + 1, true);
  }

  @Override
  public Object read(POxOPrimitiveDecoder decoder) throws POxOSerializerException {
    int ordinal = decoder.readVarInt(true);
    ordinal--;
    Object[] enumConstants = classToSerialize.getEnumConstants();

    return enumConstants[ordinal];
  }
}
