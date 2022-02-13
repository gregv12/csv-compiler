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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface CsvMarshaller {

    /**
     * The row containing the column identifiers for this bean marshaller. A value of less than indicates no mapping row
     *
     * @return the column to property mapping row
     */
    int mappingRow() default 1;

    /**
     * number of header lines in the file, must be equal to or less than the mappingRow
     *
     * @return number of header lines
     */
    int headerLines() default 1;

    /**
     * ignore comment lines, comment char = '#'
     *
     * @return skip comment lines
     */
    boolean skipCommentLines() default true;

    /**
     * process escape sequences, slows the parser down. Makes the parser RFC4180 compliant
     *
     * @see <a href="https://tools.ietf.org/html/rfc4180">RC4180</a>
     * @return process escape sequence flag
     */
    boolean processEscapeSequences() default false;

    /**
     * Ignore empty lines in supplied file, otherwise report a validation error
     * on empty lines.
     *
     * @return ignore empty lines
     */
    boolean skipEmptyLines() default false;

    /**
     * The field separator character, default is ','
     *
     * @return field separator
     */
    char fieldSeparator() default ',';

    /**
     * Ignore quotes
     * @return ignore quotes
     */
    boolean ignoreQuotes() default false;

    /**
     * Process and validate records that are missing fields default is false. A missing field is a field that has no
     * matching value for a column header or index. Only applies to the last fields in a row.
     *
     * @return validate a record with missing field
     */
    boolean acceptPartials() default false;

    /**
     * trim white space from start and end of the record
     *
     * @return trim white space flag
     */
    boolean trim() default false;

    /**
     * Re-use the same bean instance for each record or create a new instance on
     * every record. Default is to re-use the same instance
     *
     * @return flag new instance per record
     */
    boolean newBeanPerRecord() default true;

    /**
     * Flag to control formatting of generated source files
     *
     * @return flag to control formatting of source files
     */
    boolean formatSource() default false;

    /**
     * Exit the processing on the first error whether fatal or not, missing header is fatal, unexpected empty line is
     * a warning to the {@link com.fluxtion.extension.csvcompiler.ValidationLogger }and continue.
     * Set to true and warnings will result in throwing a {@link com.fluxtion.extension.csvcompiler.CsvProcessingException}
     * @return flag controlling early exit of parsing on warning
     */
    boolean failOnFirstError() default false;

}
