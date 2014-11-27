package com.google.devtools.poxoserializer;

import com.google.devtools.poxoserializer.serializers.GenericClassSerializer;

public class POxOSerializerClassPair {
  private Class<?> genericClass = null;
  
  private GenericClassSerializer serializer = null;

  public GenericClassSerializer getSerializer() {
    return serializer;
  }

  public void setSerializer(GenericClassSerializer serializer) {
    this.serializer = serializer;
  }

  public Class<?> getGenericClass() {
    return genericClass;
  }

  public void setGenericClass(Class<?> genericClass) {
    this.genericClass = genericClass;
  }
}
