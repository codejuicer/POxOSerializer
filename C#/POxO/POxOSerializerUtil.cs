using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Reflection;

namespace POxO
{
    public class POxOSerializerUtil
    {
        private Dictionary<Type, ConstructorInfo> typeConstructorMap;
        private Dictionary<String, Type> classForName;

        private Dictionary<Type, String> nameForClass;

        public POxOSerializerUtil()
        {
            classForName = new Dictionary<String, Type>();
            nameForClass = new Dictionary<Type, String>();
            typeConstructorMap = new Dictionary<Type, ConstructorInfo>();
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
            nameForClass.Add(typeof(List<>), "list");
            nameForClass.Add(typeof(Dictionary<,>), "map");

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
            classForName.Add("list", typeof(List<>));
            classForName.Add("map", typeof(Dictionary<,>));
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
                    ret = cInfo.Invoke(new object[] { });
                }
                catch
                {
                    ret = null;
                }
            }
            else
            {
                ret = typeConstructorMap[clazz];
            }


            return ret;
        }

        public GenericClassSerializer GetTypeSerializer(Type fieldType)
        {
            GenericClassSerializer ret = null;
            if (typeof(Int32).IsAssignableFrom(fieldType) || typeof(int).IsAssignableFrom(fieldType))
            {
                ret = new IntegerSerializer(fieldType);
            }
            else if (typeof(Int64).IsAssignableFrom(fieldType) || typeof(long).IsAssignableFrom(fieldType))
            {
                ret = new LongSerializer(fieldType);
            }
            else if (typeof(Int16).IsAssignableFrom(fieldType) || typeof(short).IsAssignableFrom(fieldType))
            {
                ret = new ShortSerializer(fieldType);
            }
            else if (typeof(float).IsAssignableFrom(fieldType))
            {
                ret = new FloatSerializer(fieldType);
            }
            else if (typeof(Double).IsAssignableFrom(fieldType)
              || typeof(double).IsAssignableFrom(fieldType))
            {
                ret = new DoubleSerializer(fieldType);
            }
            else if (typeof(String).IsAssignableFrom(fieldType))
            {
                ret = new StringSerializer();
            }
            else if (typeof(SByte).IsAssignableFrom(fieldType) || typeof(byte).IsAssignableFrom(fieldType))
            {
                ret = new ByteSerializer(fieldType);
            }
            else if (typeof(Char).IsAssignableFrom(fieldType)
              || typeof(char).IsAssignableFrom(fieldType))
            {
                ret = new CharSerializer(fieldType);
            }
            else if (typeof(Boolean).IsAssignableFrom(fieldType)
              || typeof(bool).IsAssignableFrom(fieldType))
            {
                ret = new BooleanSerializer(fieldType);
            }
            else if (typeof(DateTime).IsAssignableFrom(fieldType))
            {
                ret = new DateSerializer();
            }
            else if (typeof(Enum).IsAssignableFrom(fieldType))
            {
                ret = new EnumSerializer(fieldType);
            }
           
            return ret;
        }

        public GenericClassSerializer GetFieldSerializer(FieldInfo field)
        {
            GenericClassSerializer ret = null;
            Type fieldType = field.FieldType;
            if (typeof(Int32).IsAssignableFrom(fieldType) || typeof(int).IsAssignableFrom(fieldType))
            {
                ret = new IntegerSerializer(fieldType);
            }
            else if (typeof(Int64).IsAssignableFrom(fieldType) || typeof(long).IsAssignableFrom(fieldType))
            {
                ret = new LongSerializer(fieldType);
            }
            else if (typeof(Int16).IsAssignableFrom(fieldType) || typeof(short).IsAssignableFrom(fieldType))
            {
                ret = new ShortSerializer(fieldType);
            }
            else if (typeof(float).IsAssignableFrom(fieldType))
            {
                ret = new FloatSerializer(fieldType);
            }
            else if (typeof(Double).IsAssignableFrom(fieldType)
              || typeof(double).IsAssignableFrom(fieldType))
            {
                ret = new DoubleSerializer(fieldType);
            }
            else if (typeof(String).IsAssignableFrom(fieldType))
            {
                ret = new StringSerializer();
            }
            else if (typeof(SByte).IsAssignableFrom(fieldType) || typeof(byte).IsAssignableFrom(fieldType))
            {
                ret = new ByteSerializer(fieldType);
            }
            else if (typeof(Char).IsAssignableFrom(fieldType)
              || typeof(char).IsAssignableFrom(fieldType))
            {
                ret = new CharSerializer(fieldType);
            }
            else if (typeof(Boolean).IsAssignableFrom(fieldType)
              || typeof(bool).IsAssignableFrom(fieldType))
            {
                ret = new BooleanSerializer(fieldType);
            }
            else if (typeof(DateTime).IsAssignableFrom(fieldType))
            {
                ret = new DateSerializer();
            }
            else if (typeof(Enum).IsAssignableFrom(fieldType))
            {
                ret = new EnumSerializer(fieldType);
            }
            else if (fieldType.IsGenericType &&
                ((fieldType.GetGenericTypeDefinition() == typeof(IList<>)) || (fieldType.GetGenericTypeDefinition() == typeof(List<>))))
            {
                POxOSerializerClassPair pair = new POxOSerializerClassPair();
                recirsiveFindSerializer(fieldType, pair);
                ret = pair.getSerializer();
            }
            else if (fieldType.IsGenericType &&
                ((fieldType.GetGenericTypeDefinition() == typeof(IDictionary<,>)) || (fieldType.GetGenericTypeDefinition() == typeof(Dictionary<,>))))
            {
                POxOSerializerClassPair pair = new POxOSerializerClassPair();
                recirsiveFindSerializer(fieldType, pair);
                ret = pair.getSerializer();
            }
            else
            {
                ret = new ObjectSerializer();
            }
            return ret;
        }


        private void recirsiveFindSerializer(Type genericType, POxOSerializerClassPair pair) {
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
                    recirsiveFindSerializer(actualType, nestedPair);
                    nestedPairs[i] = nestedPair;
                }
                if ((genericType.GetGenericTypeDefinition() == typeof(IList<>)) || (genericType.GetGenericTypeDefinition() == typeof(List<>)))
                {
                    pair.setGenericClass(genericType);
                    pair.setSerializer(new ListSerializer(nestedPairs[0].getGenericClass(), nestedPairs[0].getSerializer()));
                }
                else
                {
                    if ((genericType.GetGenericTypeDefinition() == typeof(IDictionary<,>)) || (genericType.GetGenericTypeDefinition() == typeof(Dictionary<,>)))
                    {
                        pair.setGenericClass(genericType);
                        pair.setSerializer(new MapSerializer(nestedPairs[0].getGenericClass(),
                                        nestedPairs[1].getGenericClass(), nestedPairs[0].getSerializer(),
                                        nestedPairs[1].getSerializer()));
                    }
                }
            }
            
            //if (genericType instanceof GenericArrayType) {
          //  Type componentType = ((GenericArrayType) genericType).getGenericComponentType();
          //  if (componentType instanceof Type) {
          //    pair.setGenericClass((Type) componentType);
          //    pair.setSerializer(new ListSerializer((Type) componentType, getTypeSerializer((Type) componentType)));
          //    return;
          //  } else {
          //    POxOSerializerClassPair nestedPair = new POxOSerializerClassPair();
          //    recirsiveFindSerializer(componentType, nestedPair);
          //    pair.setGenericClass(nestedPair.getGenericClass());
          //    pair.setSerializer(new ListSerializer(nestedPair.getGenericClass(),
          //        nestedPair.getSerializer()));
          //    return;
          //  }
          //}
          //if (genericType instanceof ParameterizedType) {
          //  Type[] actualTypes = ((ParameterizedType) genericType).getActualTypeArguments();
          //  Type genericClass = (Type) ((ParameterizedType) genericType).getRawType();
          //  if (Map.class.isAssignableFrom(genericClass)) {
          //    POxOSerializerClassPair[] serializers = new POxOSerializerClassPair[actualTypes.length];
          //    for (int i = 0, n = actualTypes.length; i < n; i++) {
          //      Type actualType = actualTypes[i];

          //      if (actualType instanceof Type) {
          //        POxOSerializerClassPair nestedPair = new POxOSerializerClassPair();
          //        nestedPair.setGenericClass((Type) actualType);
          //        nestedPair.setSerializer(getTypeSerializer((Type) actualType));
          //        serializers[i] = nestedPair;
          //      } else if (actualType instanceof ParameterizedType) {
          //        POxOSerializerClassPair nestedPair = new POxOSerializerClassPair();
          //        recirsiveFindSerializer(actualType, nestedPair);
          //        serializers[i] = nestedPair;
          //      } else
          //        continue;
          //    }
          //    pair.setGenericClass((Type) ((ParameterizedType) genericType).getRawType());
          //    pair.setSerializer(new MapSerializer(serializers[0].getGenericClass(),
          //        serializers[1].getGenericClass(), serializers[0].getSerializer(),
          //        serializers[1].getSerializer()));
          //  } else if (List.class.isAssignableFrom(genericClass)) {
          //    for (int i = 0, n = actualTypes.length; i < n; i++) {
          //      Type actualType = actualTypes[i];

          //      if (actualType instanceof Type) {
          //        pair.setGenericClass((Type) ((ParameterizedType) genericType).getRawType());
          //        pair.setSerializer(new ListSerializer((Type) actualType, getTypeSerializer((Type) actualType)));
          //      } else if (actualType instanceof ParameterizedType) {
          //        POxOSerializerClassPair nestedPair = new POxOSerializerClassPair();
          //        recirsiveFindSerializer(actualType, nestedPair);
          //        pair.setGenericClass((Type) ((ParameterizedType) genericType).getRawType());
          //        pair.setSerializer(new ListSerializer(nestedPair.getGenericClass(),
          //            nestedPair.getSerializer()));
          //      } else
          //        continue;
          //    }
          //  }
          //}
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
