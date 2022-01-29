package com.fluxtion.extension.csvcompiler;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

public class CsvMarshallerGeneratorAnnotationTest {

    @Test
    @SneakyThrows
//    @Disabled
    public void simpleTest(){

        Runnable runner = Util.compileInstance("com.fluxtion.extension.csvcompiler.MyRunner",
                """
                            package  com.fluxtion.extension.csvcompiler;
                            
                            import com.fluxtion.extension.csvcompiler.annotations.CsvMarshaller;
                            
                            @CsvMarshaller
                            public final class MyRunner implements Runnable{
                            
                                String name;
                                String classification;
                                int age;
                                
                                @Override
                                public void run() {
                                    System.out.println("hello world");
                                }
                                
                                public String getName(){
                                    return name;
                                }
                                
                                public void setName(String name){
                                    this.name = name;
                                }
                                
                                public int getAge(){
                                    return age;
                                }
                                
                            }
                        """);
        runner.run();
    }

}
