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

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace org.codejuicer.poxoserializer
{
    public class PrimitiveClassesContainer
    {
        private sbyte bNotNull;

        public sbyte BNotNull
        {
            get { return bNotNull; }
            set { bNotNull = value; }
        }

        private char cNotNull;

        public char CNotNull
        {
            get { return cNotNull; }
            set { cNotNull = value; }
        }

        private short sNotNull;

        public short SNotNull
        {
            get { return sNotNull; }
            set { sNotNull = value; }
        }

        private long lNotNull;

        public long LNotNull
        {
            get { return lNotNull; }
            set { lNotNull = value; }
        }

        private String st;

        public String St
        {
            get { return st; }
            set { st = value; }
        }

        private String stUTF8;

        public String StUTF8
        {
            get { return stUTF8; }
            set { stUTF8 = value; }
        }

        private int iNotNull;

        public int INotNull
        {
            get { return iNotNull; }
            set { iNotNull = value; }
        }

        private bool boNotNull;

        public bool BoNotNull
        {
            get { return boNotNull; }
            set { boNotNull = value; }
        }

        private DateTime timestamp;

        public DateTime Timestamp
        {
            get { return timestamp; }
            set { timestamp = value; }
        }

        private float fNotNull;

        public float FNotNull
        {
            get { return fNotNull; }
            set { fNotNull = value; }
        }

        private double dNotNull;

        public double DNotNull
        {
            get { return dNotNull; }
            set { dNotNull = value; }
        }

        private sbyte? bCanNull;

        public sbyte? BCanNull
        {
            get { return bCanNull; }
            set { bCanNull = value; }
        }

        private char? cCanNull;

        public char? CCanNull
        {
            get { return cCanNull; }
            set { cCanNull = value; }
        }

        private short? sCanNull;

        public short? SCanNull
        {
            get { return sCanNull; }
            set { sCanNull = value; }
        }

        private long? lCanNull;

        public long? LCanNull
        {
            get { return lCanNull; }
            set { lCanNull = value; }
        }

        private int? iCanNull;

        public int? ICanNull
        {
            get { return iCanNull; }
            set { iCanNull = value; }
        }

        private Boolean? boCanNull;

        public Boolean? BoCanNull
        {
            get { return boCanNull; }
            set { boCanNull = value; }
        }

        private float? fCanNull;

        public float? FCanNull
        {
            get { return fCanNull; }
            set { fCanNull = value; }
        }

        private Double? dCanNull;

        public Double? DCanNull
        {
            get { return dCanNull; }
            set { dCanNull = value; }
        }

        private TestEnum enumValue;

        public TestEnum EnumValue
        {
            get { return enumValue; }
            set { enumValue = value; }
        }

        private IList<Int32> ints;

        public IList<Int32> Ints
        {
            get { return ints; }
            set { ints = value; }
        }

        private IList<SByte> bytes;

        public IList<SByte> Bytes
        {
            get
            {
                return bytes;
            }

            set
            {
                bytes = value;
            }
        }

        private IList<String> strings;

        public IList<String> Strings
        {
            get { return strings; }
            set { strings = value; }
        }

        private IDictionary<String, Double> map;

        public IDictionary<String, Double> Map
        {
            get { return map; }
            set { map = value; }
        }

        private IList<IDictionary<String, IList<Int32>>> nestedCollections;

        public IList<IDictionary<String, IList<Int32>>> NestedCollections
        {
            get { return nestedCollections; }
            set { nestedCollections = value; }
        }

        private List<NestedObjectClass> nestedClass;

        public List<NestedObjectClass> NestedClass
        {
            get { return nestedClass; }
            set { nestedClass = value; }
        }

        private Dictionary<String, Object> genericValueMap;

        public Dictionary<String, Object> GenericValueMap
        {
            set { genericValueMap = value; }
            get { return genericValueMap; }
        }
    }
}
