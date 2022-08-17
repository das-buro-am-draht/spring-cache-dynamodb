# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).

# v2

## [Unreleased] 
### Changed
- check TTL for stored items, as items that have expired may still appear

## [2.0.0] - 2022-08-17
### Changed
- use AWS Java SDK v2.x [@derXear](https://github.com/derXear)
- update Spring Boot dependencies [@derXear](https://github.com/derXear)

# v1

## [0.9.6] - 2020-11-13
### Changed 
- add getter for `DynamoCacheWriter` and `DynamoSerializer` in `DynamoCache` [@derXear](https://github.com/derXear)
- update Amazon DynamoDB dependencies [@derXear](https://github.com/derXear)

## [0.9.5] - 2020-03-12
### Changed 
- the `DynamoDB::CreateTable` permission is not required anymore if the table already exist, 
instead at least `DynamoDB::DescribeTable` is necessary [@derXear](https://github.com/derXear)

## [0.9.4] - 2020-01-14
### Fixed 
- fix unit used for ttl from EpochMillis to EpochSeconds [@dnltsk](https://github.com/dnltsk)

## [0.9.3] - 2019-12-18
### Fixed 
- fix datatype used for ttl [@dnltsk](https://github.com/dnltsk)

## [0.9.2] - 2019-12-04
### Changed 
- fix javadoc and maven-gpg-plugin configuration by [@derXear](https://github.com/derXear)

## [0.9.1] - 2019-09-09
### Fixed 
- fix locking DefaultDynamoCacheWriter by [@derXear](https://github.com/derXear)

## [0.9.0] - 2019-09-06
### Added 
- initial code base by [@derXear](https://github.com/derXear)

[Unreleased]: https://github.com/bad-opensource/spring-cache-dynamodb/compare/v2.0.0...HEAD
[2.0.0]: https://github.com/bad-opensource/spring-cache-dynamodb/releases/tag/v2.0.0
[0.9.6]: https://github.com/bad-opensource/spring-cache-dynamodb/releases/tag/v0.9.6
[0.9.5]: https://github.com/bad-opensource/spring-cache-dynamodb/releases/tag/v0.9.5
[0.9.4]: https://github.com/bad-opensource/spring-cache-dynamodb/releases/tag/v0.9.4
[0.9.3]: https://github.com/bad-opensource/spring-cache-dynamodb/releases/tag/v0.9.3
[0.9.2]: https://github.com/bad-opensource/spring-cache-dynamodb/releases/tag/v0.9.2
[0.9.1]: https://github.com/bad-opensource/spring-cache-dynamodb/releases/tag/v0.9.1
[0.9.0]: https://github.com/bad-opensource/spring-cache-dynamodb/releases/tag/v0.9.0
