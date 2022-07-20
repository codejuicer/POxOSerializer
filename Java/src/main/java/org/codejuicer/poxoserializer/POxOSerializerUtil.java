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

package org.codejuicer.poxoserializer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.codejuicer.poxoserializer.exception.POxOSerializerException;
import org.codejuicer.poxoserializer.io.POxOPrimitiveDecoder;
import org.codejuicer.poxoserializer.io.POxOPrimitiveEncoder;
import org.codejuicer.poxoserializer.serializers.BooleanSerializer;
import org.codejuicer.poxoserializer.serializers.ByteSerializer;
import org.codejuicer.poxoserializer.serializers.CharSerializer;
import org.codejuicer.poxoserializer.serializers.DateSerializer;
import org.codejuicer.poxoserializer.serializers.DoubleSerializer;
import org.codejuicer.poxoserializer.serializers.EnumSerializer;
import org.codejuicer.poxoserializer.serializers.FloatSerializer;
import org.codejuicer.poxoserializer.serializers.GenericClassSerializer;
import org.codejuicer.poxoserializer.serializers.IntegerSerializer;
import org.codejuicer.poxoserializer.serializers.ListSerializer;
import org.codejuicer.poxoserializer.serializers.LocalDateSerializer;
import org.codejuicer.poxoserializer.serializers.LocalDateTimeSerializer;
import org.codejuicer.poxoserializer.serializers.LocalTimeSerializer;
import org.codejuicer.poxoserializer.serializers.LongSerializer;
import org.codejuicer.poxoserializer.serializers.MapSerializer;
import org.codejuicer.poxoserializer.serializers.ObjectSerializer;
import org.codejuicer.poxoserializer.serializers.SetSerializer;
import org.codejuicer.poxoserializer.serializers.ShortSerializer;
import org.codejuicer.poxoserializer.serializers.StringSerializer;
import org.codejuicer.poxoserializer.serializers.ZonedDateTimeSerializer;

public class POxOSerializerUtil {
    private Map<String, Constructor<?>> constructrForClass;

    private Map<String, Class<?>> classForName;
    private Map<Class<?>, String> nameForClass;

    private Map<Class<?>, GenericClassSerializer> serializerForClass;

    private ClassLoader classLoader;

    public POxOSerializerUtil() {
        constructrForClass = new TreeMap<String, Constructor<?>>();
        classForName = new TreeMap<String, Class<?>>();
        nameForClass = new HashMap<Class<?>, String>();
        serializerForClass = new HashMap<Class<?>, GenericClassSerializer>();
        classLoader = this.getClass().getClassLoader();
        initializePrimitiveType();
    }

    @SuppressWarnings("unchecked")
    public <T> T createNewInstance(Class<? extends T> genericClass) throws InstantiationException,
        IllegalAccessException, IllegalArgumentException, InvocationTargetException {

        Constructor<?> ctor = constructrForClass.get(genericClass.getName());
        if (ctor == null) {
            Constructor<?>[] ctors = genericClass.getDeclaredConstructors();
            for (int i = 0; i < ctors.length; i++) {
                ctor = ctors[i];
                if (ctor.getGenericParameterTypes().length == 0) {
                    break;
                }
            }

            ctor.setAccessible(true);
            constructrForClass.put(genericClass.getName(), ctor);
        }
        T ret = (T)ctor.newInstance();

        return ret;
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

    public GenericClassSerializer getFieldSerializer(Field field) throws POxOSerializerException {
        GenericClassSerializer ret = null;
        Class<?> fieldType = field.getType();
        if (List.class.isAssignableFrom(fieldType)) {
            POxOSerializerClassPair pair = new POxOSerializerClassPair();
            recursiveFindSerializer(field.getGenericType(), pair);
            ret = pair.getSerializer();
        } else if (Map.class.isAssignableFrom(fieldType)) {
            POxOSerializerClassPair pair = new POxOSerializerClassPair();
            recursiveFindSerializer(field.getGenericType(), pair);
            ret = pair.getSerializer();
        } else {
            ret = getTypeSerializer(fieldType);
        }
        return ret;
    }

    public String getNameFromClass(Class<?> type) {
        String name;
        if (List.class.isAssignableFrom(type)) {
            name = "list";
        } else if (Map.class.isAssignableFrom(type)) {
            name = "map";
        } else {
            name = nameForClass.get(type);
            if (name == null) {
                name = type.getName();
                nameForClass.put(type, name);
                classForName.put(name, type);
            }
        }
        return name;
    }

    public GenericClassSerializer getTypeSerializer(Class<?> fieldType) throws POxOSerializerException {
        GenericClassSerializer ret = serializerForClass.get(fieldType);
        if (ret == null) {
            if (Enum.class.isAssignableFrom(fieldType)) {
                ret = new EnumSerializer(fieldType);
                serializerForClass.put(fieldType, ret);
            } else if (List.class.isAssignableFrom(fieldType)) {
                POxOSerializerClassPair pair = new POxOSerializerClassPair();
                pair.setGenericClass(Object.class);
                pair.setSerializer(serializerForClass.get(Object.class));
                ret = new ListSerializer(pair);
                serializerForClass.put(fieldType, ret);
            } else if (Set.class.isAssignableFrom(fieldType)) {
                POxOSerializerClassPair pair = new POxOSerializerClassPair();
                pair.setGenericClass(Object.class);
                pair.setSerializer(serializerForClass.get(Object.class));
                ret = new SetSerializer(pair);
                serializerForClass.put(fieldType, ret);
            } else if (Map.class.isAssignableFrom(fieldType)) {
                POxOSerializerClassPair keyPair = new POxOSerializerClassPair();
                keyPair.setGenericClass(Object.class);
                keyPair.setSerializer(serializerForClass.get(Object.class));
                POxOSerializerClassPair valuePair = new POxOSerializerClassPair();
                valuePair.setGenericClass(Object.class);
                valuePair.setSerializer(serializerForClass.get(Object.class));
                ret = new MapSerializer(keyPair, valuePair);
                serializerForClass.put(fieldType, ret);
            } else {
                ret = serializerForClass.get(Object.class);
            }
        }

        return ret;
    }

    public List<Class<?>> readSubClassParameterType(Type genericType, POxOPrimitiveDecoder decoder) {
        List<Class<?>> ret = new ArrayList<Class<?>>();
        if (genericType instanceof ParameterizedType) {
            ParameterizedType type = (ParameterizedType)genericType;
            Type[] typeArguments = type.getActualTypeArguments();

            for (Type typeArgument : typeArguments) {
                if (typeArgument instanceof Class<?>) {
                    ret.add((Class<?>)typeArgument);
                    decoder.readString();
                } else {
                    ret.addAll(readSubClassParameterType(typeArgument, decoder));
                }
            }
        } else {
            ret.add(genericType.getClass());
            // decoder.readString();
        }
        return ret;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public List<Class<?>> writeSubClassParameterType(Type genericType, POxOPrimitiveEncoder encoder) {
        List<Class<?>> ret = new ArrayList<Class<?>>();
        if (genericType instanceof ParameterizedType) {
            ParameterizedType type = (ParameterizedType)genericType;
            Type[] typeArguments = type.getActualTypeArguments();

            for (Type typeArgument : typeArguments) {
                if (typeArgument instanceof Class<?>) {
                    ret.add((Class<?>)typeArgument);
                    encoder.writeString(getNameFromClass((Class<?>)typeArgument));
                } else {
                    ret.addAll(writeSubClassParameterType(typeArgument, encoder));
                }
            }
        } else {
            ret.add(genericType.getClass());
            // encoder.writeString("object");
        }
        return ret;
    }

    private void initializePrimitiveType() {
        serializerForClass.put(Object.class, new ObjectSerializer(this));

        nameForClass.put(Integer.class, "int");
        serializerForClass.put(Integer.class, new IntegerSerializer(Integer.class));

        nameForClass.put(int.class, "int");
        serializerForClass.put(int.class, new IntegerSerializer(int.class));

        nameForClass.put(Long.class, "long");
        serializerForClass.put(Long.class, new LongSerializer(Long.class));

        nameForClass.put(long.class, "long");
        serializerForClass.put(long.class, new LongSerializer(long.class));

        nameForClass.put(Short.class, "short");
        serializerForClass.put(Short.class, new ShortSerializer(Short.class));

        nameForClass.put(short.class, "short");
        serializerForClass.put(short.class, new ShortSerializer(short.class));

        nameForClass.put(Double.class, "double");
        serializerForClass.put(Double.class, new DoubleSerializer(Double.class));

        nameForClass.put(double.class, "double");
        serializerForClass.put(double.class, new DoubleSerializer(double.class));

        nameForClass.put(Float.class, "float");
        serializerForClass.put(Float.class, new FloatSerializer(Float.class));

        nameForClass.put(float.class, "float");
        serializerForClass.put(float.class, new FloatSerializer(float.class));

        nameForClass.put(Boolean.class, "bool");
        serializerForClass.put(Boolean.class, new BooleanSerializer(Boolean.class));

        nameForClass.put(boolean.class, "bool");
        serializerForClass.put(boolean.class, new BooleanSerializer(boolean.class));

        nameForClass.put(Byte.class, "byte");
        serializerForClass.put(Byte.class, new ByteSerializer(Byte.class));

        nameForClass.put(byte.class, "byte");
        serializerForClass.put(byte.class, new ByteSerializer(byte.class));

        nameForClass.put(Character.class, "char");
        serializerForClass.put(Character.class, new CharSerializer(Character.class));

        nameForClass.put(char.class, "char");
        serializerForClass.put(char.class, new CharSerializer(char.class));

        nameForClass.put(String.class, "string");
        serializerForClass.put(String.class, new StringSerializer());

        nameForClass.put(Date.class, "date");
        serializerForClass.put(Date.class, new DateSerializer());

        nameForClass.put(ZonedDateTime.class, "zdt");
        serializerForClass.put(ZonedDateTime.class, new ZonedDateTimeSerializer());

        nameForClass.put(LocalDateTime.class, "ldt");
        serializerForClass.put(LocalDateTime.class, new LocalDateTimeSerializer());

        nameForClass.put(LocalDateTime.class, "ld");
        serializerForClass.put(LocalDate.class, new LocalDateSerializer());

        nameForClass.put(LocalDateTime.class, "lt");
        serializerForClass.put(LocalTime.class, new LocalTimeSerializer());

        nameForClass.put(Enum.class, "enum");
        serializerForClass.put(Enum.class, new EnumSerializer(Enum.class));

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
        classForName.put("zdt", ZonedDateTime.class);
        classForName.put("ldt", LocalDateTime.class);
        classForName.put("ld", LocalDate.class);
        classForName.put("lt", LocalTime.class);
        classForName.put("enum", Enum.class);
        classForName.put("list", List.class);
        classForName.put("map", Map.class);
    }

    private void recursiveFindSerializer(Type genericType, POxOSerializerClassPair pair)
        throws POxOSerializerException {
        if (genericType instanceof GenericArrayType) {
            Type componentType = ((GenericArrayType)genericType).getGenericComponentType();
            if (componentType instanceof Class<?>) {
                pair.setGenericClass((Class<?>)componentType);
                pair.setSerializer(new ListSerializer(pair));
            } else {
                POxOSerializerClassPair nestedPair = new POxOSerializerClassPair();
                recursiveFindSerializer(componentType, nestedPair);
                pair.setGenericClass(nestedPair.getGenericClass());
                pair.setSerializer(new ListSerializer(nestedPair));
            }
        } else if (genericType instanceof ParameterizedType) {
            Type[] actualTypes = ((ParameterizedType)genericType).getActualTypeArguments();
            Class<?> genericClass = (Class<?>)((ParameterizedType)genericType).getRawType();
            if (Map.class.isAssignableFrom(genericClass)) {
                POxOSerializerClassPair[] serializers = new POxOSerializerClassPair[actualTypes.length];
                for (int i = 0, n = actualTypes.length; i < n; i++) {
                    Type actualType = actualTypes[i];

                    if (actualType instanceof Class<?>) {
                        POxOSerializerClassPair nestedPair = new POxOSerializerClassPair();
                        nestedPair.setGenericClass((Class<?>)actualType);
                        nestedPair.setSerializer(getTypeSerializer((Class<?>)actualType));
                        serializers[i] = nestedPair;
                    } else if (actualType instanceof ParameterizedType) {
                        POxOSerializerClassPair nestedPair = new POxOSerializerClassPair();
                        recursiveFindSerializer(actualType, nestedPair);
                        serializers[i] = nestedPair;
                    } else {
                        continue;
                    }
                }
                pair.setGenericClass(genericClass);
                pair.setSerializer(new MapSerializer(serializers[0], serializers[1]));
            } else if (List.class.isAssignableFrom(genericClass)) {
                for (int i = 0, n = actualTypes.length; i < n; i++) {
                    Type actualType = actualTypes[i];
                    POxOSerializerClassPair nestedPair = new POxOSerializerClassPair();
                    recursiveFindSerializer(actualType, nestedPair);

                    if (actualType instanceof Class<?>) {
                        pair.setGenericClass(genericClass);
                        pair.setSerializer(new ListSerializer(nestedPair));
                    } else if (actualType instanceof ParameterizedType) {
                        nestedPair = new POxOSerializerClassPair();
                        recursiveFindSerializer(actualType, nestedPair);
                        pair.setGenericClass((Class<?>)((ParameterizedType)genericType).getRawType());
                        pair.setSerializer(new ListSerializer(nestedPair));
                    } else {
                        continue;
                    }
                }
            } else if (Set.class.isAssignableFrom(genericClass)) {
                for (int i = 0, n = actualTypes.length; i < n; i++) {
                    Type actualType = actualTypes[i];
                    POxOSerializerClassPair nestedPair = new POxOSerializerClassPair();
                    recursiveFindSerializer(actualType, nestedPair);

                    if (actualType instanceof Class<?>) {
                        pair.setGenericClass(genericClass);
                        pair.setSerializer(new SetSerializer(nestedPair));
                    } else if (actualType instanceof ParameterizedType) {
                        nestedPair = new POxOSerializerClassPair();
                        recursiveFindSerializer(actualType, nestedPair);
                        pair.setGenericClass((Class<?>)((ParameterizedType)genericType).getRawType());
                        pair.setSerializer(new SetSerializer(nestedPair));
                    } else {
                        continue;
                    }
                }
            }
        } else {
            pair.setGenericClass((Class<?>)genericType);
            pair.setSerializer(getTypeSerializer((Class<?>)genericType));
        }
    }

    // public Type WriteGenericMethodRecursive(Class<?> nestedGenericType, POxOPrimitiveEncoder encoder)
    // {
    // Class<?> ret = nestedGenericType;
    // Class<?> currentType = ret;
    //
    // Type t = currentType.getGenericSuperclass();
    //
    // for (int i = 0; i < ; i++)
    // {
    // encoder.writeString(getNameFromClass(currentType.GetGenericArguments()[i]));
    // }
    // foreach (Type t in currentType.GetGenericArguments())
    // {
    // WriteGenericMethodRecursive(t, encoder);
    // }
    // return ret;
    // }
}
