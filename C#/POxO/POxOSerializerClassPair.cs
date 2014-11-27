using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace POxO
{
    public class POxOSerializerClassPair
    {
        private Type genericClass = null;

        private GenericClassSerializer serializer = null;

        public GenericClassSerializer getSerializer()
        {
            return serializer;
        }

        public void setSerializer(GenericClassSerializer serializer)
        {
            this.serializer = serializer;
        }

        public Type getGenericClass()
        {
            return genericClass;
        }

        public void setGenericClass(Type genericClass)
        {
            this.genericClass = genericClass;
        }
    }
}
