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
import com.fluxtion.extension.csvcompiler.jmh.beans.Person;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class UtilGeneratePersonData {

    public static final String PERSON_10_COLUMNS_TXT = "./src/main/data/person10Columns.txt";
    public static final String PERSON_COLUMNS_TXT = "./src/main/data/person.txt";
    public static final String PERSON_LONGNAME_COLUMNS_TXT = "./src/main/data/personLongName.txt";

    public static void main(String[] args) throws IOException {
        FileWriter writer = new FileWriter(PERSON_10_COLUMNS_TXT);
        FileWriter writer2 = new FileWriter(PERSON_COLUMNS_TXT);
        FileWriter writerLongName = new FileWriter(PERSON_LONGNAME_COLUMNS_TXT);
        RowMarshaller<DataWithNames> marshaller10Columns = RowMarshaller.load(DataWithNames.class);
        marshaller10Columns.writeHeaders(writer);

        RowMarshaller<Person> marshallerPerson = RowMarshaller.load(Person.class);
        marshallerPerson.writeHeaders(writer2);
        marshallerPerson.writeHeaders(writerLongName);

        Random random = new Random();
        DataWithNames data = new DataWithNames();
        Person person = new Person();
        Person personLongName = new Person();
        for (int i = 0; i < 1_000_000; i++) {
            //
            person.setAge((int) (random.nextDouble() * 20 + 2));
            person.setName("greg");
            marshallerPerson.writeRow(person, writer2);
            //
            personLongName.setAge((int) (random.nextDouble() * 20 + 2));
            personLongName.setName("dpfijepfij some very long name eofihjrf0ihr g0i eoighj 0igh 0righ e08gh e08ghi e09t8gh e0t8gh e09erigh 90erigh 0er8hg 0er8g h");
            marshallerPerson.writeRow(personLongName, writerLongName);
            //
            data.setName("g");
            data.setAge((int) (random.nextDouble() * 20 + 2));
            data.setName1("a");
            data.setName2("b");
            data.setName3("c");
            data.setName4("d");
            data.setName5("e");
            data.setName6("f");
            data.setName7("g");
            data.setName8("h");
            data.setName9("i");
            marshaller10Columns.writeRow(data, writer);
        }
        writer.close();
        writer2.close();
        writerLongName.close();
    }

}
