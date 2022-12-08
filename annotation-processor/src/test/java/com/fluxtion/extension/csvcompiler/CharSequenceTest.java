package com.fluxtion.extension.csvcompiler;

import com.fluxtion.extension.csvcompiler.annotations.CsvMarshaller;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

public class CharSequenceTest {

    @Test
    public void testCharSequenceField(){

        int sum = RowMarshaller.load(SimpleBean.class)
                .stream(
                        "id,number\n" +
                        "bob,30\n" +
                        "diana,200\n" +
                        "liz,10\n" +
                        "diana,300\n"
                )
                .filter(b -> b.getId().toString().equalsIgnoreCase("diana"))
                .mapToInt(SimpleBean::getNumber)
                .sum();
        MatcherAssert.assertThat(sum, CoreMatchers.is(500));
    }

    @CsvMarshaller
    public static class SimpleBean{
        private CharSequence id;
        private int number;

        public CharSequence getId() {
            return id;
        }

        public void setId(CharSequence id) {
            this.id = id;
        }

        public int getNumber() {
            return number;
        }

        public void setNumber(int number) {
            this.number = number;
        }
    }
}
