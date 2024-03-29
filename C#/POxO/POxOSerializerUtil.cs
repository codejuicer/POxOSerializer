﻿/*
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

using System;
using System.Collections.Generic;
using System.Reflection;

namespace POxO
{
    public class POxOSerializerUtil
    {
        private Dictionary<Type, ConstructorInfo> typeConstructorMap;

        private Dictionary<String, Type> classForName;
        private Dictionary<Type, String> nameForClass;

        private Dictionary<Type, GenericClassSerializer> serializerForClass;

        public POxOSerializerUtil()
        {
            classForName = new Dictionary<String, Type>();
            nameForClass = new Dictionary<Type, String>();
            typeConstructorMap = new Dictionary<Type, ConstructorInfo>();
            serializerForClass = new Dictionary<Type, GenericClassSerializer>();
            initializePrimitiveType();
        }

        private void initializePrimitiveType()
        {
            nameForClass.Add(typeof(Int32), "int");
            nameForClass.Add(typeof(Int64), "long");
            nameForClass.Add(typeof(Int16), "short");
            nameForClass.Add(typeof(Double), "double");
            nameForClass.Add(typeof(Single), "float");
            nameForClass.Add(typeof(Boolean), "bool");
            nameForClass.Add(typeof(SByte), "byte");
            nameForClass.Add(typeof(Char), "char");
            nameForClass.Add(typeof(String), "string");
            nameForClass.Add(typeof(DateTime), "date");
            nameForClass.Add(typeof(Enum), "enum");
            
            classForName.Add("int", typeof(Int32));
            classForName.Add("long", typeof(Int64));
            classForName.Add("short", typeof(Int16));
            classForName.Add("double", typeof(Double));
            classForName.Add("float", typeof(float));
            classForName.Add("bool", typeof(Boolean));
            classForName.Add("byte", typeof(SByte));
            classForName.Add("char", typeof(Char));
            classForName.Add("string", typeof(String));
            classForName.Add("date", typeof(DateTime));
            classForName.Add("enum", typeof(Enum));
            classForName.Add("list", typeof(IList<>));
            classForName.Add("set", typeof(ISet<>));
            classForName.Add("map", typeof(IDictionary<,>));

            serializerForClass.Add(typeof(Object), new ObjectSerializer(this));
            serializerForClass.Add(typeof(int), new IntegerSerializer(typeof(int)));
            serializerForClass.Add(typeof(int?), new IntegerSerializer(typeof(int?)));
            serializerForClass.Add(typeof(long), new LongSerializer(typeof(long)));
            serializerForClass.Add(typeof(long?), new LongSerializer(typeof(long?)));
            serializerForClass.Add(typeof(short), new ShortSerializer(typeof(short)));
            serializerForClass.Add(typeof(short?), new ShortSerializer(typeof(short?)));
            serializerForClass.Add(typeof(double), new DoubleSerializer(typeof(double)));
            serializerForClass.Add(typeof(double?), new DoubleSerializer(typeof(double?)));
            serializerForClass.Add(typeof(float), new FloatSerializer(typeof(float)));
            serializerForClass.Add(typeof(float?), new FloatSerializer(typeof(float?)));
            serializerForClass.Add(typeof(bool), new BooleanSerializer(typeof(bool)));
            serializerForClass.Add(typeof(bool?), new BooleanSerializer(typeof(bool?)));
            serializerForClass.Add(typeof(sbyte), new ByteSerializer(typeof(sbyte)));
            serializerForClass.Add(typeof(sbyte?), new ByteSerializer(typeof(sbyte?)));
            serializerForClass.Add(typeof(char), new CharSerializer(typeof(char)));
            serializerForClass.Add(typeof(char?), new CharSerializer(typeof(char?)));
            serializerForClass.Add(typeof(string), new StringSerializer());
            serializerForClass.Add(typeof(DateTime), new DateSerializer());
            serializerForClass.Add(typeof(Enum), new EnumSerializer(typeof(Enum)));
        }

        public Object createNewInstance(Type clazz)
        {
            Object ret = null;
            if (!typeConstructorMap.ContainsKey(clazz))
            {
                try
                {
                    ConstructorInfo cInfo = clazz.GetConstructor(BindingFlags.NonPublic | BindingFlags.CreateInstance | BindingFlags.Instance | BindingFlags.Public,
                                    null, new Type[] { }, null);
                    typeConstructorMap.Add(clazz, cInfo);
                    ret = cInfo.Invoke(new object[] { });
                }
                catch
                {
                    ret = null;
                }
            }
            else
            {
                ConstructorInfo cInfo = typeConstructorMap[clazz];
                ret = cInfo.Invoke(new object[] { });
            }


            return ret;
        }

        public GenericClassSerializer GetTypeSerializer(Type fieldType)
        {
            GenericClassSerializer ret = null;
            if (serializerForClass.ContainsKey(fieldType))
            {
                ret = serializerForClass[fieldType];
            }
            else
            {
                if (typeof(Enum).IsAssignableFrom(fieldType))
                {
                    ret = new EnumSerializer(fieldType);
                    serializerForClass.Add(fieldType, ret);
                }
                else
                {
                    if (fieldType.IsGenericType)
                    {
                        if ((fieldType.GetGenericTypeDefinition() == typeof(IList<>)) || (fieldType.GetGenericTypeDefinition() == typeof(List<>)))
                        {
                            POxOSerializerClassPair pair = new POxOSerializerClassPair();
                            pair.setGenericClass(typeof(Object));
                            pair.setSerializer(serializerForClass[typeof(Object)]);
                            ret = new ListSerializer(pair);
                            serializerForClass.Add(fieldType, ret);
                        }
                        else
                        {
                            if ((fieldType.GetGenericTypeDefinition() == typeof(ISet<>)) || (fieldType.GetGenericTypeDefinition() == typeof(HashSet<>)))
                            {
                                Type genType = fieldType.GetGenericArguments()[0];
                                POxOSerializerClassPair pair = new POxOSerializerClassPair();
                                pair.setGenericClass(typeof(Object));
                                pair.setSerializer(serializerForClass[typeof(Object)]);
                                ret = new SetSerializer(pair);
                                serializerForClass.Add(fieldType, ret);
                            }
                            else
                            {
                                if ((fieldType.GetGenericTypeDefinition() == typeof(IDictionary<,>)) || (fieldType.GetGenericTypeDefinition() == typeof(Dictionary<,>)))
                                {
                                    POxOSerializerClassPair keyPair = new POxOSerializerClassPair();
                                    keyPair.setGenericClass(typeof(Object));
                                    keyPair.setSerializer(serializerForClass[typeof(Object)]);
                                    POxOSerializerClassPair valuePair = new POxOSerializerClassPair();
                                    valuePair.setGenericClass(typeof(Object));
                                    valuePair.setSerializer(serializerForClass[typeof(Object)]);
                                    ret = new MapSerializer(keyPair, valuePair);
                                    serializerForClass.Add(fieldType, ret);
                                }
                            }
                        }
                    }
                    else
                    {
                        ret = serializerForClass[typeof(Object)];
                    }
                }
            }

            return ret;
        }

        public GenericClassSerializer GetFieldSerializer(FieldInfo field)
        {
            GenericClassSerializer ret = null;
            Type fieldType = field.FieldType;
            if (fieldType.IsGenericType &&
                ((fieldType.GetGenericTypeDefinition() == typeof(IList<>)) || (fieldType.GetGenericTypeDefinition() == typeof(List<>))
            || (fieldType.GetGenericTypeDefinition() == typeof(ISet<>)) || (fieldType.GetGenericTypeDefinition() == typeof(HashSet<>))
            || (fieldType.GetGenericTypeDefinition() == typeof(IDictionary<,>)) || (fieldType.GetGenericTypeDefinition() == typeof(Dictionary<,>))))
            {
                POxOSerializerClassPair pair = new POxOSerializerClassPair();
                recursiveFindSerializer(fieldType, pair);
                ret = pair.getSerializer();
            }
            else
            {
                ret = GetTypeSerializer(fieldType);
            }
            return ret;
        }


        private void recursiveFindSerializer(Type genericType, POxOSerializerClassPair pair) {
            if (!genericType.IsGenericType)
            {
                pair.setGenericClass(genericType);
                pair.setSerializer(GetTypeSerializer(genericType));
            }
            else
            {
                Type[] genericTypes = genericType.GetGenericArguments();
                POxOSerializerClassPair[] nestedPairs = new POxOSerializerClassPair[genericTypes.Length];
                for (int i = 0; i < genericTypes.Length; i++)
                {
                    Type actualType = genericTypes[i];
                    POxOSerializerClassPair nestedPair = new POxOSerializerClassPair();
                    recursiveFindSerializer(actualType, nestedPair);
                    nestedPairs[i] = nestedPair;
                }
                if ((genericType.GetGenericTypeDefinition() == typeof(IList<>)) || (genericType.GetGenericTypeDefinition() == typeof(List<>)))
                {
                    pair.setGenericClass(genericType);
                    pair.setSerializer(new ListSerializer(nestedPairs[0]));
                }
                else
                {
                    if ((genericType.GetGenericTypeDefinition() == typeof(ISet<>)) || (genericType.GetGenericTypeDefinition() == typeof(HashSet<>)))
                    {
                        pair.setGenericClass(genericType);
                        pair.setSerializer(new SetSerializer(nestedPairs[0]));
                    }
                    else
                    {
                        if ((genericType.GetGenericTypeDefinition() == typeof(IDictionary<,>)) || (genericType.GetGenericTypeDefinition() == typeof(Dictionary<,>)))
                        {
                            pair.setGenericClass(genericType);
                            pair.setSerializer(new MapSerializer(nestedPairs[0],
                                            nestedPairs[1]));
                        }
                        else
                        {
                            if ((genericType.GetGenericTypeDefinition() == typeof(Nullable<>)))
                            {
                                pair.setGenericClass(genericType);
                                pair.setSerializer(new NullableSerializer(nestedPairs[0]));
                            }
                        }
                    }
                }
            }
        }

        public Type getClassFromName(String className)
        {
            Type type = null;
            if (!classForName.ContainsKey(className))
            {
                type = LoadTypeFromName(className);
                classForName.Add(className, type);
                nameForClass.Add(type, className);
            }
            else
            {
                type = classForName[className];
            }

            return type;
        }

        public String getNameFromClass(Type type)
        {
            String name = null;
            if(type.IsGenericType)
            {
                if ((type.GetGenericTypeDefinition() == typeof(IList<>)) || (type.GetGenericTypeDefinition() == typeof(List<>)))
                {
                    name = "list";
                }
                else
                {
                    if ((type.GetGenericTypeDefinition() == typeof(ISet<>)) || (type.GetGenericTypeDefinition() == typeof(HashSet<>)))
                    {
                        name = "set";
                    }
                    else
                    {
                        if ((type.GetGenericTypeDefinition() == typeof(IDictionary<,>)) || (type.GetGenericTypeDefinition() == typeof(Dictionary<,>)))
                        {
                            name = "map";
                        }
                    }
                }
            }
            else
            {
                if (!nameForClass.ContainsKey(type))
                {
                    name = type.FullName;
                    nameForClass.Add(type, name);
                    classForName.Add(name, type);
                }
                else
                {
                    name = nameForClass[type];
                }
            }

            return name;
        }

        private Type LoadTypeFromName(String typeName)
        {
            Type ret = null;


            Assembly[] assemblies = AppDomain.CurrentDomain.GetAssemblies();
            // the trouble is that we don't know which assembly the class is defined in,
            // because we are using the "Web Site" model in Visual Studio that compiles
            // them on the fly into assemblies with random names
            // -> however, we do know that the assembly will be named App_Web_*
            // (http://msdn.microsoft.com/en-us/magazine/cc163496.aspx)
            foreach (Assembly assembly in assemblies)
            {
                Type t = assembly.GetType(typeName);
                if (t != null)
                {
                    ret = t;
                    break;
                }
            }

            if (ret == null)
                throw new ArgumentException("Unknown type to deserialize " + typeName);

            return ret;
        }
    }
}
