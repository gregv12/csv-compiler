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

import static java.beans.Introspector.decapitalize;

/**
 * Standard conversion functions from CharSequence to primitive types.
 *
 * @author gregp
 */
public interface Conversion {

    static double atod(CharSequence sb) {
        return sb.length() == 0 ? 0 : getDouble(sb, 0, sb.length());
    }

    static long atol(CharSequence s) throws NumberFormatException {
        if (s.length() == 0) {
            return 0;
        }
        int radix = 10;

        if (s == null) {
            throw new NumberFormatException("null");
        }

        if (radix < Character.MIN_RADIX) {
            throw new NumberFormatException("radix " + radix
                    + " less than Character.MIN_RADIX");
        }
        if (radix > Character.MAX_RADIX) {
            throw new NumberFormatException("radix " + radix
                    + " greater than Character.MAX_RADIX");
        }

        long result = 0;
        boolean negative = false;
        int i = 0, len = s.length();
        long limit = -Long.MAX_VALUE;
        long multmin;
        int digit;

        if (len > 0) {
            char firstChar = s.charAt(0);
            if (firstChar < '0') { // Possible leading "+" or "-"
                if (firstChar == '-') {
                    negative = true;
                    limit = Long.MIN_VALUE;
                } else if (firstChar != '+') {
                    throw new NumberFormatException("For input string: \"" + s + "\"");
                }

                if (len == 1) // Cannot have lone "+" or "-"
                {
                    throw new NumberFormatException("For input string: \"" + s + "\"");
                }
                i++;
            }
            multmin = limit / radix;
            while (i < len) {
                // Accumulating negatively avoids surprises near MAX_VALUE
                digit = Character.digit(s.charAt(i++), radix);
                if (digit < 0) {
                    throw new NumberFormatException("For input string: \"" + s + "\"");
                }
                if (result < multmin) {
                    throw new NumberFormatException("For input string: \"" + s + "\"");
                }
                result *= radix;
                if (result < limit + digit) {
                    throw new NumberFormatException("For input string: \"" + s + "\"");
                }
                result -= digit;
            }
        } else {
            throw new NumberFormatException("For input string: \"" + s + "\"");
        }
        return negative ? result : -result;

    }

    // Calculate the value of the specified exponent - reuse a precalculated value if possible
    static double getPow10(final int exp) {
        return Math.pow(10., exp);
    }

    static double getDouble(final CharSequence csq,
            final int offset, final int end) throws NumberFormatException {

        int off = offset;
        int len = end - offset;

        if (len == 0) {
            return Double.NaN;
        }

        char ch;
        boolean numSign = true;

        ch = csq.charAt(off);
        if (ch == '+') {
            off++;
            len--;
        } else if (ch == '-') {
            numSign = false;
            off++;
            len--;
        }

        double number;

        // Look for the special csqings NaN, Inf,
        if (len >= 3
                && ((ch = csq.charAt(off)) == 'n' || ch == 'N')
                && ((ch = csq.charAt(off + 1)) == 'a' || ch == 'A')
                && ((ch = csq.charAt(off + 2)) == 'n' || ch == 'N')) {

            number = Double.NaN;

            // Look for the longer csqing first then try the shorter.
        } else if (len >= 8
                && ((ch = csq.charAt(off)) == 'i' || ch == 'I')
                && ((ch = csq.charAt(off + 1)) == 'n' || ch == 'N')
                && ((ch = csq.charAt(off + 2)) == 'f' || ch == 'F')
                && ((ch = csq.charAt(off + 3)) == 'i' || ch == 'I')
                && ((ch = csq.charAt(off + 4)) == 'n' || ch == 'N')
                && ((ch = csq.charAt(off + 5)) == 'i' || ch == 'I')
                && ((ch = csq.charAt(off + 6)) == 't' || ch == 'T')
                && ((ch = csq.charAt(off + 7)) == 'y' || ch == 'Y')) {

            number = Double.POSITIVE_INFINITY;

        } else if (len >= 3
                && ((ch = csq.charAt(off)) == 'i' || ch == 'I')
                && ((ch = csq.charAt(off + 1)) == 'n' || ch == 'N')
                && ((ch = csq.charAt(off + 2)) == 'f' || ch == 'F')) {

            number = Double.POSITIVE_INFINITY;

        } else {

            boolean error = true;

            int startOffset = off;
            double dval;

            // TODO: check too many digits (overflow) 
            for (dval = 0d; (len > 0) && ((ch = csq.charAt(off)) >= '0') && (ch <= '9');) {
                dval *= 10d;
                dval += ch - '0';
                off++;
                len--;
            }
            int numberLength = off - startOffset;

            number = dval;

            if (numberLength > 0) {
                error = false;
            }

            // Check for fractional values after decimal
            if ((len > 0) && (csq.charAt(off) == '.')) {

                off++;
                len--;

                startOffset = off;

                // TODO: check too many digits (overflow) 
                for (dval = 0d; (len > 0) && ((ch = csq.charAt(off)) >= '0') && (ch <= '9');) {
                    dval *= 10d;
                    dval += ch - '0';
                    off++;
                    len--;
                }
                numberLength = off - startOffset;

                if (numberLength > 0) {
                    // TODO: try factorizing pow10 with exponent below: only 1 long + operation
                    number += getPow10(-numberLength) * dval;
                    error = false;
                }
            }

            if (error) {
                throw new NumberFormatException("Invalid Double : " + csq);
            }

            // Look for an exponent
            if (len > 0) {
                // note: ignore any non-digit character at end:

                if ((ch = csq.charAt(off)) == 'e' || ch == 'E') {

                    off++;
                    len--;

                    if (len > 0) {
                        boolean expSign = true;

                        ch = csq.charAt(off);
                        if (ch == '+') {
                            off++;
                            len--;
                        } else if (ch == '-') {
                            expSign = false;
                            off++;
                            len--;
                        }

                        int exponent = 0;

                        // note: ignore any non-digit character at end:
                        for (exponent = 0; (len > 0) && ((ch = csq.charAt(off)) >= '0') && (ch <= '9');) {
                            exponent *= 10;
                            exponent += ch - '0';
                            off++;
                            len--;
                        }

                        // TODO: check exponent < 1024 (overflow)
                        if (!expSign) {
                            exponent = -exponent;
                        }

                        // For very small numbers we try to miminize
                        // effects of denormalization.
                        if (exponent > -300) {
                            // TODO: cache Math.pow ?? see web page
                            number *= getPow10(exponent);
                        } else {
                            number = 1.0E-300 * (number * getPow10(exponent + 300));
                        }
                    }
                }
            }
            // check other characters:
            if (len > 0) {
                throw new NumberFormatException("Invalid Double : " + csq);
            }
        }

        return (numSign) ? number : -number;
    }

    static int indexOf(final CharSequence csq, final char c, final int off, final int end) {
        for (int i = off; i < end; i++) {
            if (csq.charAt(i) == c) {
                return i;
            }
        }
        return -1;
    }
    
    static boolean atobool(CharSequence s){
        return Boolean.valueOf(s.toString());
    }

    static int atoi(CharSequence s)
            throws NumberFormatException {
        if (s.length() == 0) {
            return 0;
        }
        int radix = 10;
        /*
         * WARNING: This method may be invoked early during VM initialization
         * before IntegerCache is initialized. Care must be taken to not use
         * the valueOf method.
         */

        if (s == null) {
            throw new NumberFormatException("null");
        }

        if (radix < Character.MIN_RADIX) {
            throw new NumberFormatException("radix " + radix
                    + " less than Character.MIN_RADIX");
        }

        if (radix > Character.MAX_RADIX) {
            throw new NumberFormatException("radix " + radix
                    + " greater than Character.MAX_RADIX");
        }

        int result = 0;
        boolean negative = false;
        int i = 0, len = s.length();
        int limit = -Integer.MAX_VALUE;
        int multmin;
        int digit;

        if (len > 0) {
            char firstChar = s.charAt(0);
            if (firstChar < '0') { // Possible leading "+" or "-"
                if (firstChar == '-') {
                    negative = true;
                    limit = Integer.MIN_VALUE;
                } else if (firstChar != '+') {
                    throw new NumberFormatException("For input string: \"" + s + "\"");
                }

                if (len == 1) // Cannot have lone "+" or "-"
                {
                    throw new NumberFormatException("For input string: \"" + s + "\"");
                }
                i++;
            }
            multmin = limit / radix;
            while (i < len) {
                // Accumulating negatively avoids surprises near MAX_VALUE
                digit = Character.digit(s.charAt(i++), radix);
                if (digit < 0) {
                    throw new NumberFormatException("For input string: \"" + s + "\"");
                }
                if (result < multmin) {
                    throw new NumberFormatException("For input string: \"" + s + "\"");
                }
                result *= radix;
                if (result < limit + digit) {
                    throw new NumberFormatException("For input string: \"" + s + "\"");
                }
                result -= digit;
            }
        } else {
            throw new NumberFormatException("For input string: \"" + s + "\"");
        }
        return negative ? result : -result;
    }

    static int atoi(CharSequence s, int defaultVal)
            throws NumberFormatException {
        if (s.length() == 0) {
            return 0;
        }
        int radix = 10;
        /*
         * WARNING: This method may be invoked early during VM initialization
         * before IntegerCache is initialized. Care must be taken to not use
         * the valueOf method.
         */

        if (s == null) {
            return defaultVal;
        }

        if (radix < Character.MIN_RADIX) {
            return defaultVal;
        }

        if (radix > Character.MAX_RADIX) {
            return defaultVal;
        }

        int result = 0;
        boolean negative = false;
        int i = 0, len = s.length();
        int limit = -Integer.MAX_VALUE;
        int multmin;
        int digit;

        if (len > 0) {
            char firstChar = s.charAt(0);
            if (firstChar < '0') { // Possible leading "+" or "-"
                if (firstChar == '-') {
                    negative = true;
                    limit = Integer.MIN_VALUE;
                } else if (firstChar != '+') {
                    return defaultVal;
                }

                if (len == 1) // Cannot have lone "+" or "-"
                {
                    return defaultVal;
                }
                i++;
            }
            multmin = limit / radix;
            while (i < len) {
                // Accumulating negatively avoids surprises near MAX_VALUE
                digit = Character.digit(s.charAt(i++), radix);
                if (digit < 0) {
                    return defaultVal;
                }
                if (result < multmin) {
                    return defaultVal;
                }
                result *= radix;
                if (result < limit + digit) {
                    return defaultVal;
                }
                result -= digit;
            }
        } else {
            return defaultVal;
        }
        return negative ? result : -result;
    }

    /**
     * Converts a String to a valid java identifier, removing all invalid characters
     * @param str
     * @return
     */
    static String getIdentifier(String str) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            if ((i == 0 && Character.isJavaIdentifierStart(str.charAt(i))) || (i > 0 && Character.isJavaIdentifierPart(str.charAt(i)))) {
                sb.append(str.charAt(i));
            }
        }
        return decapitalize(sb.toString()).trim();
    }

    class DefaultValue {

        private final String val;

        public DefaultValue(String val) {
            this.val = val;
        }

        public CharSequence defaultVal(CharSequence seq) {
            if (seq.length() < 1) {
                return val;
            }
            return seq;
        }

    }
}
