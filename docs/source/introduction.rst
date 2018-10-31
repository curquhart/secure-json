============
Introduction
============
SecureJSON is a JSON serializer and deserializer with a goal of keeping strings (character sequences) in memory for
the minimal amount of time.

Strings stick around in the JVM much longer than one would like when they contain sensitive information (please see
https://medium.com/@_west_on/protecting-strings-in-jvm-memory-84c365f8f01c for information about this.)

Originally, SecureJSON used the SecureString implementation referenced in the above article, but due to that it copies
any input CharSequences, I decided to build a new implementation which only creates buffers for input characters. This
is backed by a directly allocated ByteBuffer and:

    * CharSequence inputs are never copied. We keep the originals by reference, which means if the original is wiped,
      we won't be retaining information from them.
    * We allocate more memory as needed, so we're not restricted by whatever initial capacity we use. This applies
      both to our internal buffers and custom (optional) overrides.
    * The buffer(s) are cleared immediately after they have been consumed.

Having seen the results of `JSONTestSuite <https://github.com/nst/JSONTestSuite>`_, and being maybe a little bit
competitive, my second goal was to have the most thorough JSON deserializer of all of the tested libraries. SecureJSON
passes all "must pass" tests, fails all "must fail" tests, and passes all "may pass but doesn't have to" tests, which
include things like UTF16/32, extremely huge numbers, etc.
