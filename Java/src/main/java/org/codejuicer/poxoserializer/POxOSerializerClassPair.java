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

import org.codejuicer.poxoserializer.serializers.GenericClassSerializer;

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
