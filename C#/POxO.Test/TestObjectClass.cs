using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace org.codejuicer.poxoserializer
{
    public class TestObjectClass
    {
        private String campo1;

        private String campo2;
        private String campo3;
        private String campo4;
        private String campo5;
        private String campo6;
        private String campo7;
        private String campo8;

        public string Campo1
        {
            get
            {
                return campo1;
            }
        }

        public string Campo2
        {
            get
            {
                return campo2;
            }
        }

        public string Campo3
        {
            get
            {
                return campo3;
            }
        }

        public string Campo4
        {
            get
            {
                return campo4;
            }
        }

        public string Campo5
        {
            get
            {
                return campo5;
            }
        }

        public string Campo6
        {
            get
            {
                return campo6;
            }
        }

        public string Campo7
        {
            get
            {
                return campo7;
            }
        }

        public string Campo8
        {
            get
            {
                return campo8;
            }
        }

        protected TestObjectClass()
        {

        }

        public TestObjectClass(String campo1, String campo2, String campo3, String campo4, String campo5,
                               String campo6, String campo7, String campo8)
        {
            this.campo1 = campo1;
            this.campo2 = campo2;
            this.campo3 = campo3;
            this.campo4 = campo4;
            this.campo5 = campo5;
            this.campo6 = campo6;
            this.campo7 = campo7;
            this.campo8 = campo8;
        }
    }
}
