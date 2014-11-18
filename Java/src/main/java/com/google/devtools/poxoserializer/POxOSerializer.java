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

package com.google.devtools.poxoserializer;

import com.google.devtools.poxoserializer.exception.POxOSerializerException;
import com.google.devtools.poxoserializer.io.POxOPrimitiveDecoder;
import com.google.devtools.poxoserializer.io.POxOPrimitiveEncoder;
import com.google.devtools.poxoserializer.serializers.ObjectSerializer;

import java.io.IOException;

public class POxOSerializer {
  private POxOPrimitiveDecoder input;

  private POxOPrimitiveEncoder output;

  private ObjectSerializer objSerializer;
  
  public POxOSerializer() {
    objSerializer = new ObjectSerializer();
    objSerializer.setClassLoader(this.getClass().getClassLoader());
  }

  public Object deserialize(byte[] bytes) throws POxOSerializerException {
    input = new POxOPrimitiveDecoder(bytes);

    Object ret = objSerializer.read(input,objSerializer);

    try {
      input.close();
    } catch (IOException e) {
      throw new POxOSerializerException("Error during decoder stream closing.", e);
    }
    return ret;
  }

  public byte[] serialize(Object obj) throws POxOSerializerException {

    if (obj == null) {
      throw new IllegalArgumentException("It is not possible serialize null object");
    }

    output = new POxOPrimitiveEncoder(2048);

    objSerializer.write(output, objSerializer, obj);
    byte[] ret = output.toByteArray();

    try {
      output.close();
    } catch (IOException e) {
      throw new POxOSerializerException("Error during encoder stream closing.", e);
    }

    return ret;
  }

  public void setClassLoader(ClassLoader classLoader) {
    objSerializer.setClassLoader(classLoader);
  }
}
