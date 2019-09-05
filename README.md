# spring-cache-dynamodb

[![Apache 2.0 License][license-image]][license-url]
[![Build Status][travis-image]][travis-url]
[![Coverage Status][coveralls-image]][coveralls-url]

> Spring Cache implementation based on Amazon DynamoDB

## Install

### Maven dependency

```xml
<dependency>
  <groupId>com.dasburo</groupId>
  <artifactId>spring-cache-dynamodb</artifactId>
  <version>1.0.0</version>
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

#### @DynamoDBLockedCacheable

This library also provides an annotation that works similar to the @Cacheable. 
However, distributed access to a Cache key from different applications or instances is mutually exclusive.

##### YAML

```yaml
spring:
  cache:
    dynamo:
      lock:
          # Delimiter used on a combined Cache key. Default: , (comma)
          delimiter:
          # Table name for distributed locking. Default: LockTable
          tableName: 
          # Default: 60 (in seconds)
          leaseDuration:
          # Default: 3 (in seconds)
          heartratePeriod:
          # Poll time to acquire lock. Default: 1 (in seconds)
          refreshPeriod: 
          # Additional time beyond leaseDuration. Defaults: 5 (in seconds)
          additionalTimeToWaitForLock: 
```
## License

Spring Data Redis is Open Source software released under the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0.html).

[license-image]: https://img.shields.io/badge/license-Apache%202-blue.svg
[license-url]: http://www.apache.org/licenses/LICENSE-2.0
[travis-image]: https://travis-ci.com/bad-opensource/spring-cache-dynamodb.svg?branch=master
[travis-url]: https://travis-ci.com/bad-opensource/spring-cache-dynamodb
[coveralls-image]: https://coveralls.io/repos/github/bad-opensource/spring-cache-dynamodb/badge.svg
[coveralls-url]: https://coveralls.io/github/bad-opensource/spring-cache-dynamodb
