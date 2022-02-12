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

package com.fluxtion.extension.csvcompiler;

import com.fluxtion.extension.csvcompiler.beans.StringsOnly;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This file originates from the FastCSV (https://github.com/osiegmar/FastCSV) test suite
 * Format: INPUT EXPECTED [FLAGS]
 * Format of INPUT: SPACE = ␣ // CR = ␍ // LF = ␊
 * Format of EXPECTED: New row = ⏎ // Separated columns = ↷ // Empty field = ◯ // Empty list = ∅
 *
 */
public class PartialsTest {

    /**
     * Simple columns / Single Row
     * D                       D
     * D,D                     D↷D
     * ,D                      ◯↷D
     */
    @Test
    public void simpleColumnRow(){
        assertGoodParse("D", new StringsOnly("D"));
        assertGoodParse("D,D", new StringsOnly("D","D"));
        assertGoodParse(",D", new StringsOnly("", "D"));
    }

    /**
     * Spaces
     * ␣                       ␣
     * ␣,␣                     ␣↷␣
     * ,␣                      ◯↷␣
     * ␣D                      ␣D
     * ␣D␣,␣D␣                 ␣D␣↷␣D␣
     */
    @Test
    public void spacesTest(){
        assertGoodParse(" ", new StringsOnly(" "));
        assertGoodParse(" , ", new StringsOnly(" "," "));
        assertGoodParse(", ", new StringsOnly("", " "));
        assertGoodParse(" D", new StringsOnly(" D"));
        assertGoodParse(" D , D ", new StringsOnly(" D ", " D "));
    }


    /**
     * Trailing field separator
     * D,                      D↷◯
     * A,␊B                    A↷◯⏎B
     * ␣,                      ␣↷◯
     * ␣,␊D                    ␣↷◯⏎D
     */
    @Test
    public void fieldSeparator(){
        assertGoodParse("D,", new StringsOnly("D",""));
        assertGoodParse("A,\nB", new StringsOnly("A",""), new StringsOnly("B"));
        assertGoodParse(" ,", new StringsOnly(" ",""));
        assertGoodParse(" ,\nD", new StringsOnly(" ",""), new StringsOnly("D"));
    }

    /**
     *
     * # Newlines with Linefeed (Unix)
     * A␊B                     A⏎B
     * D␊                      D
     * ␊D                      ◯⏎D
     * ␊D                      D           [skipEmptyLines]
     *
     */
    @Test
    public void newLineFeed(){

    }

    static void assertGoodParse(String input, StringsOnly... expected){
        List<StringsOnly> results = RowMarshaller.load(StringsOnly.class).stream(input).collect(Collectors.toList());
        Assertions.assertIterableEquals( Arrays.asList(expected), results, "contents differ for input:" + input);
    }
}
