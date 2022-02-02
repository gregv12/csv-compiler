package com.fluxtion.extension.csvcompiler;

import com.fluxtion.extension.csvcompiler.beans.Person;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.fluxtion.extension.csvcompiler.SuccessfulMarshallerTest.testPersonErrors;

public class FailingMarshallerTest {

    @Test
    public void recordMultipleErrors() {
        testPersonErrors(
                Person.class,
                "name,age\n" +
                        "tim,sfgdg\n" +
                        "lisa,44\n" +
                        "lisa,fddg\n",
                List.of(2,4),
                Person.build(Person::new, "lisa", 44)
        );
    }

    @Test
    public void noSkipCsvMarshallerEmptyLines() {
        testPersonErrors(
                Person.NoSkip.class,
                "name,age\n" +
                        "tim,32\n" +
                        "\n" +
                        "lisa,44\n",
                List.of(3),
                Person.build(Person.NoSkip::new, "tim", 32),
                Person.build(Person.NoSkip::new, "lisa", 44)
        );
    }

    @Test
    public void failFastTest() {
        Assertions.assertThrows(CsvProcessingException.class, () -> testPersonErrors(
                Person.FailFast.class,
                "name,age\n" +
                        "tim,dfrfrf\n" +
                        "\n" +
                        "lisa\n",
                List.of(2)
        ));
    }
}
