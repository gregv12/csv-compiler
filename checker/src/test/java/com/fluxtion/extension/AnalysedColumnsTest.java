package com.fluxtion.extension;

import com.fluxtion.extension.csvcompiler.TypeAnalyser.AnalysedColumn;
import com.fluxtion.extension.csvcompiler.TypeAnalyser.CandidateConverter;
import com.fluxtion.extension.csvcompiler.converters.ConstantStringConverter;
import com.fluxtion.extension.csvcompiler.converters.Conversion;
import com.fluxtion.extension.csvcompiler.converters.LocalDateConverter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.CharConversionException;

public class AnalysedColumnsTest {

    @Test
    public void constantStringCandidatePass(){
        CandidateConverter candidateConverter = CandidateConverter.builder()
                .type("String")
                .fieldConverter(new ConstantStringConverter())
                .converterConfig("TEST")
                .build();

        Assertions.assertTrue(candidateConverter.testConversion("hello"));
    }


    @Test
    public void dateCandidatePass(){
        CandidateConverter candidateConverter = CandidateConverter.builder()
                .type("Date")
                .fieldConverter(new LocalDateConverter())
                .build();

        Assertions.assertTrue(candidateConverter.testConversion("2011-12-03"));
    }

    @Test
    public void dateCandidateCustomConfigPass(){
        CandidateConverter candidateConverter = CandidateConverter.builder()
                .type("Date")
                .fieldConverter(new LocalDateConverter())
                .converterConfig("dd-MMM-yyyy")
                .build();

        Assertions.assertTrue(candidateConverter.testConversion("03-Dec-2011"));
    }

    @Test
    public void dateCandidateCustomConfigFail(){
        CandidateConverter candidateConverter = CandidateConverter.builder()
                .type("Date")
                .fieldConverter(new LocalDateConverter())
                .converterConfig("dd-MMM-yyyy")
                .build();

        Assertions.assertFalse(candidateConverter.testConversion("03-drf-2011"));
    }

    @Test
    public void primitiveCandidateSuccess(){
        CandidateConverter candidateConverter = CandidateConverter.builder()
                .type("double")
                .primitiveConverter(Conversion::atod)
                .build();

        Assertions.assertTrue(candidateConverter.testConversion("25.36"));
    }

    @Test
    public void primitiveCandidateConfig(){
        CandidateConverter candidateConverter = CandidateConverter.builder()
                .type("double")
                .primitiveConverter(Conversion::atod)
                .build();
        Assertions.assertEquals("double",candidateConverter.getType());
        Assertions.assertNotNull(candidateConverter.getConverterConfig());
        Assertions.assertNull(candidateConverter.getFieldConverter());
    }

    @Test
    public void analyseColumnForDouble(){
        AnalysedColumn analysedColumn = new AnalysedColumn("testCol");
        analysedColumn.pruneConverters("5");
        analysedColumn.pruneConverters("65");
        analysedColumn.pruneConverters("2565");
        analysedColumn.pruneConverters("25");
        analysedColumn.pruneConverters("25.36");
        Assertions.assertEquals("double", analysedColumn.bestConverter().getType());
    }

    @Test
    public void analyseColumnForLong(){
        AnalysedColumn analysedColumn = new AnalysedColumn("testCol");
        analysedColumn.pruneConverters("5");
        analysedColumn.pruneConverters("65");
        analysedColumn.pruneConverters("2565");
        analysedColumn.pruneConverters("6545644484848");
        Assertions.assertEquals("long", analysedColumn.bestConverter().getType());
    }

    @Test
    public void analyseColumnForInt(){
        AnalysedColumn analysedColumn = new AnalysedColumn("testCol");
        analysedColumn.pruneConverters("5");
        analysedColumn.pruneConverters("65");
        analysedColumn.pruneConverters("2565");
        Assertions.assertEquals("int", analysedColumn.bestConverter().getType());
    }

    @Test
    public void analyseColumnForNoMatchAsString(){
        AnalysedColumn analysedColumn = new AnalysedColumn("testCol");
        analysedColumn.pruneConverters("5");
        analysedColumn.pruneConverters("65");
        analysedColumn.pruneConverters("23.56t");
        Assertions.assertEquals("String", analysedColumn.bestConverter().getType());
    }

    @Test
    public void analyseColumnForBoolean(){
        AnalysedColumn analysedColumn = new AnalysedColumn("testCol");
        analysedColumn.pruneConverters("T");
        analysedColumn.pruneConverters("f");
        analysedColumn.pruneConverters("false");
        analysedColumn.pruneConverters("TRUE");
        Assertions.assertEquals("boolean", analysedColumn.bestConverter().getType());
    }

    @Test
    public void columnMappingForDouble(){
        AnalysedColumn analysedColumn = new AnalysedColumn("testCol");
        analysedColumn.pruneConverters("25.36");
        System.out.println(analysedColumn.asColumnMapping());
    }

    @Test
    public void testCharFromInt() throws CharConversionException {
        char cAsInt = Conversion.atoc("c");
        System.out.println("asChar:" + cAsInt);
    }


}
