# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

### Changed

- compatibility updates for SonarQube 25.5.0
- upgrade libraries versions
- correction of technical problem with Integration tests (because of Maven format in technical answer to "sonar-orchestrator-junit5" library)
- upgrade JDK from 11 to 17
- [#4](https://github.com/green-code-initiative/creedengo-java/issues/4) Improvement: "++i" statement is not so bad
- [#105](https://github.com/green-code-initiative/creedengo-java/pull/105) fix GCI82 rule to handle record types and adjust test cases

### Deleted

## [2.1.1] - 2025-03-13

### Changed

- compatibility updates for SonarQube 25.1.0, 25.2.0 and 25.3.0 compatibility
- upgrade creedengo-rules-specifications lib to 2.2.2

## [2.1.0] - 2025-01-07

### Added

- [#88](https://github.com/green-code-initiative/creedengo-java/pull/88) Add new Java rule GCI94 - Use orElseGet instead of orElse
- [#89](https://github.com/green-code-initiative/creedengo-java/pull/89) Add new Java rule GCI82 - Make non reassigned variables constants

### Changed

- upgrade some libraries versions
- improve Integration Tests system to be more flexible (add new IT for each rule)
- [#21](https://github.com/green-code-initiative/creedengo-java/issues/21) Improvement: some method calls are legitimate in a for loop expression
- check compatibility with SonarQube 10.7.0 and 24.12.0
- upgrade actions/upload-artifact and actions/download-artifact from v3 to v4)

## [2.0.0] - 2024-12-18

### Added

- [#59](https://github.com/green-code-initiative/creedengo-java/pull/59) Add builtin profile `ecoCode way` to aggregate all implemented ecoCode rules by this plugin
- [#53](https://github.com/green-code-initiative/creedengo-java/issues/53) Improve integration tests
- Rename rules ECXXX to the new Green Code Initiative naming convention GCIXXX
- migration from ecocode to creedengo - all over the code

### Changed

- [#49](https://github.com/green-code-initiative/creedengo-java/pull/49) Add test to ensure all Rules are registered
- [#336](https://github.com/green-code-initiative/creedengo-rules-specifications/issues/336) [Adds Maven Wrapper](https://github.com/green-code-initiative/creedengo-java/pull/67)

## [1.6.2] - 2024-07-21

### Changed

- [#60](https://github.com/green-code-initiative/creedengo-java/issues/60) Check + update for SonarQube 10.6.0 compatibility
- refactoring docker system
- upgrade ecocode-rules-specifications to 1.6.2

## [1.6.1] - 2024-05-15

### Changed

- [#15](https://github.com/green-code-initiative/creedengo-java/issues/15) correction NullPointer in EC2 rule
- check Sonarqube 10.5.1 compatibility + update docker files and README.md

## [1.6.0] - 2024-02-02

### Added

- [#12](https://github.com/green-code-initiative/creedengo-java/issues/12) Add support for SonarQube 10.4 "DownloadOnlyWhenRequired" feature

### Deleted

- [#6](https://github.com/green-code-initiative/creedengo-java/pull/6) Delete deprecated java rules EC4, EC53, EC63 and EC75

## [1.5.2] - 2024-01-23

### Changed

- [#9](https://github.com/green-code-initiative/creedengo-java/issues/9) EC2 rule : correction no block statement use case

## [1.5.1] - 2024-01-23

### Changed

- [#7](https://github.com/green-code-initiative/creedengo-java/issues/7) EC2 rule : correction NullPointer with interface

## [1.5.0] - 2024-01-06

### Added

- Java rules moved from `ecoCode` repository to current repository
- Add 10.3 SonarQube compatibility

### Changed

- Update ecocode-rules-specifications to 1.4.6

[unreleased](https://github.com/green-code-initiative/creedengo-java/compare/2.1.1...HEAD)
[2.1.1](https://github.com/green-code-initiative/creedengo-java/compare/2.1.0...2.1.1)
[2.1.0](https://github.com/green-code-initiative/creedengo-java/compare/2.0.0...2.1.0)
[2.0.0](https://github.com/green-code-initiative/creedengo-java/compare/1.6.2...2.0.0)
[1.6.2](https://github.com/green-code-initiative/creedengo-java/compare/1.6.1...1.6.2)
[1.6.1](https://github.com/green-code-initiative/creedengo-java/compare/1.6.0...1.6.1)
[1.6.0](https://github.com/green-code-initiative/creedengo-java/compare/1.5.2...1.6.0)
[1.5.2](https://github.com/green-code-initiative/creedengo-java/compare/1.5.1...1.5.2)
[1.5.1](https://github.com/green-code-initiative/creedengo-java/compare/1.5.0...1.5.1)
[1.5.0](https://github.com/green-code-initiative/creedengo-java/releases/tag/1.5.0)
