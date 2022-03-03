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
import com.fluxtion.extension.csvcompiler.jmh.beans.Person;
import org.openjdk.jmh.annotations.Benchmark;
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

@BenchmarkMode({Mode.Throughput})
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, warmups = 1)
@Warmup(iterations = 1, time = 1)
@Measurement(iterations = 1, time = 2)
public class PersonBenchmark {

    @Benchmark
    public void readDoublesColumnsWithBufferCopy(Blackhole blackhole) throws FileNotFoundException {
        double maxValue = RowMarshaller.load(Person.PersonBufferCopy.class)
                .stream(new BufferedReader(new FileReader(UtilGeneratePersonData.PERSON_10_COLUMNS_TXT)))
                .mapToDouble(Person::getAge)
                .sum();
        blackhole.consume(maxValue);
    }

    @Benchmark
    public void readDoublesColumnsNoBufferCopy(Blackhole blackhole) throws FileNotFoundException {
        double maxValue = RowMarshaller.load(Person.class)
                .stream(new BufferedReader(new FileReader(UtilGeneratePersonData.PERSON_10_COLUMNS_TXT)))
                .mapToDouble(Person::getAge)
                .sum();
        blackhole.consume(maxValue);
    }


    double maxValue;
    @Benchmark
    public void readDoublesForEacColumnsWithBufferCopy(Blackhole blackhole) throws FileNotFoundException {
        RowMarshaller.load(Person.PersonBufferCopy.class)
                .forEach(p -> maxValue += p.getAge(),
                        new BufferedReader(new FileReader(UtilGeneratePersonData.PERSON_10_COLUMNS_TXT)));
        blackhole.consume(maxValue);
    }

    @Benchmark
    public void readDoublesForEachColumnsNoBufferCopy(Blackhole blackhole) throws FileNotFoundException {
        RowMarshaller.load(Person.class)
                .forEach(p -> maxValue += p.getAge(),
                        new BufferedReader(new FileReader(UtilGeneratePersonData.PERSON_10_COLUMNS_TXT)));
        blackhole.consume(maxValue);
    }

//    @Benchmark
//    public void readDoubles10Columns(Blackhole blackhole) throws FileNotFoundException {
//
////        PersonNoBufferCsvMarshaller marshaller = new PersonNoBufferCsvMarshaller();
////        double maxValue = marshaller
////                .stream(new BufferedReader(new FileReader(FILENAME_10COLUMNS)))
////                .mapToDouble(Person::getAge)
////                .sum();
////        blackhole.consume(maxValue);
//    }
}
