/*
 Copyright (c) 2015, Laurent Bourges. All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 - Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.

 - Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.fluxtion.extension.csvcompiler.converters;

public final class NumberParser {

    public static int getInteger(final CharSequence csq) throws NumberFormatException {
        return getInteger(csq, 0, csq.length());
    }

    public static int getInteger(final CharSequence csq,
                                 final int offset, final int end) throws NumberFormatException {

        int off = offset;

        boolean sign = false;
        char ch;

        if ((end == 0)
                || (((ch = csq.charAt(off)) < '0') || (ch > '9'))
                && (!(sign = ch == '-') || (++off == end) || (((ch = csq.charAt(off)) < '0') || (ch > '9')))) {
            throw new NumberFormatException(csq.toString());
        }
        // check overflow:
        final int limit = (sign) ? (-Integer.MAX_VALUE / 10) : (-Integer.MIN_VALUE / 10); // inline

        for (int ival = 0;; ival *= 10) {
            ival += '0' - ch; // negative
            if (++off == end) {
                return sign ? ival : -ival;
            }
            if (((ch = csq.charAt(off)) < '0') || (ch > '9')) {
                throw new NumberFormatException(csq.toString());
            }
            if (ival < limit) {
                throw new NumberFormatException(csq.toString());
            }
        }
    }

    public static long getLong(final CharSequence csq) throws NumberFormatException {
        return getLong(csq, 0, csq.length());
    }

    public static long getLong(final CharSequence csq,
                               final int offset, final int end) throws NumberFormatException {

        int off = offset;

        boolean sign = false;
        char ch;

        if ((end == 0)
                || (((ch = csq.charAt(off)) < '0') || (ch > '9'))
                && (!(sign = ch == '-') || (++off == end) || (((ch = csq.charAt(off)) < '0') || (ch > '9')))) {
            throw new NumberFormatException(csq.toString());
        }
        // check overflow:
        final long limit = (sign) ? (-Long.MAX_VALUE / 10l) : (-Long.MIN_VALUE / 10l); // inline

        for (long lval = 0l;; lval *= 10l) {
            lval += '0' - ch; // negative
            if (++off == end) {
                return sign ? lval : -lval;
            }
            if (((ch = csq.charAt(off)) < '0') || (ch > '9')) {
                throw new NumberFormatException(csq.toString());
            }
            if (lval < limit) {
                throw new NumberFormatException(csq.toString());
            }
        }
    }


    private NumberParser() {
        // utility class
    }
}