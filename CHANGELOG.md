gelfclient Changes
==================

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
