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
import com.fluxtion.extension.csvcompiler.converters.Conversion;
import com.fluxtion.extension.csvcompiler.jmh.beans.CanadaCharData;
import com.fluxtion.extension.csvcompiler.jmh.beans.CanadaData;
import com.fluxtion.extension.csvcompiler.jmh.beans.CanadaDataJavaParser;
import com.fluxtion.extension.csvcompiler.jmh.beans.CanadaDataNoReuse;
import de.siegmar.fastcsv.reader.CsvReader;
import org.openjdk.jmh.infra.Blackhole;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

//@BenchmarkMode({Mode.Throughput})
//@OutputTimeUnit(TimeUnit.SECONDS)
//@State(Scope.Benchmark)
//@Fork(value = 1, warmups = 1)
//@Warmup(iterations = 1, time = 1)
//@Measurement(iterations = 1, time = 2)
public class CanadaBenchmark {

    public static final String FILENAME = "src/main/data/canada.txt";
    public static final String FILENAME_10COLUMNS = "src/main/data/canada10Columns.txt";

//    @BenchmarkMode
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
    public void readDoubles10Columns(Blackhole blackhole) throws FileNotFoundException {
        double maxValue = RowMarshaller.load(CanadaData.class)
                .stream(new BufferedReader(new FileReader(FILENAME_10COLUMNS)))
                .mapToDouble(CanadaData::getDoubleValue)
                .sum();
        blackhole.consume(maxValue);
    }

//    @Benchmark
    public void readDoublesNoReuse(Blackhole blackhole) throws FileNotFoundException {
        double maxValue = RowMarshaller.load(CanadaDataNoReuse.class)
                .stream(new BufferedReader(new FileReader(FILENAME)))
                .mapToDouble(CanadaDataNoReuse::getDoubleValue)
                .sum();
        blackhole.consume(maxValue);
    }

//    @Benchmark
    public void readDoublesForEach(Blackhole blackhole) throws FileNotFoundException {

        class Ans{
            double maxValue = 0.0;
        }
        Ans ans = new Ans();
        RowMarshaller.load(CanadaData.class)
                .forEach(c -> ans.maxValue += c.getDoubleValue(), new BufferedReader(new FileReader(FILENAME)));
        blackhole.consume(ans.maxValue);
    }

//    @Benchmark
    public void readDoubleFastCsvParser(Blackhole blackhole) throws FileNotFoundException {
        double maxValue = CsvReader.builder().build(new BufferedReader(new FileReader(FILENAME)))
                .stream().map(r -> r.getField(0))
                .mapToDouble(Conversion::atod)
                .mapToObj(s -> {
                    CanadaData data = new CanadaData();
                    data.setDoubleValue(s);
                    return data;
                })
                .mapToDouble(CanadaData::getDoubleValue)
                .sum();
        blackhole.consume(maxValue);
    }

//    @Benchmark
    public void readDoubleFastCsvParserNoHelp(Blackhole blackhole) throws FileNotFoundException {
        double maxValue = CsvReader.builder().build(new BufferedReader(new FileReader(FILENAME)))
                .stream().map(r -> r.getField(0))
                .mapToDouble(Double::parseDouble)
                .mapToObj(s -> {
                    CanadaData data = new CanadaData();
                    data.setDoubleValue(s);
                    return data;
                })
                .mapToDouble(CanadaData::getDoubleValue)
                .sum();
        blackhole.consume(maxValue);
    }

//    @Benchmark
    public void readDoubleFastCsvParserNoHelp10Columns(Blackhole blackhole) throws FileNotFoundException {
        double maxValue = CsvReader.builder().build(new BufferedReader(new FileReader(FILENAME_10COLUMNS)))
                .stream().map(r -> r.getField(0))
                .mapToDouble(Double::parseDouble)
                .mapToObj(s -> {
                    CanadaData data = new CanadaData();
                    data.setDoubleValue(s);
                    return data;
                })
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
