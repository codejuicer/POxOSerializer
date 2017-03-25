using System;

namespace com.thalesgroup.itms.model.messages
{
	public class TestMessage
	{
		private long? timestamp;

		private string content;

		private string idoperator;

		private string subsystem;

		private string element;

		private int? idcategory;

		protected internal TestMessage()
		{
		}

		public TestMessage(long? timestamp, string content, string idoperator, string subsystem
			, string element, int? idcategory)
			: base()
		{
			this.timestamp = timestamp;
			this.content = content;
			this.idoperator = idoperator;
			this.subsystem = subsystem;
			this.element = element;
			this.idcategory = idcategory;
		}

		public virtual long? Timestamp
		{
			get
			{
				return timestamp;
			}
		}

		public virtual string Content
		{
			get
			{
				return content;
			}
		}

		public virtual string Idoperator
		{
			get
			{
				return idoperator;
			}
		}

		public virtual string Subsystem
		{
			get
			{
				return subsystem;
			}
		}

		public virtual string Element
		{
			get
			{
				return element;
			}
		}

		public virtual int? Idcategory
		{
			get
			{
				return idcategory;
			}
		}
	}
}
