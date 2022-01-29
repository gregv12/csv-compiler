package com.fluxtion.extension.csvcompiler;

import com.fluxtion.extension.csvcompiler.annotations.CsvMarshaller;

import java.util.Objects;

@CsvMarshaller(processEscapeSequences = true, formatSource = true, newBeanPerRecord = false)
public class PersonRecycleInstance {
    private String name;
    private int age;

    public PersonRecycleInstance(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public PersonRecycleInstance() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "Person{" +
                "name='" + name + '\'' +
                ", age=" + age +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PersonRecycleInstance)) return false;
        PersonRecycleInstance person = (PersonRecycleInstance) o;
        return age == person.age && Objects.equals(name, person.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, age);
    }
}
