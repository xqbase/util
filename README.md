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
- ***C.*** **Test.properties** given in Conf.properties

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

This will store the properties file into **conf_dir** (if given in Conf.properties) or **conf/** relative to the current folder.

#### Easy Opening/Closing Logs

```java
Logger logger = Conf.openLogger("Test", 1048576, 10);
```

This will open a logger ([**java.util.logging.Logger**](http://docs.oracle.com/javase/8/docs/api/java/util/logging/Logger.html)) with output file under folder **log_dir** (if given in Conf.properties) or **logs/** relative to the current folder.

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

Wrap a [**Runnable**](http://docs.oracle.com/javase/8/docs/api/java/lang/Runnable.html) in order to:
- Make the logging suffix in *branch thread* (callee thread) the same as *trunk thread* (caller thread)
- Make the logging stack trace in *branch thread* concatenating with *trunk thread*
- Count number of *branch thread*s

For example:

```java
System.setProperty("java.util.logging.SimpleFormatter.format",
		"%1$tY-%1$tm-%1$td %1$tk:%1$tM:%1$tS.%1$tL %2$s%n%4$s: %5$s%6$s%n");
Logger logger = Log.getAndSet(Conf.openLogger("Test", 1048576, 10));
Log.suffix.set(" [Test Suffix]");
Log.i("Started");
ExecutorService executor = Executors.newCachedThreadPool();
executor.execute(Runnables.wrap(() -> { // Line 15
	Time.sleep(2000);
	try {
		throw new Exception(); // Line 18
	} catch (Exception e) {
		Log.e("Exception Thrown in Branch Thread", e);
	}
}));
Time.sleep(1000);
Log.i("Number of Branch Threads: " + Runnables.getThreadNum());
Runnables.shutdown(executor);
Log.i("Stopped");
Conf.closeLogger(Log.getAndSet(logger));
```

This will output:

```
2015-09-01 00:00:00.000 com.xqbase.util.TestLog main
INFO: Started [Test Suffix]
2015-09-01 00:00:01.000 com.xqbase.util.TestLog main
INFO: Number of Branch Threads: 1 [Test Suffix]
2015-09-01 00:00:02.000 com.xqbase.util.TestLog lambda$0
SEVERE: Exception Thrown in Branch Thread [Test Suffix]
java.lang.Exception
	at com.xqbase.util.TestLog.lambda$0(TestLog.java:18)              // <-- In Branch Thread
	at com.xqbase.util.TestLog$$Lambda$1/29854731.run(Unknown Source) // <-- In Trunk Thread
	at com.xqbase.util.TestLog.main(TestLog.java:15)

2015-09-01 00:00:02.000 com.xqbase.util.TestLog main
INFO: Stopped [Test Suffix]
```

Without **Runnables.wrap()**, no suffix and stack trace of *trunk thread* can be got, and number of *branch thread*s will not be counted:

```
2015-09-01 00:00:00.000 com.xqbase.util.TestLog main
INFO: Started [Test Suffix]
2015-09-01 00:00:01.000 com.xqbase.util.TestLog main
INFO: Number of Branch Threads: 0 [Test Suffix]
2015-09-01 00:00:02.000 com.xqbase.util.TestLog lambda$0
SEVERE: Exception Thrown in Branch Thread
java.lang.Exception
	at com.xqbase.util.TestLog.lambda$0(TestLog.java:18)
	at com.xqbase.util.TestLog$$Lambda$1/29854731.run(Unknown Source)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)
	at java.lang.Thread.run(Thread.java:745)
```

### Pool

A simple and thread-safe pool implemented by deque: active objects are returned and borrowed from the head, and timeout objects are removed from the tail.

An **initializer** (to create an object), a **finalizer** (to destroy an object) and a timeout (in milliseconds) must be given for a pool.

A pooled object is controlled by its pool *entry*. When an *entry* closed, the pooled object is either returned to the pool or destroyed, e.g.

```java
try (Pool<Socket>.Entry entry = pool.borrow()) {
	Socket socket = entry.getObject();
	// use the socket
	...
	// return if socket is ok
	entry.setValid(true);
} catch (IOException e) {
	// handle exception
	...
	// destroy if exception thrown
	// entry.setValid(false) by default
}
```

Closing of a pool will destroy all inactive (returned) objects, and any active (borrowed) objects (whether valid or invalid) will be destroyed when returning.

### Lazy

A lazy factory for singletons implemented by double-checked locking.

An **initializer** (to create an instance) and a **finalizer** (to destroy the instance) must be given.

Closing of a lazy factory will destroy the instance if created.

### Service

Create a Windows or Linux service, which can be stopped gracefully.

A Java program running as a service, whether in Windows or Linux, can be written as:

```java 
private static Service service = new Service();

public static void main(String[] args) {
	if (!service.startup(args)) {
		return;
	}
	// initialize
	...
	while (!Thread.interrupted()) {
		// keep service running
		...
	}
	// close resources
	...
	service.shutdown();
}
```

A Windows service is usually made with the *service runner* of [Apache Commons Daemon](http://commons.apache.org/proper/commons-daemon/). The *service runner* will call the main method with an argument "stop" in another thread to notify shutdown, which can be caught by **service.startup()**. So the **Service** object must be a singleton.

A Linux service may receive SIGTERM (kill) and start the JVM's *shutdown hook*.

**service.startup()** will consider the following cases:
- If the argument is "stop", it may be a stop notification by the *service runner* and the *shutdown hook* will be started.
- If the main method is not called by the *service runner* (the main method is on the top of stack trace), it will add a *shutdown hook* to catch SIGTERM.
- Otherwise (may be called by the *service runner*) it will do nothing.

The *shutdown hook* will be suspended until **service.shutdown()** is called. This can prevent the main thread being killed before closing resources.

A service can be easily controlled by the following methods:
- `service.shutdownNow()` to enforce to stop the service.
- `service.isInterrupted()` to check whether the service is stopping. In the main thread, this can be replaced with `Thread.interrupted()` or `Thread.currentThread().isInterrupted()`. 
- `service.execute(Runnable)` to execute a Runnable in the thread pool (created by the service). This runnable will be interrupted when the service is stopping. 
- `service.addShutdownHook(Runnable)` to add a Runnable (NOT the JVM's *shutdown hook*) into the queue which will be run when the service is stopping.
- `service.register(AutoCloseable)` to add an AutoCloseable into the queue which will be closed when the service is stopping.

### Base64

Encode byte array to Base64 string or decode Base64 string to byte array.

Use [**java.util.Base64**](http://docs.oracle.com/javase/8/docs/api/java/util/Base64.html) for JDK 1.8.

### Database Connections

### HTTP Client Operations

### Servlet Utils

### JavaScript Server Page

### CountMap and LockMap

### Functional Interfaces