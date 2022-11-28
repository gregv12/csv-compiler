package com.fluxtion.extension.csvcompiler.processor.model;

import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.FormatterException;
import com.google.googlejavaformat.java.JavaFormatterOptions;

public interface CodeFormatter {

    static String formatJavaString(String javaSourceString) throws FormatterException {
        return new Formatter(
                JavaFormatterOptions.builder().style(JavaFormatterOptions.Style.AOSP).build()
        ).formatSource(javaSourceString);
    }
}
