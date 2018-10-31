# Secure JSON
[![pipeline status](https://gitlab.com/curquhart/secure-json/badges/master/pipeline.svg)](https://gitlab.com/curquhart/secure-json/commits/master)
[![coverage report](https://gitlab.com/curquhart/secure-json/badges/master/coverage.svg)](https://gitlab.com/curquhart/secure-json/commits/master)
[![Documentation Status](https://readthedocs.org/projects/securejson/badge/?version=latest)](https://securejson.readthedocs.io/en/latest/?badge=latest)

## About
Secure JSON is a JSON reader and writer that works outside of the garbage-collected heap. It can work on CharSequence
(including String) or InputStream. Serialization is to a bytebuffer-backed CharSequence and is destroyed immediately
after it has been consumed.

It is Java 7 and higher compatible (tests run against Java 7, Java 8, and Java 10).

Visit [securejson.readthedocs.io](https://securejson.readthedocs.io) for full documentation.