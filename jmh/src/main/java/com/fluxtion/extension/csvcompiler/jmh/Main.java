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

package com.fluxtion.extension.csvcompiler.jmh;

import com.fluxtion.extension.csvcompiler.RowMarshaller;
import com.fluxtion.extension.csvcompiler.jmh.beans.CanadaData;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class Main {

    public static final String FILENAME = "src/main/data/canada.txt";

    public static void main(String[] args) throws FileNotFoundException {
        double maxValue = RowMarshaller.load(CanadaData.class)
                .stream(new BufferedReader(new FileReader(FILENAME)))
                .mapToDouble(CanadaData::getDoubleValue)
                .peek(System.out::println)
                .sum();
    }
}
