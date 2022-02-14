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
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.io.StringReader;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Fork(value = 2, warmups = 1)
@Warmup(iterations = 1, time = 2)
@Measurement(iterations = 2, time = 4)
public class FirstBenchmark {

    String data1 = "1,\"Eldon Base for stackable storage shelf, platinum\",Muhammed MacIntyre,3,-213.25,38.94,35,Nunavut,Storage & Organization,0.8\n" +
            "2,\"1.7 Cubic Foot Compact \"\"Cube\"\" Office Refrigerators\",Barry French,293,457.81,208.16,68.02,Nunavut,Appliances,0.58\n" +
            "3,\"Cardinal Slant-D® Ring Binder, Heavy Gauge Vinyl\",Barry French,293,46.71,8.69,2.99,Nunavut,Binders and Binder Accessories,0.39\n" +
            "4,R380,Clay Rozendal,483,1198.97,195.99,3.99,Nunavut,Telephones and Communication,0.58\n" +
            "5,Holmes HEPA Air Purifier,Carlos Soltero,515,30.94,21.78,5.94,Nunavut,Appliances,0.5\n" +
            "6,G.E. Longer-Life Indoor Recessed Floodlight Bulbs,Carlos Soltero,515,4.43,6.64,4.95,Nunavut,Office Furnishings,0.37\n" +
            "7,\"Angle-D Binders with Locking Rings, Label Holders\",Carl Jackson,613,-54.04,7.3,7.72,Nunavut,Binders and Binder Accessories,0.38\n" +
            "8,\"SAFCO Mobile Desk Side File, Wire Frame\",Carl Jackson,613,127.70,42.76,6.22,Nunavut,Storage & Organization,\n" +
            "9,\"SAFCO Commercial Wire Shelving, Black\",Monica Federle,643,-695.26,138.14,35,Nunavut,Storage & Organization,\n" +
            "10,Xerox 198,Dorothy Badders,678,-226.36,4.98,8.33,Nunavut,Paper,0.38\n" +
            "11,Xerox 1980,Neola Schneider,807,-166.85,4.28,6.18,Nunavut,Paper,0.4\n" +
            "12,Advantus Map Pennant Flags and Round Head Tacks,Neola Schneider,807,-14.33,3.95,2,Nunavut,Rubber Bands,0.53\n" +
            "13,Holmes HEPA Air Purifier,Carlos Daly,868,134.72,21.78,5.94,Nunavut,Appliances,0.5\n" +
            "14,\"DS/HD IBM Formatted Diskettes, 200/Pack - Staples\",Carlos Daly,868,114.46,47.98,3.61,Nunavut,Computer Peripherals,0.71\n" +
            "15,\"Wilson Jones 1\"\" Hanging DublLock® Ring Binders\",Claudia Miner,933,-4.72,5.28,2.99,Nunavut,Binders and Binder Accessories,0.37\n" +
            "16,Ultra Commercial Grade Dual Valve Door Closer,Neola Schneider,995,782.91,39.89,3.04,Nunavut,Office Furnishings,0.53\n" +
            "17,\"#10-4 1/8\"\" x 9 1/2\"\" Premium Diagonal Seam Envelopes\",Allen Rosenblatt,998,93.80,15.74,1.39,Nunavut,Envelopes,0.4\n" +
            "18,Hon 4-Shelf Metal Bookcases,Sylvia Foulston,1154,440.72,100.98,26.22,Nunavut,Bookcases,0.6\n" +
            "19,\"Lesro Sheffield Collection Coffee Table, End Table, Center Table, Corner Table\",Sylvia Foulston,1154,-481.04,71.37,69,Nunavut,Tables,0.68\n" +
            "20,g520,Jim Radford,1344,-11.68,65.99,5.26,Nunavut,Telephones and Communication,0.59\n" +
            "21,LX 788,Jim Radford,1344,313.58,155.99,8.99,Nunavut,Telephones and Communication,0.58\n" +
            "22,Avery 52,Carlos Soltero,1412,26.92,3.69,0.5,Nunavut,Labels,0.38\n" +
            "23,Plymouth Boxed Rubber Bands by Plymouth,Carlos Soltero,1412,-5.77,4.71,0.7,Nunavut,Rubber Bands,0.8\n" +
            "24,\"GBC Pre-Punched Binding Paper, Plastic, White, 8-1/2\"\" x 11\"\"\",Carl Ludwig,1539,-172.88,15.99,13.18,Nunavut,Binders and Binder Accessories,0.37";


    @Benchmark
    public void noReuse(Blackhole blackhole) {
        RowMarshaller.load(SampleData.class).forEach(blackhole::consume, new StringReader(data1));


//        List<SampleData> data = RowMarshaller.load(SampleData.class).stream(data1)
//                .collect(Collectors.toList());
//        blackhole.consume(data);
    }

    @Benchmark
    public void flyweight(Blackhole blackhole) {
        RowMarshaller.load(SampleDataFlyweight.class).forEach(blackhole::consume, new StringReader(data1));
//        List<SampleDataFlyweight> data = RowMarshaller.load(SampleDataFlyweight.class).stream(data1)
//                .collect(Collectors.toList());
//        blackhole.consume(data);
    }


}
