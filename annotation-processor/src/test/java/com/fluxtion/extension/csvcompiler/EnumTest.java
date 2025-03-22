package com.fluxtion.extension.csvcompiler;

import com.fluxtion.extension.csvcompiler.beans.BeanWithEnum;
import com.fluxtion.extension.csvcompiler.beans.TestEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

public class EnumTest {

    @Test
    public void testEnum() {
        RowMarshaller<BeanWithEnum> marshaller = RowMarshaller.load(BeanWithEnum.class);
        List<BeanWithEnum> beanWithEnumList = marshaller
                .stream(
                        "assetName,innergetEnumValue\n" +
                                "VALUE1,VALUE_1\n" +
                                "VALUE2,VALUE_2\n")
                .collect(Collectors.toList());
        Assertions.assertEquals(2, beanWithEnumList.size());

        Assertions.assertEquals(TestEnum.VALUE1, beanWithEnumList.get(0).getAssetName());
        Assertions.assertEquals(BeanWithEnum.InnerEnum.VALUE_1, beanWithEnumList.get(0).getInnergetEnumValue());

        Assertions.assertEquals(TestEnum.VALUE2, beanWithEnumList.get(1).getAssetName());
        Assertions.assertEquals(BeanWithEnum.InnerEnum.VALUE_2, beanWithEnumList.get(1).getInnergetEnumValue());
    }
}
