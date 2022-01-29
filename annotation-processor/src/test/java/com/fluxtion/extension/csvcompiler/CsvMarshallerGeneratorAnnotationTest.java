package com.fluxtion.extension.csvcompiler;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

public class CsvMarshallerGeneratorAnnotationTest {

    @Test
    @SneakyThrows
//    @Disabled
    public void simpleTest(){

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
                "            System.out.println(\"hello world\");\n" +
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

}
