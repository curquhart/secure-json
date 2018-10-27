package com.chelseaurquhart.securejson;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents a location to serialize to/from.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SerializeTo {
    /**
     * The target to serialize to. To allow nesting, this supports an array.
     * @return An array of Strings.
     */
    String[] value() default "";

    /**
     * The relativity setting. This changes the location that fields are serialized relative to their siblings.
     * @return A Relativity value.
     */
    Relativity relativity() default Relativity.RELATIVE;
}
