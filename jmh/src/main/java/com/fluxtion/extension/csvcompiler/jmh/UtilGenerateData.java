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
import com.fluxtion.extension.csvcompiler.annotations.CsvMarshaller;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class UtilGenerateData {

    public static void main(String[] args) throws IOException {

        FileWriter writer = new FileWriter("./src/main/data/canada10Columns.txt");
        RowMarshaller<DataWithNames> marshaller = RowMarshaller.load(DataWithNames.class);
//        marshaller.writeHeaders(writer);
        Random random = new Random();
        DataWithNames data = new DataWithNames();
        for (int i = 0; i < 100_000; i++) {
            data.setValue(random.nextDouble());
            data.setName1("sample name1");
            data.setName2("sample name2");
            data.setName3("sample name3");
            data.setName4("sample name4");
            data.setName5("sample name5");
            data.setName6("sample name6");
            data.setName7("sample name7");
            data.setName8("sample name8");
            data.setName9("sample name9");
            marshaller.writeRow(data, writer);
        }
        writer.close();
    }

    @CsvMarshaller
    public static class DataWithNames{
        private double value;
        private String name1;
        private String name2;
        private String name3;
        private String name4;
        private String name5;
        private String name6;
        private String name7;
        private String name8;
        private String name9;

        public double getValue() {
            return value;
        }

        public void setValue(double value) {
            this.value = value;
        }

        public String getName1() {
            return name1;
        }

        public void setName1(String name1) {
            this.name1 = name1;
        }

        public String getName2() {
            return name2;
        }

        public void setName2(String name2) {
            this.name2 = name2;
        }

        public String getName3() {
            return name3;
        }

        public void setName3(String name3) {
            this.name3 = name3;
        }

        public String getName4() {
            return name4;
        }

        public void setName4(String name4) {
            this.name4 = name4;
        }

        public String getName5() {
            return name5;
        }

        public void setName5(String name5) {
            this.name5 = name5;
        }

        public String getName6() {
            return name6;
        }

        public void setName6(String name6) {
            this.name6 = name6;
        }

        public String getName7() {
            return name7;
        }

        public void setName7(String name7) {
            this.name7 = name7;
        }

        public String getName8() {
            return name8;
        }

        public void setName8(String name8) {
            this.name8 = name8;
        }

        public String getName9() {
            return name9;
        }

        public void setName9(String name9) {
            this.name9 = name9;
        }
    }
}
