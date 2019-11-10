gson-deep-merge
===============

`gson-deep-merge` is a library that allows deep merging of objects with Gson.

Usage
-----

Include this in your POM:

```xml
<dependency>
  <groupId>io.brymck</groupId>
  <artifactId>gson-deep-merge</artifactId>
  <version>0.0.1-SNAPSHOT</version>
</dependency>
```

And use it as so, given two instances `original` and `update` of class `Example`:

```java
Gson gson = new Gson();
GsonDeepMerge gsonDeepMerge = new GsonDeepMerge();

Example merged = gsonDeepMerge.deepMerge(gson, original, update, Example.class);
```

One obvious use case for tools like Kotlin is to embed this in an extension function.
For instance, given the above `Example` class:

```kotlin
// Extension implementation
private val gson = Gson()
private val gsonDeepMerge = GsonDeepMerge()

fun Example.deepMerge(other: Example): Example =
  gsonDeepMerge.deepMerge(gson, this, other, this::class.java)

// Usage
val foo = Example(foo = Foo())
val bar = Example(bar = Bar())
val fooBar = foo.deepMerge(bar);
```
