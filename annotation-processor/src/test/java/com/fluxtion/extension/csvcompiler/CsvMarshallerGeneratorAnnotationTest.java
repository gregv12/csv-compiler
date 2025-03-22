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
//    @Disabled
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
                        "        String name2;\n" +
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
    public void fluentCapitalNames() {
        String code = "package com.fluxtion.extension.csvcompiler;\n" +
                "\n" +
                "import com.fluxtion.extension.csvcompiler.annotations.CsvMarshaller;\n" +
                "@CsvMarshaller(fluent = true, requireGetSetInSourceCode = false)\n" +
                "public class FluentBean {\n" +
                "\n" +
                "    private String name;\n" +
                "    private String AGE;\n" +
                "\n" +
                "    public String name() {\n" +
                "        return name;\n" +
                "    }\n" +
                "\n" +
                "    public void name(String name) {\n" +
                "        this.name = name;\n" +
                "    }\n" +
                "\n" +
                "    public String AGE() {\n" +
                "        return AGE;\n" +
                "    }\n" +
                "\n" +
                "    public void AGE(String AGE) {\n" +
                "        this.AGE = AGE;\n" +
                "    }\n" +
                "}";

        Object runner = Util.compileInstance(
                "com.fluxtion.extension.csvcompiler.FluentBean",
                code);
//        System.out.println(runner);
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
                        "        String assetName;\n" +
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
                        "        public String getAssetName(){\n" +
                        "            return assetName;\n" +
                        "        }\n" +
                        "\n" +
                        "        public void setAssetName(String name){\n" +
                        "            this.assetName = assetName;\n" +
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

//    @Test
    public void testAssetName(){
        Util.compileInstance("com.fluxtion.extension.csvcompiler.BeanWitSt",
                "package com.fluxtion.extension.csvcompiler;\n" +
                        "import com.fluxtion.extension.csvcompiler.annotations.CsvMarshaller;\n" +
                        "import com.fluxtion.extension.csvcompiler.beans.TestEnum;\n" +
                        "\n" +
                        "@CsvMarshaller\n" +
                        "public class BeanWitSt {\n" +
                        "    private TestEnum assetName;\n" +
                        "\n" +
                        "    public TestEnum getAssetName() {\n" +
                        "        return assetName;\n" +
                        "    }\n" +
                        "\n" +
                        "    public void setAssetName(TestEnum assetName) {\n" +
                        "        this.assetName = assetName;\n" +
                        "    }\n" +
                        "}");
    }

    public static class MYTestClass {

        @ColumnMapping(columnName = "myname", defaultValue = "WHO ARE YOU")
        int c;

        public static class MyNestedClass {
        }
    }


}
