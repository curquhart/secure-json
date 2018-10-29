package com.chelseaurquhart.securejson;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents a name to serialize to/from.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Serialize {
    /**
     * The target to serialize to. To allow nesting, this supports an array.
     * @return An array of Strings.
     */
    String[] name() default {};

    /**
     * The relativeTo setting. This changes the name that fields are serialized relative to their siblings.
     * @return A Relativity name.
     */
    Relativity relativeTo() default Relativity.RELATIVE;
}
