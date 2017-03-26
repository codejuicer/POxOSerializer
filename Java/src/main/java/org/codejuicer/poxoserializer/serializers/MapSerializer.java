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

package org.codejuicer.poxoserializer.serializers;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.codejuicer.poxoserializer.POxOSerializerClassPair;
import org.codejuicer.poxoserializer.exception.POxOSerializerException;
import org.codejuicer.poxoserializer.io.POxOPrimitiveDecoder;
import org.codejuicer.poxoserializer.io.POxOPrimitiveEncoder;

public class MapSerializer extends GenericClassSerializer {

    private POxOSerializerClassPair keyPair;
    private POxOSerializerClassPair valuePair;

    public MapSerializer(POxOSerializerClassPair keyPair, POxOSerializerClassPair valuePair) {
        super(true);
        this.keyPair = keyPair;
        this.valuePair = valuePair;
    }

    @Override
    public Object read(POxOPrimitiveDecoder decoder) throws POxOSerializerException {
        if (canBeNull) {
            byte isNull = decoder.readByte();
            if (isNull == 0x00) {
                return null;
            }
        }

        return createAndFillMapOfType(decoder);
    }

    @Override
    public void write(POxOPrimitiveEncoder encoder, Object value) throws POxOSerializerException {
        Map<?, ?> map = (Map<?, ?>)value;
        if (canBeNull) {
            if (map == null) {
                encoder.write(0x00);
                return;
            } else {
                encoder.write(0x01);
            }
        }
        GenericClassSerializer keyNestedSerializer = keyPair.getSerializer();
        GenericClassSerializer valueNestedSerializer = valuePair.getSerializer();
        encoder.writeVarInt(map.size(), true);
        for (Entry<?, ?> entry : map.entrySet()) {
            keyNestedSerializer.write(encoder, entry.getKey());
            valueNestedSerializer.write(encoder, entry.getValue());
        }
    }

    @SuppressWarnings("unchecked")
    private <K, V> Map<K, V> createAndFillMapOfType(POxOPrimitiveDecoder decoder)
        throws POxOSerializerException {

        Map<K, V> map = new HashMap<K, V>();
        GenericClassSerializer keyNestedSerializer = keyPair.getSerializer();
        GenericClassSerializer valueNestedSerializer = valuePair.getSerializer();

        int size = decoder.readVarInt(true);

        for (int i = 0; i < size; i++) {
            K key = (K)keyNestedSerializer.read(decoder);
            V value = (V)valueNestedSerializer.read(decoder);
            map.put(key, value);
        }

        return map;
    }
}
