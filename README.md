POxOSerializer
==============

POxOSerializer is a fast and efficient serializer for POJO and POCO classes. The goal of this serializer is to allow communication between software written in java and c#. 
Using this serializer you can serialize/deserialize any class that is composed by primitive types, strings, lists and maps. 
The POxOSerializer has an easy to use API with only serialize and deserialize method.

It is not thread safe and it improve his performance along the time.


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