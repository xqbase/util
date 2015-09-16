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

Variety of **byte[]** operations, including:
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

Copy from an [**InputStream**](http://docs.oracle.com/javase/8/docs/api/java/io/InputStream.html) to an [**OutputStream**](http://docs.oracle.com/javase/8/docs/api/java/io/OutputStream.html).

### Time

Convert date or time string to Unix time (in milliseconds), and vice versa.

### Conf

#### Easy Locating Path

```java
Conf.getAbsolutePath("conf/Test.properties");
```

This will get the absolute path of **conf/Test.properties** relative to the current folder, which is the parent folder of **classpath** (**classes/** or **lib/**) by default.

The current folder can be changed by:

```java
Conf.chdir("../src/main/webapp/WEB-INF");
```

This is very useful to debug a Maven project in Eclipse. The **classpath** in Eclipse may be **target/classes** but configurations and logs may be placed under **src/main/webapp/WEB-INF**.

#### Easy Loading Configurations

```java
Properties p = Conf.load("Test");
```

This will load the following files (if exists) successively:
- ***A.*** **/Test.properties** as resource (in **classes/** folder or jar file)
- ***B.*** **conf/Test.properties** relative to the current folder (the parent folder of **classpath** by default)
- ***C.*** **Test.properties** defined in Conf.properties

The latter overwrites the former, so project can be deployed without changing inner configurations.

Conf.properties (as resource or atop **classpath**) looks like:

```
conf_dir=/etc/test
log_dir=/var/log/test
```

Which means **/etc/test/Test.properties** (***C***) will finally be loaded and overwrite ***B*** and ***A***.

For example, we have a *development* and a *deployment* environment. In *development* we can write our DB url in **/Db.properties** as resource (***A***):

```
jdbc.url=jdbc:mysql://localhost/develop
```

This configuration will be packaged in jar or war file.

And in *deployment* we have a DB url in **/etc/test/Db.properties** (assume **conf_dir=/etc/test**) (***C***):

```
jdbc.url=jdbc:mysql://10.1.1.10/deploy
```

We can deploy our project simply by copying jar or war files, and the program will finally get the *deployment* configuration from properties ***C***.

Any default configurations can be written into properties ***A*** and will take effect without changing properties ***C***, e.g.

```
jdbc.driver=com.mysql.jdbc.NonRegisteringDriver
```

#### Easy Storing Configurations

```java
Conf.store("Test", p);
```

This will store the properties file into **conf_dir** (if defined in Conf.properties) or **conf/** relative to the current folder.

#### Easy Opening/Closing Logs

```java
Logger logger = Conf.openLogger("Test", 1048576, 10);
```

This will open a logger ([**java.util.logging.Logger**](http://docs.oracle.com/javase/8/docs/api/java/util/logging/Logger.html)) with output file under folder **log_dir** (if defined in Conf.properties) or **logs/** relative to the current folder.

```java
Conf.closeLogger(logger);
```

This will close the logger.

#### Easy Traversing Classes

```java
for (String classes : Conf.getClasses("com.xqbase.util", "com.xqbase.test")) {
	System.out.println(classes);
}
```

This will traverse all classes under given packages.

### Log

Android style logging but without tag:
- `Log.v/d/i/w/e(String message)`
- `Log.v/d/i/w/e(Throwable throwable)`
- `Log.v/d/i/w/e(String message, Throwable throwable)`

Make sure to set a [**java.util.logging.Logger**](http://docs.oracle.com/javase/8/docs/api/java/util/logging/Logger.html) before using logging, e.g.

```java
Logger originalLogger = Log.getAndSet(Conf.openLogger("Test", 1048576, 10));
```

This will open and set a new logger and get the original one.

Make sure to close current logger and restore original one before shutdown, e.g.

```java
Conf.closeLogger(Log.getAndSet(originalLogger));
```

**Log.suffix** can be set in a filter:

```java
Log.suffix.set(" [" + req.getRemoteAddr() + ", " + req.getRequestURL() + ", " +
		req.getHeader("Referer") + ", " + req.getHeader("User-Agent") + "]");
```

This will append additional information (Client-IP, URL, Referer and User-Agent) after each logging message.

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