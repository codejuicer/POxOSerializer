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

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import org.codejuicer.poxoserializer.exception.POxOSerializerException;
import org.codejuicer.poxoserializer.io.POxOPrimitiveDecoder;
import org.codejuicer.poxoserializer.io.POxOPrimitiveEncoder;

public class ZonedDateTimeSerializer extends GenericClassSerializer {

    public ZonedDateTimeSerializer() {
        super(true);
    }

    @Override
    public void write(POxOPrimitiveEncoder encoder, Object value) throws POxOSerializerException {
        if (canBeNull) {
            if (value == null) {
                encoder.write(0x00);
                return;
            } else {
                encoder.write(0x01);
            }
        }

        ZonedDateTime valueTyped = ((ZonedDateTime)value);
        long longValue = valueTyped.toInstant().toEpochMilli();
        encoder.writeLong(longValue, true);
        String zone = valueTyped.getZone().getId();
        encoder.writeString(zone);
    }

    @Override
    public Object read(POxOPrimitiveDecoder decoder) throws POxOSerializerException {
        if (canBeNull) {
            byte isNull = decoder.readByte();
            if (isNull == 0x00) {
                return null;
            }
        }
        Instant i = Instant.ofEpochMilli(decoder.readLong(true));
        ZoneId zone = ZoneId.of(decoder.readString());
        ZonedDateTime value = ZonedDateTime.ofInstant(i, zone);
        return value;
    }
}
