package com.fluxtion.extension.csvcompiler;

import com.fluxtion.extension.csvcompiler.annotations.CsvMarshaller;
import com.fluxtion.extension.csvcompiler.beans.ArrayBean;
import com.fluxtion.extension.csvcompiler.generated.writersRoyalty;
import lombok.Data;
import org.junit.jupiter.api.Test;

public class TabbedDataTest {

    @Test
    public void testArray(){
        StringBuilder sb = new StringBuilder();
        RowMarshaller<ArrayBean> royaltyRowMarshaller = RowMarshaller.load(ArrayBean.class);
        royaltyRowMarshaller.writeHeaders(sb);
//        System.out.println(sb);
        royaltyRowMarshaller
                .stream(
                        "data,names,scores,venues\n" +
                                "\"23|59\",greg,\"1243|3545|55\",\"eded|dfedfef|efefef df\"\n" +
                                "45,\"fred,jenny\",\"155|12\",\"dsefef\"\n"
                )
                .forEach(System.out::println);
    }

    @Test
    public void testArrayA(){
        StringBuilder sb = new StringBuilder();
        RowMarshaller<ArrayBean> royaltyRowMarshaller = RowMarshaller.load(ArrayBean.class);
        royaltyRowMarshaller.writeHeaders(sb);
        royaltyRowMarshaller
                .stream(
                        "data,names,scores\n" +
                                "\"23|5|9\",greg,34\n" +
                                "45,\"fred|jenny\",\n" +
                                ",\"fred|jenny\",\n"
                )
                .forEach(r -> royaltyRowMarshaller.writeRow(r, sb));
        System.out.println(sb);
    }

    @Test
    public void testTabbedData(){
        StringBuilder sb = new StringBuilder();
        RowMarshaller<writersRoyalty> royaltyRowMarshaller = RowMarshaller.load(writersRoyalty.class);
        royaltyRowMarshaller.writeHeaders(sb);
        royaltyRowMarshaller
                .stream(
                        "Work Primary Title\tWork Writer List\n" +
                                "A,B,C\tANDRE CIBELLI ABUJAMRA\n"
                )
                .forEach(r -> royaltyRowMarshaller.writeRow(r, sb));
        System.out.println(sb);
    }

    @CsvMarshaller(
            fieldSeparator = '\t',
            processEscapeSequences = true
    )
    @Data
    public static class TabbedPerson{
        String name;
        String thing;
        int age;
    }
}
