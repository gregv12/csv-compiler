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

package com.fluxtion.extension.csvcompiler.converters;

import java.util.IllegalFormatConversionException;

import java.util.function.Function;

/**
 * Standard conversion functions from CharSequence to primitive types.
 *
 */
public interface Conversion {

    String ARRAY_DELIMITER = "|";
    static double atod(CharSequence charSequence) {
        return FastDoubleParser.parseDouble(charSequence);
    }

    static long atol(CharSequence charSequence) throws NumberFormatException {
        return NumberParser.getLong(charSequence);
    }

    static boolean atobool(CharSequence charSequence){
        String test = charSequence.toString().toLowerCase();
        if(test.equals("true") | test.equals("t")){
            return true;
        } else if (test.equals("f") | test.equals("false")) {
            return false;
        }
        throw new IllegalArgumentException("boolean fields must be one of [true, false, t, f] in any case");
    }

    static int atoi(CharSequence charSequence) throws NumberFormatException {
        return NumberParser.getInteger(charSequence);
    }

    static char atoc(CharSequence charSequence) throws IllegalFormatConversionException {
        if(charSequence.length() != 1){
            throw new IllegalArgumentException("char fields must have only 1 character");
        }
        return charSequence.charAt(0);
    }
    /**
     * Converts a String to a valid java identifier, removing all invalid characters
     * @param str
     * @return
     */
    static String getIdentifier(String str) {
        return str.trim();
//        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < str.length(); i++) {
//            if ((i == 0 && Character.isJavaIdentifierStart(str.charAt(i))) || (i > 0 && Character.isJavaIdentifierPart(str.charAt(i)))) {
//                sb.append(str.charAt(i));
//            }
//        }
//        return decapitalize(sb.toString()).trim();
    }

    static Function<String, String> uncapitalizeHeader(String delimiter){
        return new HeaderUncapitalize(delimiter)::process;
    }

    static String uncapitalizeHeader(final String header, String delimieter){
        String ret = "";
        for(String s : header.split(delimieter)){
            ret += uncapitalize(s) + ",";
        }
        return ret.substring(0, ret.length()-1);
    }

    static String uncapitalize(final String str) {
        final int strLen = str == null ? 0 : str.length();
        if (strLen == 0) {
            return str;
        }

        final int firstCodepoint = str.codePointAt(0);
        final int newCodePoint = Character.toLowerCase(firstCodepoint);
        if (firstCodepoint == newCodePoint) {
            // already capitalized
            return str;
        }

        final int[] newCodePoints = new int[strLen]; // cannot be longer than the char array
        int outOffset = 0;
        newCodePoints[outOffset++] = newCodePoint; // copy the first codepoint
        for (int inOffset = Character.charCount(firstCodepoint); inOffset < strLen; ) {
            final int codepoint = str.codePointAt(inOffset);
            newCodePoints[outOffset++] = codepoint; // copy the remaining ones
            inOffset += Character.charCount(codepoint);
        }
        return new String(newCodePoints, 0, outOffset);
    }

    class HeaderUncapitalize{
        private final String delimiter;

        public HeaderUncapitalize(String delimiter) {
            this.delimiter = delimiter;
        }

        public String process(String header){
            return uncapitalizeHeader(header, delimiter);
        }
    }

}
