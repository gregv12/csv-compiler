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

import com.fluxtion.extension.csvcompiler.annotations.ColumnMapping;
import com.fluxtion.extension.csvcompiler.processor.Util;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

public class CsvMarshallerGeneratorAnnotationTest {
    @Test
    @SneakyThrows
    public void booleanCompileTest() {

        Object runner = Util.compileInstance("com.fluxtion.extension.csvcompiler.MyBooleanTest",
                "    package  com.fluxtion.extension.csvcompiler;\n" +
                        "\n" +
                        "    import com.fluxtion.extension.csvcompiler.annotations.CsvMarshaller;\n" +
                        "    import com.fluxtion.extension.csvcompiler.annotations.ColumnMapping;\n" +
                        "    import com.fluxtion.extension.csvcompiler.annotations.PostProcessMethod;\n" +
                        "\n" +
                        "    @CsvMarshaller\n" +
                        "    public final class MyBooleanTest{\n" +
                        "\n" +
                        "        @ColumnMapping(optionalField = true)\n" +
                        "        boolean name;\n" +
                        "\n" +
                        "        public boolean isName(){\n" +
                        "            return name;\n" +
                        "        }\n" +
                        "\n" +
                        "        public void setName(boolean name){\n" +
                        "            this.name = name;\n" +
                        "        }\n" +
                        "\n" +
                        "        @PostProcessMethod\n" +
                        "        public void postProcess(){\n" +
//                        "            setName(getName().toUpperCase());\n" +
                        "        }\n" +
                        "\n" +
                        "    }\n");
    }


    @Test
    @SneakyThrows
//    @Disabled
    public void simpleTest() {

        Runnable runner = Util.compileInstance("com.fluxtion.extension.csvcompiler.MyRunner",
                "    package  com.fluxtion.extension.csvcompiler;\n" +
                        "\n" +
                        "    import com.fluxtion.extension.csvcompiler.annotations.CsvMarshaller;\n" +
                        "\n" +
                        "    @CsvMarshaller\n" +
                        "    public final class MyRunner implements Runnable{\n" +
                        "\n" +
                        "        String name;\n" +
                        "        String classification;\n" +
                        "        int age;\n" +
                        "\n" +
                        "        @Override\n" +
                        "        public void run() {\n" +
                        "            //System.out.println(\"hello world\");\n" +
                        "        }\n" +
                        "\n" +
                        "        public String getName(){\n" +
                        "            return name;\n" +
                        "        }\n" +
                        "\n" +
                        "        public void setName(String name){\n" +
                        "            this.name = name;\n" +
                        "        }\n" +
                        "\n" +
                        "        public int getAge(){\n" +
                        "            return age;\n" +
                        "        }\n" +
                        "\n" +
                        "    }\n");
        runner.run();
    }

    @Test
//    @SneakyThrows
    public void nestedClassCompileTest() {

        Object runner = Util.compileInstance("com.fluxtion.extension.csvcompiler.DefaultLookupOptional",
                "package  com.fluxtion.extension.csvcompiler;\n" +
                        "import com.fluxtion.extension.csvcompiler.annotations.ColumnMapping;\n" +
                        "import com.fluxtion.extension.csvcompiler.annotations.CsvMarshaller;\n" +
                        "import com.fluxtion.extension.csvcompiler.annotations.DataMapping;\n" +
                        "\n" +
                        "@CsvMarshaller\n" +
                        "public class DefaultLookupOptional {\n" +
                        "\n" +
                        "    @DataMapping(lookupName = \"meta\")\n" +
                        "    @ColumnMapping(optionalField = true, defaultValue = \"myDefault\")//, defaultValue = \"dataFile\")\n" +
                        "    private String dataFile;\n" +
                        "\n" +
                        "    public String getDataFile() {\n" +
                        "        return dataFile;\n" +
                        "    }\n" +
                        "\n" +
                        "    public void setDataFile(String dataFile) {\n" +
                        "        this.dataFile = dataFile;\n" +
                        "    }\n" +
                        "}");

//        MYTestClass.MyNestedClass x = new MYTestClass.MyNestedClass();
    }

    public static class MYTestClass {

        @ColumnMapping(columnName = "myname", defaultValue = "WHO ARE YOU")
        int c;

        public static class MyNestedClass {
        }
    }

}
