package com.fluxtion.extension.csvcompiler;

import com.fluxtion.extension.csvcompiler.annotations.CsvMarshaller;
import org.junit.jupiter.api.Test;

public class GenerationTests {

    @Test
    public void loadLoopWithPartials(){

        LoopWithPartials firstRow = RowMarshaller.load(LoopWithPartials.class)
                .stream("col1,col2,col5,col3,col4\n" +
                        "we,3,more,12.2,\n" +
                        "we,3,more\n" +
                        "")
                .findFirst().get();
    }

    @CsvMarshaller(acceptPartials = true, loopAssignmentLimit = 3)
    public static class LoopWithPartials{
        private String col1;
        private int col2;
        private double col3;
        private String col4;
        private String col5;

        public String getCol1() {
            return col1;
        }

        public void setCol1(String col1) {
            this.col1 = col1;
        }

        public int getCol2() {
            return col2;
        }

        public void setCol2(int col2) {
            this.col2 = col2;
        }

        public double getCol3() {
            return col3;
        }

        public void setCol3(double col3) {
            this.col3 = col3;
        }

        public String getCol4() {
            return col4;
        }

        public void setCol4(String col4) {
            this.col4 = col4;
        }

        public String getCol5() {
            return col5;
        }

        public void setCol5(String col5) {
            this.col5 = col5;
        }
    }
}
