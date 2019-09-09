# spring-cache-dynamodb

[![Apache 2.0 License][license-image]][license-url]
[![Build Status][travis-image]][travis-url]
[![Coverage Status][coveralls-image]][coveralls-url]

> Spring Cache implementation based on Amazon DynamoDB

## Install

[![](https://jitpack.io/v/bad-opensource/spring-cache-dynamodb.svg)](https://jitpack.io/#bad-opensource/spring-cache-dynamodb)

To integrate this Git repository into your project, simply follow these 2 steps (example for Maven)

**Step 1:** Add the JitPack repository to your build file
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

**Step 2:** Add the dependency
```xml
<dependency>
    <groupId>com.github.bad-opensource</groupId>
    <artifactId>spring-cache-dynamodb</artifactId>
    <version>0.9.1</version>
</dependency>
```

## Usage

### Quick start

There is an autoconfiguration class that will setup a simple key-value cache for you, provided you have specified the following properties.

#### Properties

```properties
# TTL (in seconds). Default is Duration.ZERO and disables TTL.
spring.cache.dynamo.caches[0].ttl = 10s

# Cache name for the @Cacheable annotation.
spring.cache.dynamo.caches[0].cacheName = myCache

# Value that indicates if the cache must be flushed on application start.
spring.cache.dynamo.caches[0].flushOnBoot = true
```

#### YAML

```yaml
spring:
  cache:
    dynamo:
      caches:
        - # TTL.
          ttl: 10s
          # Cache name for the @Cacheable annotation.
          cacheName: myCache
          # Value that indicates if the cache table must be flushed when the application starts.
          flushOnBoot: true
```

### Custom configuration

To customize the creation of a cache manager, create a Java Bean in a [configuration class](http://docs.spring.io/spring-boot/docs/current/reference/html/using-boot-configuration-classes.html):

```java
@Bean
public AWSCredentials amazonAWSCredentials() {
    return new BasicAWSCredentials(amazonAWSAccessKey, amazonAWSSecretKey);
}

@Bean
public AWSCredentialsProvider amazonAWSCredentialsProvider(AWSCredentials amazonAWSCredentials) {
    return new AWSStaticCredentialsProvider(amazonAWSCredentials);
}

@Bean
public AmazonDynamoDB amazonDynamoDB(AWSCredentialsProvider amazonAWSCredentialsProvider) {
    return AmazonDynamoDBClientBuilder.standard()
      .withCredentials(amazonAWSCredentialsProvider)
      .withRegion(Regions.EU_CENTRAL_1).build();
}

@Bean
public CacheManager cacheManager(AmazonDynamoDB amazonDynamoDB) {
    List<DynamoCacheBuilder> cacheBuilders = new ArrayList<>();
    cacheBuilders.add(DynamoCacheBuilder.newInstance("myCache", amazonDynamoDB)
        .withTTL(Duration.ofSeconds(600)));
      
    return new DynamoCacheManager(cacheBuilders);
}
```

#### Serializers

By default, the included `StringSerializer` is used. But it's also possible to define a custom Serializer 
of type `DynamoSerializer` for each cache. 

### How to use the cache?

#### @Cacheable

After the cache has been configured, you can use the `Cacheable` [annotation](http://docs.spring.io/spring/docs/current/spring-framework-reference/html/cache.html). 
In the following example, the cache "myCache" is used like this:

```java
@Cacheable(value = "myCache", key = "#id")
public Data getData(String id) {
  // ...
}
```

The `id` parameter is used as document identifier. 
Note that the cache key must be of type `java.lang.String`.
It is also possible to use [SpEL](https://docs.spring.io/spring/docs/current/spring-framework-reference/core.html#expressions-beandef-annotation-based) to generate a combined key.

The `Data` object will be stored in a DynamoDB table for future use (as the TTL has not expired). 
Note that cache elements must be serializable (i.e. implement `java.io.Serializable`).

## License

Spring Cache DynamoDB is Open Source software released under the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0.html).

[license-image]: https://img.shields.io/badge/license-Apache%202-blue.svg
[license-url]: http://www.apache.org/licenses/LICENSE-2.0
[travis-image]: https://travis-ci.com/bad-opensource/spring-cache-dynamodb.svg?branch=master
[travis-url]: https://travis-ci.com/bad-opensource/spring-cache-dynamodb
[coveralls-image]: https://coveralls.io/repos/github/bad-opensource/spring-cache-dynamodb/badge.svg
[coveralls-url]: https://coveralls.io/github/bad-opensource/spring-cache-dynamodb
