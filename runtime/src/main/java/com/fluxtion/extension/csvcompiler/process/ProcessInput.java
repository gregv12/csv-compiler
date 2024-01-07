package com.fluxtion.extension.csvcompiler.process;

import com.fluxtion.extension.csvcompiler.RowMarshaller;

import java.io.IOException;
import java.io.PrintWriter;

public class ProcessInput {

    public static void run(RowMarshaller<?> rowMarshaller) throws IOException {
        System.out.println("printing headers - START");
        rowMarshaller.writeHeaders(new PrintWriter(System.out));
        System.out.println("printing headers - END");
    }

}
