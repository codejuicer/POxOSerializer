using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace org.codejuicer.poxoserializer
{
    class GenericTypeContainer
    {
        private IList<String> list;

        private IDictionary<String, String> map;

        private IList<IDictionary<String, String>> listMap;

        private IList<IList<String>> listList;

        private IDictionary<String, IList<Int32?>> mapList;

        private IList<TestObjectClass> listObject;

        private ISet<TestObjectClass> setObject;

        public IList<string> List
        {
            get
            {
                return list;
            }

            set
            {
                list = value;
            }
        }

        public IDictionary<string, string> Map
        {
            get
            {
                return map;
            }

            set
            {
                map = value;
            }
        }

        public IList<IDictionary<string, string>> ListMap
        {
            get
            {
                return listMap;
            }

            set
            {
                listMap = value;
            }
        }

        public IList<IList<string>> ListList
        {
            get
            {
                return listList;
            }

            set
            {
                listList = value;
            }
        }

        public IDictionary<string, IList<int?>> MapList
        {
            get
            {
                return mapList;
            }

            set
            {
                mapList = value;
            }
        }

        public IList<TestObjectClass> ListObject
        {
            get
            {
                return listObject;
            }

            set
            {
                listObject = value;
            }
        }

        public ISet<TestObjectClass> SetObject
        {
            get
            {
                return setObject;
            }

            set
            {
                setObject = value;
            }
        }
    }
}
