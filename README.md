# XQBase Util
Reusable Java components for [www.xqbase.com](http://www.xqbase.com/)

## What can I do with XQBase Util?
XQBase Util provides simple and easy codes with following features:

- [Number Parsing](#numbers)
- [Byte Array and InputOutput/OutputStream Operations](#bytes)
- [Time Operations](#time)
- [Configuration Files](#conf)
- [Logging](#log)
- [Pool and Singleton](#pool)
- [Windows or Linux Service](#service)
- [Base64 (only for JDK 1.7)](#base64)
- [Database Connections](#database-connections)
- [HTTP Client Operations](#http-client-operations)
- [Servlet Utils](#servlet-utils)
- [JavaScript Server Page](#javascript-server-page)
- [CountMap and LockMap](#countmap-and-lockmap)
- [Functional Interfaces](#functional-interfaces)

## How do I use it?

You can use it as a maven dependency for JDK 1.8:

```xml
<dependency>
    <groupId>com.xqbase</groupId>
    <artifactId>xqbase-util</artifactId>
    <version>0.2.7</version>
</dependency>
```

or for JDK 1.7:

```xml
<dependency>
    <groupId>com.xqbase</groupId>
    <artifactId>xqbase-util-jdk17</artifactId>
    <version>0.2.7</version>
</dependency>
```

## Useful Components

### Numbers
Help to parse numbers (int, long, float and double) and prevent unchecked [**NumberFormatException**](http://docs.oracle.com/javase/8/docs/api/java/lang/NumberFormatException.html).

Class **Numbers** has 4 types of methods:
- `Numbers.parseXxx(String s)` to parse a number with *default value* of **0**
- `Numbers.parseXxx(String s, int i)` to parse a number with a given *default value* **i**
- `Numbers.parseXxx(String s, int min, int max)` to parse a number with a range between **min** and **max** (inclusive), only for **int** so far
- `Numbers.parseXxx(String s, int i, int min, int max)` to parse a number with a given *default value* **i** and a range between **min** and **max** (inclusive), only for **int** so far

All these methods will return *default value* if null or not parsable.

### Bytes
Various **byte[]** operations, including:
- Encoding/Decoding to/from hexadecimal
- Storing/Retrieving **short**, **int** or **long** to/from **byte[]**
- Concatenating/Truncating/Comparing of **byte[]**s
- Generating random **byte[]**
- Dumping for debug

### ByteArrayQueue
A queue of bytes like [**ByteBuffer**](http://docs.oracle.com/javase/8/docs/api/java/nio/ByteBuffer.html) but easy to add (into the tail) or retrieve (from the head).

This queue can be used as both [**ByteArrayInputStream**](http://docs.oracle.com/javase/8/docs/api/java/io/ByteArrayInputStream.html) and [**ByteArrayOutputStream**](http://docs.oracle.com/javase/8/docs/api/java/io/ByteArrayOutputStream.html), and can be easily duplicated, e.g.

```java
ByteArrayQueue baq = new ByteArrayQueue();
// Read from a file
try (FileInputStream file = new FileInputStream(...)) {
	Streams.copy(file, baq.getOutputStream());
}
// Analyze this ByteArrayQueue
Bytes.dump(System.out, baq.array(), baq.offset(), baq.length());
...
// Write to a file and do not damage the original ByteArrayQueue
try (FileOutputStream file1 = new FileOutputStream(...)) {
	Streams.copy(baq.clone().getInputStream(), file1);
}
// Continue to analyze this ByteArrayQueue
...
// Write to another file
try (FileOutputStream file2 = new FileOutputStream(...)) {
	Streams.copy(baq.getInputStream(), file2);
}
```

### Streams

### Time

### Conf

### Log

### Runnables

### Pool

### Lazy

### Service

### Base64
Encode Byte Array to Base64 String or Decode Base64 String to Byte Array.

Use [**java.util.Base64**](http://docs.oracle.com/javase/8/docs/api/java/util/Base64.html) for JDK 1.8.

### Database Connections

### HTTP Client Operations

### Servlet Utils

### JavaScript Server Page

### CountMap and LockMap

### Functional Interfaces