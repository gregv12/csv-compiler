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

import static java.beans.Introspector.decapitalize;

/**
 * Standard conversion functions from CharSequence to primitive types.
 *
 */
public interface Conversion {

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

}
