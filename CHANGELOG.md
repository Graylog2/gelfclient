gelfclient Changes
==================
## 1.5.1 (2021-3-4)

* Update netty-handler to latest version `4.1.59.Final` (CVE-2020-11612) (#52) 

## 1.5.0 (2019-10-10)

* Add support for flushing message queues before shutdown. (#43)
* Update to Netty 4.1.42.Final. (#45)

## 1.4.4 (2018-11-13)

* Update to Netty 4.1.31.Final. (#40)

## 1.4.3 (2018-07-02)

* Drop support fro Java 7.
* Move number of worker threads to configuration. (#35)
* Revert support for boolean values to make sure we adhere to the GELF spec. (#31)
* Allow choice for compression algorithm in GELF UDP. (#36)
* Update to Jackson 2.8.11.
* Update to to Netty 4.1.25.Final.
* Update to TestNG 6.14.3.
* Update to Mockito 2.19.0.
* Avoid `toString()` overhead in AbstractGelfTransport. (#38)

## 1.4.2.1 (2017-06-30)

* Revert "Upgrade to TestNG 6.11" to keep Java 7 compatibility.

## 1.4.2 (2017-06-30)

* Properly support boolean values in GELF messages. (#29, #30)
* Update to Netty 4.1.12.
* Update to Jackson 2.8.9.
* Update to TestNG 6.11.
* Update to Mockito 2.8.47.
* Update to SLF4J 1.7.25.
* Relocate SLF4J packages in shaded JAR.

## 1.4.1 (2016-10-22)

* Update Maven plugins.
* Update to Netty 4.1.6.Final.
* Update to Jackson 2.8.4.
* Update to SLF4J 1.7.21.
* Update to Mockito 2.2.7.

## 1.4.0 (2016-05-30)

* Add convenience method to GelfMessageBuilder for using a millisecond timestamp. (#18)
* Limit the number of allow inflight network writeAndFlush operations. (#22)
* Add config options for `maxInflightSends` option.

## 1.3.1 (2016-01-12)

* Only append null-byte to message payload when using TCP transport. (#20)

## 1.3.0 (2015-07-08)

* Make `SO_KEEPALIVE` on TCP sockets configurable via `tcpKeepAlive()` method on `GelfConfiguration`. (#13, #15)
* Allow setting GELF level to null to remove the level field from the message. (#14)
* Make Netty and GelfSender threads daemon threads. (#8)
* Update to netty-all 4.0.29.Final.
* Update to jackson-core 2.5.4.
* Update to slf4j-api 1.7.12.
* Update to testng 6.9.4.

## 1.2.0 (2015-02-16)

* Initial TLS support for GELF TCP transport.

## 1.1.0 (2015-01-19)

* Avoid resolving the ip address early in the configuration. (See #4)
* Update to slf4j-api 1.7.10.
* Update to testng 6.8.17.
* Update to Netty 4.0.25.Final.
* Update to jackson-core 2.5.0.

## 1.0.0 (2014-08-14)

* Initial release.
