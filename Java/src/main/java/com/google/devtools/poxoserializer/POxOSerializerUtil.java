package com.google.devtools.poxoserializer;

import com.google.devtools.poxoserializer.exception.POxOSerializerException;
import com.google.devtools.poxoserializer.serializers.BooleanSerializer;
import com.google.devtools.poxoserializer.serializers.ByteSerializer;
import com.google.devtools.poxoserializer.serializers.CharSerializer;
import com.google.devtools.poxoserializer.serializers.DateSerializer;
import com.google.devtools.poxoserializer.serializers.DoubleSerializer;
import com.google.devtools.poxoserializer.serializers.EnumSerializer;
import com.google.devtools.poxoserializer.serializers.FloatSerializer;
import com.google.devtools.poxoserializer.serializers.GenericClassSerializer;
import com.google.devtools.poxoserializer.serializers.IntegerSerializer;
import com.google.devtools.poxoserializer.serializers.ListSerializer;
import com.google.devtools.poxoserializer.serializers.LongSerializer;
import com.google.devtools.poxoserializer.serializers.MapSerializer;
import com.google.devtools.poxoserializer.serializers.ObjectSerializer;
import com.google.devtools.poxoserializer.serializers.ShortSerializer;
import com.google.devtools.poxoserializer.serializers.StringSerializer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class POxOSerializerUtil {
  private Map<String, Constructor<?>> constructrForClass;

  private Map<String, Class<?>> classForName;
  private Map<Class<?>, String> nameForClass;

  private ClassLoader classLoader;

  public POxOSerializerUtil(ClassLoader classLoader) {
    this.classLoader = classLoader;
    constructrForClass = new TreeMap<String, Constructor<?>>();
    classForName = new TreeMap<String, Class<?>>();
    nameForClass = new HashMap<Class<?>, String>();
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

  @SuppressWarnings("unchecked")
  public <T> T createNewInstance(Class<? extends T> genericClass) throws InstantiationException,
    IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    Constructor<?> ctor = constructrForClass.get(genericClass.getName());
    if (ctor == null) {
      Constructor<?>[] ctors = genericClass.getDeclaredConstructors();
      for (int i = 0; i < ctors.length; i++) {
        ctor = ctors[i];
        if (ctor.getGenericParameterTypes().length == 0)
          break;
      }

      ctor.setAccessible(true);
      constructrForClass.put(genericClass.getName(), ctor);
    }
    T ret = (T) ctor.newInstance();

    return ret;
  }

  public GenericClassSerializer getTypeSerializer(Class<?> fieldType) throws POxOSerializerException {
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
    }
    
    return ret;
  }

  public GenericClassSerializer getFieldSerializer(Field field) throws POxOSerializerException {
    GenericClassSerializer ret = null;
    Class<?> fieldType = field.getType();
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
      POxOSerializerClassPair pair = new POxOSerializerClassPair();
      recirsiveFindSerializer(field.getGenericType(), pair);
      ret = pair.getSerializer();
    } else if (Map.class.isAssignableFrom(fieldType)) {
      POxOSerializerClassPair pair = new POxOSerializerClassPair();
      recirsiveFindSerializer(field.getGenericType(), pair);
      ret = pair.getSerializer();
    } else {
      ret = new ObjectSerializer();
    }
    return ret;
  }

  @SuppressWarnings({
      "rawtypes", "unchecked"
  })
  private void recirsiveFindSerializer(Type genericType, POxOSerializerClassPair pair) throws POxOSerializerException {
    if (genericType instanceof GenericArrayType) {
      Type componentType = ((GenericArrayType) genericType).getGenericComponentType();
      if (componentType instanceof Class<?>) {
        pair.setGenericClass((Class<?>) componentType);
        pair.setSerializer(new ListSerializer((Class<?>) componentType,
            getTypeSerializer((Class<?>) componentType)));
        return;
      } else {
        POxOSerializerClassPair nestedPair = new POxOSerializerClassPair();
        recirsiveFindSerializer(componentType, nestedPair);
        pair.setGenericClass(nestedPair.getGenericClass());
        pair.setSerializer(new ListSerializer(nestedPair.getGenericClass(),
            nestedPair.getSerializer()));
        return;
      }
    }
    if (genericType instanceof ParameterizedType) {
      Type[] actualTypes = ((ParameterizedType) genericType).getActualTypeArguments();
      Class<?> genericClass = (Class<?>) ((ParameterizedType) genericType).getRawType();
      if (Map.class.isAssignableFrom(genericClass)) {
        POxOSerializerClassPair[] serializers = new POxOSerializerClassPair[actualTypes.length];
        for (int i = 0, n = actualTypes.length; i < n; i++) {
          Type actualType = actualTypes[i];

          if (actualType instanceof Class<?>) {
            POxOSerializerClassPair nestedPair = new POxOSerializerClassPair();
            nestedPair.setGenericClass((Class<?>) actualType);
            nestedPair.setSerializer(getTypeSerializer((Class<?>) actualType));
            serializers[i] = nestedPair;
          } else if (actualType instanceof ParameterizedType) {
            POxOSerializerClassPair nestedPair = new POxOSerializerClassPair();
            recirsiveFindSerializer(actualType, nestedPair);
            serializers[i] = nestedPair;
          } else
            continue;
        }
        pair.setGenericClass((Class<?>) ((ParameterizedType) genericType).getRawType());
        pair.setSerializer(new MapSerializer(serializers[0].getGenericClass(),
            serializers[1].getGenericClass(), serializers[0].getSerializer(),
            serializers[1].getSerializer()));
      } else if (List.class.isAssignableFrom(genericClass)) {
        for (int i = 0, n = actualTypes.length; i < n; i++) {
          Type actualType = actualTypes[i];

          if (actualType instanceof Class<?>) {
            pair.setGenericClass((Class<?>) ((ParameterizedType) genericType).getRawType());
            pair.setSerializer(new ListSerializer((Class<?>) actualType,
                getTypeSerializer((Class<?>) actualType)));
          } else if (actualType instanceof ParameterizedType) {
            POxOSerializerClassPair nestedPair = new POxOSerializerClassPair();
            recirsiveFindSerializer(actualType, nestedPair);
            pair.setGenericClass((Class<?>) ((ParameterizedType) genericType).getRawType());
            pair.setSerializer(new ListSerializer(nestedPair.getGenericClass(),
                nestedPair.getSerializer()));
          } else
            continue;
        }
      }
    }
  }

  public Class<?> getClassFromName(String className) throws POxOSerializerException {
    Class<?> type = classForName.get(className);
    if (type == null) {
      try {
        type = classLoader.loadClass(className);
      } catch (ClassNotFoundException e) {
        throw new POxOSerializerException("Error during loading class " + className);
      }
      classForName.put(className, type);
      nameForClass.put(type, className);
    }

    return type;
  }

  public String getNameFromClass(Class<?> type) {
    String name = nameForClass.get(type);
    if (name == null) {
      name = type.getName();
      nameForClass.put(type, name);
      classForName.put(name, type);
    }
    return name;
  }
}
