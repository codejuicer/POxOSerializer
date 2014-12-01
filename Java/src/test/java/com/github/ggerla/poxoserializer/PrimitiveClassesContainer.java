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

package com.github.ggerla.poxoserializer;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class PrimitiveClassesContainer {
	private byte bNotNull;

	private char cNotNull;

	private short sNotNull;

	private long lNotNull;

	private String st;

	private String stUTF8;

	private int iNotNull;

	private boolean boNotNull;

	private Date timestamp;

	private float fNotNull;

	private double dNotNull;

	private Byte bCanNull;

	private Character cCanNull;

	private Short sCanNull;

	private Long lCanNull;

	private Integer iCanNull;

	private Boolean boCanNull;

	private Float fCanNull;

	private Double dCanNull;

	private List<Integer> ints;
	
	private List<String> strings;

	private Map<String, Double> map;

	private List<Map<String, List<Integer>>> nestedCollections;
	
	private List<NestedObjectClass> nestedClass;

	public byte getbNotNull() {
		return bNotNull;
	}

	public void setbNotNull(byte bNotNull) {
		this.bNotNull = bNotNull;
	}

	public char getcNotNull() {
		return cNotNull;
	}

	public void setcNotNull(char cNotNull) {
		this.cNotNull = cNotNull;
	}

	public short getsNotNull() {
		return sNotNull;
	}

	public void setsNotNull(short sNotNull) {
		this.sNotNull = sNotNull;
	}

	public long getlNotNull() {
		return lNotNull;
	}

	public void setlNotNull(long lNotNull) {
		this.lNotNull = lNotNull;
	}

	public String getSt() {
		return st;
	}

	public void setSt(String st) {
		this.st = st;
	}

	public String getStUTF8() {
		return stUTF8;
	}

	public void setStUTF8(String stUTF8) {
		this.stUTF8 = stUTF8;
	}

	public int getiNotNull() {
		return iNotNull;
	}

	public void setiNotNull(int iNotNull) {
		this.iNotNull = iNotNull;
	}

	public boolean isBoNotNull() {
		return boNotNull;
	}

	public void setBoNotNull(boolean boNotNull) {
		this.boNotNull = boNotNull;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public float getfNotNull() {
		return fNotNull;
	}

	public void setfNotNull(float fNotNull) {
		this.fNotNull = fNotNull;
	}

	public double getdNotNull() {
		return dNotNull;
	}

	public void setdNotNull(double dNotNull) {
		this.dNotNull = dNotNull;
	}

	
	public Byte getbCanNull() {
    return bCanNull;
  }

  public void setbCanNull(Byte bCanNull) {
    this.bCanNull = bCanNull;
  }

  public Character getcCanNull() {
    return cCanNull;
  }

  public void setcCanNull(Character cCanNull) {
    this.cCanNull = cCanNull;
  }

  public Short getsCanNull() {
    return sCanNull;
  }

  public void setsCanNull(Short sCanNull) {
    this.sCanNull = sCanNull;
  }

  public Long getlCanNull() {
    return lCanNull;
  }

  public void setlCanNull(Long lCanNull) {
    this.lCanNull = lCanNull;
  }

  public Integer getiCanNull() {
    return iCanNull;
  }

  public void setiCanNull(Integer iCanNull) {
    this.iCanNull = iCanNull;
  }

  public Boolean getBoCanNull() {
    return boCanNull;
  }

  public void setBoCanNull(Boolean boCanNull) {
    this.boCanNull = boCanNull;
  }

  public Float getfCanNull() {
    return fCanNull;
  }

  public void setfCanNull(Float fCanNull) {
    this.fCanNull = fCanNull;
  }

  public Double getdCanNull() {
    return dCanNull;
  }

  public void setdCanNull(Double dCanNull) {
    this.dCanNull = dCanNull;
  }

  public List<Integer> getInts() {
		return ints;
	}

	public void setInts(List<Integer> ints) {
		this.ints = ints;
	}

	public Map<String, Double> getMap() {
		return map;
	}

	public void setMap(Map<String, Double> map) {
		this.map = map;
	}

	public List<Map<String, List<Integer>>> getNestedCollections() {
		return nestedCollections;
	}

	public void setNestedCollections(
			List<Map<String, List<Integer>>> nestedCollections) {
		this.nestedCollections = nestedCollections;
	}

  public List<String> getStrings() {
    return strings;
  }

  public void setStrings(List<String> strings) {
    this.strings = strings;
  }

public List<NestedObjectClass> getNestedClass() {
	return nestedClass;
}

public void setNestedClass(List<NestedObjectClass> nestedClass) {
	this.nestedClass = nestedClass;
}

}
