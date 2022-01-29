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
     * @return
     */
    int headerLines() default 1;

    /**
     * ignore comment lines, comment char = '#'
     *
     * @return skip comment lines
     */
    boolean skipCommentLines() default true;

    /**
     * process escape sequences, slows the parser down
     *
     * @return
     */
    boolean processEscapeSequences() default false;

    /**
     * Ignore empty lines in supplied file, otherwise report a validation error
     * on empty lines.
     *
     * @return ignore empty lines
     */
    boolean skipEmptyLines() default true;

    /**
     * The line ending character for the input file. default is '\n'
     *
     * @return
     */
    char lineEnding() default '\n';

    /**
     * The field separator character, default is ','
     *
     * @return
     */
    char fieldSeparator() default ',';

    /**
     * Ignore character, useful for processing windows style line endings
     * default is '\r'
     *
     * @return
     */
    char ignoredChar() default '\r';

    /**
     * Ignore quotes
     * @return ignore quotes
     */
    boolean ignoreQuotes() default false;

    /**
     * Process and validate records that are missing fields, default is false
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
     * Only allow basic latin characters in the header using:<p>
     * <code>
     *     header = header.replaceAll("\\P{InBasic_Latin}", "");
     * </code>
     *
     * @return only accept ascii in header
     */
    boolean asciiOnlyHeader() default true;

    /**
     * Re-use the same bean instance for each record or create a new instance on
     * every record. Default is to re-use the same instance
     *
     * @return flag new instance per record
     */
    boolean newBeanPerRecord() default false;

    /**
     * Flag to control formatting of generated source files
     *
     * @return flag to control formatting of source files
     */
    boolean formatSource() default false;

}
