package com.fluxtion.extension.csvcompiler;

import com.fluxtion.extension.csvcompiler.beans.AllNativeMarshallerTypes;
import com.fluxtion.extension.csvcompiler.beans.Person;
import com.fluxtion.extension.csvcompiler.processor.Util;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class LoopingTest {

    @Test
    public void iteratorSingleItemTest() {
        String input = "booleanProperty,byteProperty,doubleProperty,floatProperty,intProperty,longProperty,shortProperty,stringProperty\n" +
                "true,1,10.7,1.5,100,2000,4,hello\n" +
                "";
        List<AllNativeMarshallerTypes> resultList = new ArrayList<>();
        Iterator<AllNativeMarshallerTypes> iterator = RowMarshaller.load(AllNativeMarshallerTypes.class)
                .iterator(new StringReader(input));

        while (iterator.hasNext()) {
            AllNativeMarshallerTypes data = iterator.next();
            resultList.add(data);
        }

        AllNativeMarshallerTypes bean = new AllNativeMarshallerTypes();
        bean.setBooleanProperty(true);
        bean.setByteProperty((byte) 1);
        bean.setDoubleProperty(10.7);
        bean.setFloatProperty(1.5f);
        bean.setIntProperty(100);
        bean.setLongProperty(2000);
        bean.setShortProperty((short) 4);
        bean.setStringProperty("hello");

        assertIterableEquals(
                Util.listOf(bean),
                resultList
        );
    }

    @Test
    public void iteratorMultipleItemsTest() {
        String input = "booleanProperty,byteProperty,doubleProperty,floatProperty,intProperty,longProperty,shortProperty,stringProperty\n" +
                "true,1,10.7,1.5,100,2000,4,hello\n" +
                "true,2,10.7,1.5,200,2000,4,hello\n" +
                "";
        val resultList = new ArrayList<>();
        val iterator = RowMarshaller.load(AllNativeMarshallerTypes.class)
                .iterator(new StringReader(input));

        while (iterator.hasNext()) {
            AllNativeMarshallerTypes data = iterator.next();
            resultList.add(data);
        }

        val bean = new AllNativeMarshallerTypes();
        bean.setBooleanProperty(true);
        bean.setByteProperty((byte) 1);
        bean.setDoubleProperty(10.7);
        bean.setFloatProperty(1.5f);
        bean.setIntProperty(100);
        bean.setLongProperty(2000);
        bean.setShortProperty((short) 4);
        bean.setStringProperty("hello");


        AllNativeMarshallerTypes bean2 = new AllNativeMarshallerTypes();
        bean2.setBooleanProperty(true);
        bean2.setByteProperty((byte) 2);
        bean2.setDoubleProperty(10.7);
        bean2.setFloatProperty(1.5f);
        bean2.setIntProperty(200);
        bean2.setLongProperty(2000);
        bean2.setShortProperty((short) 4);
        bean2.setStringProperty("hello");

        assertIterableEquals(
                Util.listOf(bean, bean2),
                resultList
        );
    }

    @Test
    public void iteratorHeaderTest() {
        String input = "booleanProperty,byteProperty,doubleProperty,floatProperty,intProperty,longProperty,shortProperty,stringProperty\n" +
                "";
        val resultList = new ArrayList<>();
        val iterator = RowMarshaller
                .load(AllNativeMarshallerTypes.class)
                .iterator(new StringReader(input));
        while (iterator.hasNext()) {
            AllNativeMarshallerTypes data = iterator.next();
            resultList.add(data);
        }
        Assertions.assertEquals(0, resultList.size());
    }

    @Test
    public void iteratorNoDataTest() {
        String input = "";
        val resultList = new ArrayList<>();
        val iterator = RowMarshaller
                .load(AllNativeMarshallerTypes.class)
                .iterator(new StringReader(input));
        while (iterator.hasNext()) {
            AllNativeMarshallerTypes data = iterator.next();
            resultList.add(data);
        }
        Assertions.assertEquals(0, resultList.size());
    }

    @Test
    public void streamSingleItemTest() {
        String input = "booleanProperty,byteProperty,doubleProperty,floatProperty,intProperty,longProperty,shortProperty,stringProperty\n" +
                "true,1,10.7,1.5,100,2000,4,hello\n" +
                "";
        List<AllNativeMarshallerTypes> resultList = new ArrayList<>();
        RowMarshaller.load(AllNativeMarshallerTypes.class).stream(new StringReader(input))
                .forEach(resultList::add);

        AllNativeMarshallerTypes bean = new AllNativeMarshallerTypes();
        bean.setBooleanProperty(true);
        bean.setByteProperty((byte) 1);
        bean.setDoubleProperty(10.7);
        bean.setFloatProperty(1.5f);
        bean.setIntProperty(100);
        bean.setLongProperty(2000);
        bean.setShortProperty((short) 4);
        bean.setStringProperty("hello");

        assertIterableEquals(
                Util.listOf(bean),
                resultList
        );
    }

    @Test
    public void streamMultipleTest() {
        String input = "booleanProperty,byteProperty,doubleProperty,floatProperty,intProperty,longProperty,shortProperty,stringProperty\n" +
                "true,1,10.7,1.5,100,2000,4,hello\n" +
                "true,2,10.7,1.5,200,2000,4,hello\n" +
                "";
        List<AllNativeMarshallerTypes> resultList = new ArrayList<>();
        RowMarshaller.load(AllNativeMarshallerTypes.class).stream(new StringReader(input))
                .forEach(resultList::add);

        AllNativeMarshallerTypes bean = new AllNativeMarshallerTypes();
        bean.setBooleanProperty(true);
        bean.setByteProperty((byte) 1);
        bean.setDoubleProperty(10.7);
        bean.setFloatProperty(1.5f);
        bean.setIntProperty(100);
        bean.setLongProperty(2000);
        bean.setShortProperty((short) 4);
        bean.setStringProperty("hello");

        AllNativeMarshallerTypes bean2 = new AllNativeMarshallerTypes();
        bean2.setBooleanProperty(true);
        bean2.setByteProperty((byte) 2);
        bean2.setDoubleProperty(10.7);
        bean2.setFloatProperty(1.5f);
        bean2.setIntProperty(200);
        bean2.setLongProperty(2000);
        bean2.setShortProperty((short) 4);
        bean2.setStringProperty("hello");

        assertIterableEquals(
                Util.listOf(bean, bean2),
                resultList
        );
    }

    @Test
    public void streamHeaderTest() {
        String input = "booleanProperty,byteProperty,doubleProperty,floatProperty,intProperty,longProperty,shortProperty,stringProperty\n" +
                "";
        List<AllNativeMarshallerTypes> resultList = new ArrayList<>();
        RowMarshaller.load(AllNativeMarshallerTypes.class).stream(new StringReader(input))
                .forEach(resultList::add);
        Assertions.assertEquals(0, resultList.size());
    }

    @Test
    public void streamNoDataTest() {
        String input = "";
        List<AllNativeMarshallerTypes> resultList = new ArrayList<>();
        RowMarshaller.load(AllNativeMarshallerTypes.class).stream(new StringReader(input))
                .forEach(resultList::add);
        Assertions.assertEquals(0, resultList.size());
    }

    @Test
    public void streamCalculationNewInstanceTest() {
        String input = "name,age\n" +
                "tim,32\n" +
                "lisa,44\n";
        int sum = RowMarshaller.load(Person.class).stream(new StringReader(input))
                .mapToInt(Person::getAge)
                .sum();

        Assertions.assertEquals(76, sum);
    }

    @Test
    public void streamCalculationSameInstanceTest() {
        String input = "name,age\n" +
                "tim,32\n" +
                "lisa,44\n";
        int sum = RowMarshaller.load(Person.PersonRecycleInstance.class).stream(new StringReader(input))
                .mapToInt(Person::getAge)
                .sum();

        Assertions.assertEquals(76, sum);
    }

}
