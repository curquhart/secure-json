# Secure JSON
[![pipeline status](https://gitlab.com/curquhart/secure-json/badges/master/pipeline.svg)](https://gitlab.com/curquhart/secure-json/commits/master)
[![coverage report](https://gitlab.com/curquhart/secure-json/badges/master/coverage.svg)](https://gitlab.com/curquhart/secure-json/commits/master)

## About
Secure JSON is a JSON reader and writer that works outside of the garbage-collected heap. It can work on CharSequence
(including String) or InputStream. Serialization is to a SecureCharBuffer-backed string and is destroyed after it has
been consumed.

It is Java 7 and higher compatible (tests run against Java 7, Java 8, and Java 10).

## Usage Examples
```
import com.chelseaurquhart.securejson.SecureJSON;

// consume a string
SecureJSON.fromJSON("\"json string\"", new IConsumer<CharSequence>() {
    @Override
    public void accept(final CharSequence input) {
        // do something with input
    }
});

// consume a stream
final InputStream inputStream = ...;
SecureJSON.fromJSON(inputStream, new IConsumer<CharSequence>() {
    @Override
    public void accept(final CharSequence input) {
        // do something with input
    }
});

// provide a CharSequence
SecureJSON.toJSON("a string", new IConsumer<CharSequence>() {
    @Override
    public void accept(final CharSequence output) {
        // do something with output
    }
});

// write to a stream
final OutputStream outputStream = ...;
SecureJSON.toJSON("a string", outputStream);
```