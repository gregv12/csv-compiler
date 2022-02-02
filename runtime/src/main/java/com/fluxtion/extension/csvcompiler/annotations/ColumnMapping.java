package com.fluxtion.extension.csvcompiler.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface ColumnMapping {

    /**
     * The name of the column used in the input source, by default tries to match an input field name with the name
     * of the target variable.
     *
     * @return the name of the column in the csv header
     */
    String columnName() default "";

    /**
     * A default value for a field, the empty string denotes no optional value is used. This value is used if no value
     * is found in the source row
     *
     * @return the String that is the default value
     */
    String defaultValue() default "";

    /**
     * Optional field, must be present if marked false. If the field is optional the default value will be used if
     * present. Causes an early failure if there is no mapping in the header row
     *
     * @return optional flag for field
     */
    boolean optionalField() default false;


    /**
     * Flag controlling field trimming, removes whitespace at the front and the end of the input field
     *
     * @return flag controlling field trimming.
     */
    boolean trim() default true;
}
