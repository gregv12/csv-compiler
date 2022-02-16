/*
 * Copyright (C) 2022 V12 Technology Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Server Side Public License, version 1,
 * as published by MongoDB, Inc.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Server Side License for more details.
 *
 * You should have received a copy of the Server Side Public License
 * along with this program.  If not, see 
 * <http://www.mongodb.com/licensing/server-side-public-license>.
 */
package com.fluxtion.extension.csvcompiler.tester;

import com.fluxtion.extension.csvcompiler.RowMarshaller;
import com.fluxtion.extension.csvcompiler.annotations.ColumnMapping;
import com.fluxtion.extension.csvcompiler.annotations.CsvMarshaller;
import java.io.IOException;
import java.io.StringReader;
import lombok.ToString;

/**
 *
 * @author V12 Technology Ltd.
 */
public class ProfileMain {

    static int count = 0;

    public static void main(String[] args) throws IOException {
        RowMarshaller<Person> marshaller = RowMarshaller.load(Person.class);
        String data = "Aaron smith,13\n"
                + "Belind ostermann,21\n"
                + "Carl Dagmann,33\n";
        StringReader reader = new StringReader(data);

//        while (count< 1000) {
        while (true) {
            marshaller.forEach(ProfileMain::consume, reader);
            reader.reset();
        }
    }

    public static void consume(Person p) {
//        System.out.println(p);
        count += p.getAge();
    }

    @ToString
    @CsvMarshaller(formatSource = true, noHeader = true, newBeanPerRecord = false)
    public static class Person {

        @ColumnMapping(columnIndex = 0)
        private CharSequence name;
        @ColumnMapping(columnIndex = 1)
        private int age;

//        public Person() {
//            System.out.println("Person::constructor");
//        }

        public CharSequence getName() {
            return name;
        }

        public void setName(CharSequence name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }
}
