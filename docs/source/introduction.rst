============
Introduction
============
SecureJSON is a JSON serializer and deserializer with a goal of keeping strings (character sequences) in memory for
the minimal amount of time.

Strings stick around in the JVM much longer than one would like when they contain sensitive information (please see
https://medium.com/@_west_on/protecting-strings-in-jvm-memory-84c365f8f01c for information about this.)

.. DANGER:: Please note that this is not a substitution for secure coding practices or maintaining a secure environment.
   If an attacker has access to your JVM’s memory, there isn’t really anything you can do to guarantee that they can’t
   see sensitive data, but the fleeting nature of data managed in this manner helps ensure that sensitive information is
   not kept in memory any longer than is necessary and as such helps to mitigate the risks.

SecureJSON uses something similar to what SecureString does (direct byte buffers), but unlike SecureString, it does not
copy any input CharSequences but rather just maintains all of the original references until it is closed.

Having seen the results of `JSONTestSuite <https://github.com/nst/JSONTestSuite>`_, and being maybe a little bit
competitive, my second goal was to have the most thorough JSON deserializer of all of the tested libraries. SecureJSON
passes all "must pass" tests, fails all "must fail" tests, and passes all "may pass but doesn't have to" tests, which
include things like UTF16/32, extremely huge numbers, etc.
