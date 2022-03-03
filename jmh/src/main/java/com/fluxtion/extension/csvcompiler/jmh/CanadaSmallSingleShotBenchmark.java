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
import com.fluxtion.extension.csvcompiler.jmh.beans.CanadaCharData;
import com.fluxtion.extension.csvcompiler.jmh.beans.CanadaData;
import com.fluxtion.extension.csvcompiler.jmh.beans.CanadaDataJavaParser;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 2, warmups = 1)
@Warmup(iterations = 1, time = 3)
@Measurement(iterations = 1, time = 3)
public class CanadaSmallSingleShotBenchmark {

    public static final String FILENAME = "src/main/data/canada_10rows.txt";

//    @Benchmark
    public void readDoublesJavaDoubleParser(Blackhole blackhole) throws FileNotFoundException {
        double maxValue = RowMarshaller.load(CanadaDataJavaParser.class)
                .stream(new BufferedReader(new FileReader(FILENAME)))
                .mapToDouble(CanadaDataJavaParser::getDoubleValue)
                .sum();
        blackhole.consume(maxValue);
    }

//    @Benchmark
    public void readDoubles(Blackhole blackhole) throws FileNotFoundException {
        double maxValue = RowMarshaller.load(CanadaData.class)
                .stream(new BufferedReader(new FileReader(FILENAME)))
                .mapToDouble(CanadaData::getDoubleValue)
                .sum();
        blackhole.consume(maxValue);
    }

//    @Benchmark
    public void readChar(Blackhole blackhole) throws FileNotFoundException {
        long count = RowMarshaller.load(CanadaCharData.class)
                .stream(new BufferedReader(new FileReader(FILENAME)))
                .count();
        blackhole.consume(count);
    }

//    @Benchmark
    public void readFileOnly(Blackhole blackhole) throws java.io.IOException {
        long count = 0;
        try (BufferedReader reader = java.nio.file.Files.newBufferedReader(java.nio.file.Path.of(FILENAME))) {
            int c;
            while ((c = reader.read()) != -1) {
                count++;
            }
        }
        blackhole.consume(count);
    }
}
