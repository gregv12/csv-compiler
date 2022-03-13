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
import com.fluxtion.extension.csvcompiler.jmh.beans.DataWithNames;
import com.fluxtion.extension.csvcompiler.jmh.beans.NameOnly;
import com.fluxtion.extension.csvcompiler.jmh.beans.Person;
import de.siegmar.fastcsv.reader.CsvReader;
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

import static com.fluxtion.extension.csvcompiler.jmh.UtilGeneratePersonData.PERSON_10_COLUMNS_TXT;
import static com.fluxtion.extension.csvcompiler.jmh.UtilGeneratePersonData.PERSON_COLUMNS_TXT;
import static com.fluxtion.extension.csvcompiler.jmh.UtilGeneratePersonData.PERSON_LONGNAME_COLUMNS_TXT;

@BenchmarkMode({Mode.Throughput})
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, warmups = 1)
@Warmup(iterations = 1, time = 1)
@Measurement(iterations = 1, time = 2)
public class PersonBenchmark {

//    @Benchmark
//    public void readDoublesColumnsWithBufferCopy_1_Column(Blackhole blackhole) throws FileNotFoundException {
//        double maxValue = RowMarshaller.load(Person.PersonBufferCopy.class)
//                .stream(new BufferedReader(new FileReader(PERSON_COLUMNS_TXT)))
//                .mapToDouble(Person::getAge)
//                .sum();
//        blackhole.consume(maxValue);
//    }
//
//    @Benchmark
//    public void readDoublesColumnsWithBufferCopy_10_Column(Blackhole blackhole) throws FileNotFoundException {
//        double maxValue = RowMarshaller.load(Person.PersonBufferCopy.class)
//                .stream(new BufferedReader(new FileReader(PERSON_10_COLUMNS_TXT)))
//                .mapToDouble(Person::getAge)
//                .sum();
//        blackhole.consume(maxValue);
//    }

    @Benchmark
    public void readDoublesColumnsNoBufferCopy_10_ColumnALLUSED(Blackhole blackhole) throws FileNotFoundException {
        double maxValue = RowMarshaller.load(DataWithNames.class)
                .stream(new BufferedReader(new FileReader(PERSON_10_COLUMNS_TXT)))
                .mapToDouble(Person::getAge)
                .sum();
        blackhole.consume(maxValue);
    }

    @Benchmark
    public void readDoublesColumnsNoBufferCopy_10_ColumnALLUSED_LoopAssignment(Blackhole blackhole) throws FileNotFoundException {
        double maxValue = RowMarshaller.load(DataWithNames.class)
                .stream(new BufferedReader(new FileReader(PERSON_10_COLUMNS_TXT)))
                .mapToDouble(Person::getAge)
                .sum();
        blackhole.consume(maxValue);
    }

    @Benchmark
    public void readDoublesColumnsNoBufferCopy_10_Column(Blackhole blackhole) throws FileNotFoundException {
        double maxValue = RowMarshaller.load(NameOnly.class)
                .stream(new BufferedReader(new FileReader(PERSON_10_COLUMNS_TXT)))
                .mapToDouble(n -> 0.0)
                .sum();
        blackhole.consume(maxValue);
    }

    @Benchmark
    public void readStringColumnsNoBufferCopy_1_Column(Blackhole blackhole) throws FileNotFoundException {
        double maxValue = RowMarshaller.load(NameOnly.class)
                .stream(new BufferedReader(new FileReader(PERSON_COLUMNS_TXT)))
                .mapToDouble(n -> 0.0)
                .sum();
        blackhole.consume(maxValue);
    }

    @Benchmark
    public void readDoublesColumnsNoBufferCopy_1_Column_LongName(Blackhole blackhole) throws FileNotFoundException {
        double maxValue = RowMarshaller.load(Person.class)
                .stream(new BufferedReader(new FileReader(PERSON_LONGNAME_COLUMNS_TXT)))
                .mapToDouble(Person::getAge)
                .sum();
        blackhole.consume(maxValue);
    }

    @Benchmark
    public void readDoublesColumnsNoBufferCopy_1_Column(Blackhole blackhole) throws FileNotFoundException {
        double maxValue = RowMarshaller.load(Person.class)
                .stream(new BufferedReader(new FileReader(PERSON_COLUMNS_TXT)))
                .mapToDouble(Person::getAge)
                .sum();
        blackhole.consume(maxValue);
    }

    @Benchmark
    public void readDoublesColumnsNoBufferCopy_1_Column_LoopAssignment(Blackhole blackhole) throws FileNotFoundException {
        double maxValue = RowMarshaller.load(Person.PersonLoopAssignment.class)
                .stream(new BufferedReader(new FileReader(PERSON_COLUMNS_TXT)))
                .mapToDouble(Person::getAge)
                .sum();
        blackhole.consume(maxValue);
    }

    @Benchmark
    public void readDoubleFastCsvParser_1_Column(Blackhole blackhole) throws FileNotFoundException {
        double maxValue = CsvReader.builder().build(new BufferedReader(new FileReader(PERSON_COLUMNS_TXT)))
                .stream()
                .filter(row -> !row.getField(0).equalsIgnoreCase("name"))
                .map(row -> {
                    Person person = new Person();
                    person.setName(row.getField(0));
                    person.setAge(Double.parseDouble(row.getField(1)));
//                    person.setAge(Conversion.atod(row.getField(1)));
                    return person;
                })
                .mapToDouble(Person::getAge)
                .sum();
        blackhole.consume(maxValue);
    }

    @Benchmark
    public void readDoubleFastCsvParser_1_Column_LongName(Blackhole blackhole) throws FileNotFoundException {
        double maxValue = CsvReader.builder().build(new BufferedReader(new FileReader(PERSON_LONGNAME_COLUMNS_TXT)))
                .stream()
                .filter(row -> !row.getField(0).equalsIgnoreCase("name"))
                .map(row -> {
                    Person person = new Person();
                    person.setName(row.getField(0));
                    person.setAge(Double.parseDouble(row.getField(1)));
//                    person.setAge(Conversion.atod(row.getField(1)));
                    return person;
                })
                .mapToDouble(Person::getAge)
                .sum();
        blackhole.consume(maxValue);
    }

    @Benchmark
    public void readDoubleFastCsvParser_10_Column(Blackhole blackhole) throws FileNotFoundException {
        double maxValue = CsvReader.builder().build(new BufferedReader(new FileReader(PERSON_10_COLUMNS_TXT)))
                .stream()
                .filter(row -> !row.getField(0).equalsIgnoreCase("name"))
                .map(row -> {
                    Person person = new Person();
                    person.setName(row.getField(0));
//                    person.setAge(Double.parseDouble(row.getField(1)));
//                    person.setAge(Conversion.atod(row.getField(1)));
                    return person;
                })
                .mapToDouble(Person::getAge)
                .sum();
        blackhole.consume(maxValue);
    }


    @Benchmark
    public void readDoubleFastCsvParser_10_ColumnALLUSED(Blackhole blackhole) throws FileNotFoundException {
        double maxValue = CsvReader.builder().build(new BufferedReader(new FileReader(PERSON_10_COLUMNS_TXT)))
                .stream()
                .filter(row -> !row.getField(0).equalsIgnoreCase("name"))
                .map(row -> {
                    DataWithNames person = new DataWithNames();
                    person.setName(row.getField(0));
                    person.setAge(Double.parseDouble(row.getField(1)));
                    person.setName1(row.getField(2));
                    person.setName2(row.getField(3));
                    person.setName3(row.getField(4));
                    person.setName4(row.getField(5));
                    person.setName5(row.getField(6));
                    person.setName6(row.getField(7));
                    person.setName7(row.getField(8));
                    person.setName8(row.getField(9));
                    person.setName9(row.getField(10));
//                    person.setAge(Conversion.atod(row.getField(1)));
                    return person;
                })
                .mapToDouble(Person::getAge)
                .sum();
        blackhole.consume(maxValue);
    }
}
