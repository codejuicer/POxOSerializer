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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ObjectSerializer extends GenericClassSerializer {

  private ClassLoader classLoader;

  private Map<String, Class<?>> classForName;
  private Map<Class<?>, String> nameForClass;
  private Map<String, Constructor<?>> constructrForClass;

  private Map<String, FieldsSerializer> classFieldSerializerMap;

  private Map<String, FieldSerializerUtil[]> fieldsSerializersMap;

  public ObjectSerializer() {
    super(true);
    classForName = new TreeMap<String, Class<?>>();
    nameForClass = new HashMap<Class<?>, String>();
    constructrForClass = new TreeMap<String, Constructor<?>>();
    classFieldSerializerMap = new TreeMap<String, FieldsSerializer>();
    fieldsSerializersMap = new TreeMap<String, FieldSerializerUtil[]>();
    initializePrimitiveType();
  }

  private void initializePrimitiveType() {
    nameForClass.put(Integer.class, "int");
    nameForClass.put(int.class, "int");
    nameForClass.put(Long.class, "long");
    nameForClass.put(long.class, "long");
    nameForClass.put(Short.class, "short");
    nameForClass.put(short.class, "short");
    nameForClass.put(Double.class, "double");
    nameForClass.put(double.class, "double");
    nameForClass.put(Float.class, "float");
    nameForClass.put(float.class, "float");
    nameForClass.put(Boolean.class, "bool");
    nameForClass.put(boolean.class, "bool");
    nameForClass.put(Byte.class, "byte");
    nameForClass.put(byte.class, "byte");
    nameForClass.put(Character.class, "char");
    nameForClass.put(char.class, "char");
    nameForClass.put(String.class, "string");
    nameForClass.put(Date.class, "date");
    nameForClass.put(Enum.class, "enum");
    nameForClass.put(List.class, "list");
    nameForClass.put(Map.class, "map");

    classForName.put("int", Integer.class);
    classForName.put("long", Long.class);
    classForName.put("short", Short.class);
    classForName.put("double", Double.class);
    classForName.put("float", Float.class);
    classForName.put("bool", Boolean.class);
    classForName.put("byte", Byte.class);
    classForName.put("char", Character.class);
    classForName.put("string", String.class);
    classForName.put("date", Date.class);
    classForName.put("enum", Enum.class);
    classForName.put("list", List.class);
    classForName.put("map", Map.class);
  }

  public void setClassLoader(ClassLoader classLoader) {
    this.classLoader = classLoader;
  }

  private Object createNewInstance(Class<?> clazz) throws InstantiationException,
    IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    Constructor<?> ctor = constructrForClass.get(clazz.getName());
    if (ctor == null) {
      Constructor<?>[] ctors = clazz.getDeclaredConstructors();
      for (int i = 0; i < ctors.length; i++) {
        ctor = ctors[i];
        if (ctor.getGenericParameterTypes().length == 0)
          break;
      }

      ctor.setAccessible(true);
      constructrForClass.put(clazz.getName(), ctor);
    }
    Object ret = ctor.newInstance();

    return ret;
  }

  @Override
  public Object read(POxOPrimitiveDecoder decoder, ObjectSerializer serializer)
    throws POxOSerializerException {
    Object obj = null;
    byte isNull = decoder.readByte();
    if (isNull == 0x00) {
      return obj;
    }
    try {
      String className = decoder.readString();

      if (className != null) {
        Class<?> type = classForName.get(className);
        if (type == null) {
          type = classLoader.loadClass(className);
          classForName.put(className, type);
          nameForClass.put(type, className);
        }

        GenericClassSerializer ser = getFieldSerializer(type);
        if (ser != this) {
          obj = ser.read(decoder, serializer);
        } else {
          FieldsSerializer fieldsSerializer = classFieldSerializerMap.get(className);
          if (fieldsSerializer == null) {
            fieldsSerializer = new FieldsSerializer(type, this);
            retrieveOrderedFieldsList(type);
            classFieldSerializerMap.put(className, fieldsSerializer);
          }
          obj = createNewInstance(type);

          fieldsSerializer.read(decoder, this, obj);
        }
      }
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
        | IllegalArgumentException | InvocationTargetException e) {
      throw new POxOSerializerException("Error during object deserializing.", e);
    }

    return obj;
  }

  @Override
  public void write(POxOPrimitiveEncoder encoder, ObjectSerializer serializer, Object obj)
    throws POxOSerializerException {
    if (obj == null) {
      encoder.write(0x00);
      return;
    } else {
      encoder.write(0x01);
    }
    Class<?> type = obj.getClass();

    String name = nameForClass.get(type);
    if(name == null) {
      name = type.getName();
      nameForClass.put(type, name);
      classForName.put(name, type);
    }
    
    encoder.writeString(name);

    GenericClassSerializer ser = getFieldSerializer(type);
    if (ser != this) {
      ser.write(encoder, serializer, obj);
    } else {
      FieldsSerializer fieldsSerializer = classFieldSerializerMap.get(name);
      if (fieldsSerializer == null) {
        fieldsSerializer = new FieldsSerializer(type, this);
        retrieveOrderedFieldsList(type);
        classFieldSerializerMap.put(type.getName(), fieldsSerializer);
      }

      fieldsSerializer.write(encoder, this, obj);
    }
  }

  private void retrieveOrderedFieldsList(Class<?> type) {
    if (fieldsSerializersMap.get(type.getName()) == null) {
      List<FieldSerializerUtil> allFieldsSerializer = new ArrayList<FieldSerializerUtil>();
      Class<?> nextClass = type;
      while (nextClass != Object.class) {
        Field[] declaredFields = nextClass.getDeclaredFields();
        if (declaredFields != null) {
          for (Field f : declaredFields) {
            if (Modifier.isStatic(f.getModifiers()))
              continue;
            allFieldsSerializer.add(new FieldSerializerUtil(f, getFieldSerializer(f.getType())));
          }
        }
        nextClass = nextClass.getSuperclass();
      }

      Collections.sort(allFieldsSerializer, new Comparator<FieldSerializerUtil>() {

        @Override
        public int compare(FieldSerializerUtil object1, FieldSerializerUtil object2) {
          return object1.getField().getName().compareTo(object2.getField().getName());
        }
      });

      fieldsSerializersMap.put(type.getName(),
          allFieldsSerializer.toArray(new FieldSerializerUtil[0]));
    }
  }

  private GenericClassSerializer getFieldSerializer(Class<?> fieldType) {
    GenericClassSerializer ret = null;
    if (Integer.class.isAssignableFrom(fieldType) || int.class.isAssignableFrom(fieldType)) {
      ret = new IntegerSerializer(fieldType);
    } else if (Long.class.isAssignableFrom(fieldType) || long.class.isAssignableFrom(fieldType)) {
      ret = new LongSerializer(fieldType);
    } else if (Short.class.isAssignableFrom(fieldType) || short.class.isAssignableFrom(fieldType)) {
      ret = new ShortSerializer(fieldType);
    } else if (Float.class.isAssignableFrom(fieldType) || float.class.isAssignableFrom(fieldType)) {
      ret = new FloatSerializer(fieldType);
    } else if (Double.class.isAssignableFrom(fieldType)
        || double.class.isAssignableFrom(fieldType)) {
      ret = new DoubleSerializer(fieldType);
    } else if (String.class.isAssignableFrom(fieldType)) {
      ret = new StringSerializer();
    } else if (Byte.class.isAssignableFrom(fieldType) || byte.class.isAssignableFrom(fieldType)) {
      ret = new ByteSerializer(fieldType);
    } else if (Character.class.isAssignableFrom(fieldType)
        || char.class.isAssignableFrom(fieldType)) {
      ret = new CharSerializer(fieldType);
    } else if (Boolean.class.isAssignableFrom(fieldType)
        || boolean.class.isAssignableFrom(fieldType)) {
      ret = new BooleanSerializer(fieldType);
    } else if (Date.class.isAssignableFrom(fieldType)) {
      ret = new DateSerializer();
    } else if (Enum.class.isAssignableFrom(fieldType)) {
      ret = new EnumSerializer(fieldType);
    } else if (List.class.isAssignableFrom(fieldType)) {
      ret = new ListSerializer();
    } else if (Map.class.isAssignableFrom(fieldType)) {
      ret = new MapSerializer();
    } else {
      ret = this;
    }
    return ret;
  }

  public FieldSerializerUtil[] getFieldsSerializers(Class<?> type) {
    return fieldsSerializersMap.get(type.getName());
  }
}
