POxOSerializer
==============

POxOSerializer is a fast and efficient serializer for POJO and POCO classes. The goal of this serializer is to allow communication between software written in java and c#. 
Using this serializer you can serialize/deserialize any class that is composed by primitive types, strings, lists and maps. 
List (IList) and Map (IDictionary) are implemented respectively ArrayList (List) and HashMap (Dictionary) in Java (C#).
The POxOSerializer has an easy to use API with only serialize and deserialize method.

It is not thread safe and it improve his performance along the time.


## Type supported

<table>
  <tr><td>Java</td><td>C#</td></tr>
  <tr><td>byte</td><td>SByte</td></tr>
  <tr><td>Byte</td><td>SByte</td></tr>
  <tr><td>char</td><td>Char</td></tr>
  <tr><td>Character</td><td>Char</td></tr>
  <tr><td>short</td><td>Int16</td></tr>
  <tr><td>Short</td><td>Int16</td></tr>
  <tr><td>int</td><td>Int32</td></tr>
  <tr><td>Integer</td><td>Int32</td></tr>
  <tr><td>long</td><td>Int64</td></tr>
  <tr><td>Long</td><td>Int64</td></tr>
  <tr><td>double</td><td>Double</td></tr>
  <tr><td>Double</td><td>Double</td></tr>
  <tr><td>float</td><td>Single(float)</td></tr>
  <tr><td>Float</td><td>Single(float)</td></tr>
  <tr><td>boolean</td><td>Boolean</td></tr>
  <tr><td>Boolean</td><td>Boolean</td></tr>
  <tr><td>String</td><td>String</td></tr>
  <tr><td>Date</td><td>DateTime</td></tr>
  <tr><td>enum</td><td>enum</td></tr>
  <tr><td>List</td><td>IList</td></tr>
  <tr><td>Map</td><td>IDictionary</td></tr>
</table>


## Installation

POxOSerializer plugin are available on the [releases page](https://github.com/ggerla/poxoserializer/releases). 

### Java Integration with Maven

To use the official release of POxOSerializer, please use the following snippet in your pom.xml

```xml
    <dependency>
		<groupId>com.google.devtools</groupId>
		<artifactId>poxo-serializer</artifactId>
		<version>1.0.0</version>
	</dependency>
```