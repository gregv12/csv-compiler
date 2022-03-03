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

import java.io.StringReader;
import java.util.ArrayList;
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
        assertGoodParse("A\nB", new StringsOnly("A"), new StringsOnly("B"));
        assertGoodParse("D\n", new StringsOnly("D"));
        assertGoodParse("\nD", new StringsOnly(), new StringsOnly("D"));
        assertGoodParseSkip("\nD", new StringsOnly.SkipEmptyLines("D"));
    }


    /**
     * Newlines with Carriage-Return (Legacy Mac)
     * A␍B                     A⏎B
     * D␍                      D
     * ␍D                      ◯⏎D
     * ␍D                      D           [skipEmptyLines]
     */
    @Test
    public void LegacyMacLfTest(){
        assertGoodParse("A\rB", new StringsOnly("A"), new StringsOnly("B"));
        assertGoodParse("D\r", new StringsOnly("D"));
        assertGoodParse("\rD", new StringsOnly(), new StringsOnly("D"));
        assertGoodParseSkip("\rD", new StringsOnly.SkipEmptyLines("D"));
    }
    /**
     * Newlines with Linefeed and Carriage-Return (Windows)
     * A␍␊B                    A⏎B
     * D␍␊                     D
     * ␍␊D                     ◯⏎D
     * ␍␊D                     D           [skipEmptyLines]
     */
    @Test
    public void windowsCrLfTest(){
        assertGoodParse("A\r\nB", new StringsOnly("A"), new StringsOnly("B"));
        assertGoodParse("D\r\n", new StringsOnly("D"));
        assertGoodParse("\r\nD", new StringsOnly(), new StringsOnly("D"));
        assertGoodParseSkip("\r\nD", new StringsOnly.SkipEmptyLines("D"));
    }

    /**
     * Quotation
     * "␣D␣"                   ␣D␣
     * "D"                     D
     * "D",D                   D↷D
     * D,"D"                   D↷D
     */
    @Test
    public void quotationTest(){
        assertGoodParse("\" D \"", new StringsOnly(" D "));
        assertGoodParse("\"D\"", new StringsOnly("D"));
        assertGoodParse("\"D\",D", new StringsOnly("D", "D"));
        assertGoodParse("D,\"D\"", new StringsOnly("D", "D"));
    }

    /**
     * Open Quotation
     * A,"B                    A↷B
     * A,B"                    A↷B"
     * "A,B                    A,B
     */
    @Test
    public void openQuotationTest(){
        assertGoodParse("A,\"B", new StringsOnly("A", "B"));
        assertGoodParse("A,B\"", new StringsOnly("A", "B\""));
        assertGoodParse("\"A,B", new StringsOnly("A,B"));
    }

    /**
     * # Escape Quotation
     * """D"                   "D
     * "D"""                   D"
     * "A""B"                  A"B
     */
    @Test
    public void escapedQuotationTest(){
        assertGoodParse("\"\"\"D\"", new StringsOnly("\"D"));
        assertGoodParse("\"D\"\"\"", new StringsOnly("D\""));
        assertGoodParse("\"A\"\"B\"", new StringsOnly("A\"B"));
    }

    /**
     * # Multiline
     * "A␊B"                   A␊B
     * "A␍B"                   A␍B
     * "A␍␊B"                  A␍␊B
     */
    @Test
    public void multilineQuotationTest(){
        assertGoodParse("\"A\nB\"", new StringsOnly("A\nB"));
        assertGoodParse("\"A\rB\"", new StringsOnly("A\rB"));
        assertGoodParse("\"A\r\nB\"", new StringsOnly("A\r\nB"));
    }

    /**
     * # Different column count
     * A␊B,C                   A⏎B↷C
     * A,B␊C                   A↷B⏎C
     */
    @Test
    public void differentColumnCountTest(){
        assertGoodParse("A\nB,C", new StringsOnly("A"), new StringsOnly("B","C"));
        assertGoodParse("A,B\nC", new StringsOnly("A", "B"), new StringsOnly("C"));
    }

    /**
     * ### NON RFC CONFORMING DATA ###
     *
     * "D"␣                    D␣
     * "A,B"␣                  A,B␣
     * ␣"D"                    ␣"D"
     * ␣"D"␣                   ␣"D"␣
     * "D"z                    Dz
     * "A,B"z                  A,Bz
     * z"D"                    z"D"
     * z"A,B"                  z"A↷B"
     * z"D"z                   z"D"z
     */

    @Test
    public void nonRfcConformingDataTest(){
        assertGoodParse("\"D\"XYZ", new StringsOnly("DXYZ"));
        assertGoodParse("\"A,B\" ", new StringsOnly("A,B "));
        assertGoodParse(" \"D\"", new StringsOnly(" \"D\""));
        assertGoodParse(" \"D\" ", new StringsOnly(" \"D\" "));
        assertGoodParse("\"D\"z", new StringsOnly("Dz"));
        assertGoodParse("\"A,B\"z", new StringsOnly("A,Bz"));
        assertGoodParse("z\"D\"", new StringsOnly("z\"D\""));
        assertGoodParse("z\"A,B\"", new StringsOnly("z\"A","B\""));
        assertGoodParse("z\"D\"z", new StringsOnly("z\"D\"z"));

    }

    static void assertGoodParse(String input, StringsOnly... expected){
        List<StringsOnly> results;
        results = RowMarshaller.load(StringsOnly.class).stream(input).collect(Collectors.toList());
        Assertions.assertIterableEquals( Arrays.asList(expected), results, "streaming contents differ for input:" + input);
        //stream with validator
        results = RowMarshaller.load(StringsOnly.class).setRowValidator((s,p) -> {}).stream(input).collect(Collectors.toList());
        Assertions.assertIterableEquals( Arrays.asList(expected), results, "streaming with validator contents differ for input:" + input);
        //for each
        results = new ArrayList<>();
        RowMarshaller.load(StringsOnly.class).forEach(results::add, new StringReader(input));
        Assertions.assertIterableEquals( Arrays.asList(expected), results, "forEach contents differ for input:" + input);
    }

    static void assertGoodParseSkip(String input, StringsOnly.SkipEmptyLines... expected){
        List<StringsOnly.SkipEmptyLines> results;
        results = RowMarshaller.load(StringsOnly.SkipEmptyLines.class).stream(input).collect(Collectors.toList());
        Assertions.assertIterableEquals( Arrays.asList(expected), results, "streaming contents differ for input:'" + input + "'");
        //stream with validator
        results = RowMarshaller.load(StringsOnly.SkipEmptyLines.class).setRowValidator((s,p) -> {}).stream(input).collect(Collectors.toList());
        Assertions.assertIterableEquals( Arrays.asList(expected), results, "streaming with validator contents differ for input:" + input);
        //for each
        results = new ArrayList<>();
        RowMarshaller.load(StringsOnly.SkipEmptyLines.class).forEach(results::add, new StringReader(input));
        Assertions.assertIterableEquals( Arrays.asList(expected), results, "firEach contents differ for input:" + input);
    }
}
