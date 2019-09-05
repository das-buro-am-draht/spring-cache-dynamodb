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
  <version>1.0.1</version>
</dependency>
```

### Building from source
It's not necessary to build from source, but if you want to try out the latest and greatest, 
just clone the repository
```bash
$ git clone https://github.com/bad-opensource/spring-cache-dynamodb.git
$ cd spring-cache-dynamodb
```

Spring Cache DynamoDB can be easily built with the [maven wrapper](https://github.com/takari/maven-wrapper). 
You also need JDK 1.8.
```bash
$ ./mvnw clean install
```

If you want to build with the regular mvn command, 
you will need [Maven v3.5.0](https://maven.apache.org/run-maven/index.html) or above.

## Usage

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
    DynamoCacheBuilder builder = DynamoCacheBuilder.newInstance(amazonDynamoDB, "myCache");
    builder.withTTL(Duration.ofSeconds(10));
    List<DynamoCacheBuilder> cacheBuilders = new ArrayList<>();
    cacheBuilders.add(builder);
    return new DynamoCacheManager(cacheBuilders);
}
```

### Autoconfiguration

There is also an autoconfiguration class that will setup everything for you provided you have expressed the following properties.

#### .properties

```properties
# TTL (in seconds). This property is optional and is disabled by default.
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
        -
      	  # TTL.
          ttl: 10s
          # Cache name for the @Cacheable annotation.
          cacheName: myCache
          # Value that indicates if the cache table must be flushed when the application starts.
          flushOnBoot: true
```

### How to use cache?

After the cache has been configured, you can use the `Cacheable` [annotation](http://docs.spring.io/spring/docs/current/spring-framework-reference/html/cache.html). In the following example, the cache "myCache" is used like this:

```java
@Cacheable(value = "myCache", key = "#id")
public Model getModel(String id) {
	// [...]
}
```

The `id` parameter is used as document identifier. Note that the cache key must be of type `java.lang.String`.

The `Model` object will be stored in a DynamoDB table for future use (as the TTL has not expired). Note that cache elements must be serializable (i.e. implement `java.io.Serializable`).

## License

Spring Data Redis is Open Source software released under the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0.html).

[license-image]: https://img.shields.io/badge/license-Apache%202-blue.svg
[license-url]: http://www.apache.org/licenses/LICENSE-2.0
[travis-image]: https://travis-ci.org/bad-opensource/spring-cache-dynamodb.svg?branch=master
[travis-url]: https://travis-ci.org/bad-opensource/spring-cache-dynamodb
[coveralls-image]: https://coveralls.io/repos/github/bad-opensource/spring-cache-dynamodb/badge.svg
[coveralls-url]: https://coveralls.io/github/bad-opensource/spring-cache-dynamodb
