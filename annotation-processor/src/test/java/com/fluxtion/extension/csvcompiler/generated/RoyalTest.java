package com.fluxtion.extension.csvcompiler.generated;

import com.fluxtion.extension.csvcompiler.RowMarshaller;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class RoyalTest {
    @Test
    public void testDefaultOptionalLookup(){
        RowMarshaller.load(DefaultLookupOptional.class);
    }

    @Test
    public void replay(){

        Map<String, String> registeredIdMap = Map.of(
                "registered", "1",
                "unregistered", "2",
                "waiting", "3",
                "unknown", "4"
        ) ;
        String data =
                "latest age,name          ,registered     ,resident,town\n" +
                "48        ,greg higgins  , registered    ,true    ,London\n" +
                "          ,bilbo         , registered    ,true    ,New york\n" +
                "54        ,tim higgins   ,               ,false   ,Sheffield\n" +
                "154       ,Rip Van Winkle, unregistered ,true    ,Toy town\n";


        RowMarshaller.load(Royalty.class)
                .addLookup("registeredId", registeredIdMap::get)
                .stream(data)
                .forEach(System.out::println);
    }
}
