/*
 *
 * Copyright 2022-2022 greg higgins
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.fluxtion.extension.csvcompiler.annotations;

import java.io.Writer;
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
     * Use the index of the column in place of a header value as the input column for the bean field
     *
     * @return the index of the column for this field, zero index convention
     */
    int columnIndex() default -1;

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
     * Flag controlling field trimming, removes whitespace at the front and the end of the input field. Setting this
     * flag overrides the default trimming behaviour for the class and inverts it
     *
     * @return flag controlling field trimming override.
     */
    boolean trimOverride() default false;

    /**
     * Flag controlling output escaping when writing a value to a row with
     * {@link com.fluxtion.extension.csvcompiler.RowMarshaller#writeRow(Object, Writer)}. If true the value will be
     * surrounded by quotes
     *
     * @return flag controlling quote escaping when writing values to rows.
     */
    boolean escapeOutput() default false;

    /**
     * Flag controlling output of field value when written with
     * {@link com.fluxtion.extension.csvcompiler.RowMarshaller#writeRow(Object, Writer)}. If true the value will be
     * written otherwise neither the value nor the output header will be written
     *
     * @return flag controlling field writing to output from a RowMarshaller
     */
    boolean outputField() default true;
}
