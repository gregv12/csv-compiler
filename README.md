# CSV Compiler
A java csv marshalling library that generates and compiles a CSV marshaller at build time. Driven by annotations 
CSV compiler generates a self-contained class that is a highly optimised customised marshaller equivalent to a 
handwritten marshaller in performance.
- Probably the fastest java csv parser that operates in the cloud
- Zero GC field marshaller
- No external runtime dependencies
- Simple to use annotations

## Performance
CSV compiler generates a marshaller during compilation resulting in a once only cost at development time. When deployed 
as a stateless function in the cloud the only cpu cycles billed are used to parse the data. Most other parsers pay the 
cost of introspection and document processing on every invocation. For smaller documents CSV compiler will have finished 
well before dynamically generated parsers have completed. If CSV compiler is combined with AOT Graal compilation the disparity becomes 
even greater as byte code generation optimisation that many dynamic parsers employ is not available with Graal AOT.

If reducing costs and emissions are important to you please consider using or help improve CVS compiler.

## Example
A marshaller example is available [here](), steps to process a CSV source:
1. Add CVS compiler dependencies to you project. 
2. Create a java bean with getters and setter for persistent properties 
3. Add a ```@CSVMarshaller``` annotation to the java bean source file
4. Load marshaller using ```CsvMarshallerLoader.marshaller([Bean.class])```
5. Optionally supply an error listener to handle any marshalling errors. ```.setErrorLog(ValidationLogger.CONSOLE)```
6. Stream from a reader to the marshaller add a consumer that will process marshalled instances ```.stream(Consumer<[Bean.class]>, [Reader])```

### Dependencies
- The annotation processor: used during compile time to generate the custom marshaller, not required in runtime operation. 
Use provided scope
- CSV compiler runtime: small library that provides efficient marshalling and interface definitions

```xml
<dependency>
    <groupId>com.fluxtion.csv-compiler</groupId>
    <artifactId>csv-compiler</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
<dependency>
    <groupId>com.fluxtion.csv-compiler</groupId>
    <artifactId>csv-compiler-processor</artifactId>
    <version>1.0-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>
```

### Code
Mark a java bean with annotation ```@CSVMarshaller```

```java
@ToString
@CsvMarshaller
public class Person {
    private String name;
    private int age;

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
}
```

Load CSV marshaller for the bean, set error listener, stream from a reader and push records to a consumer.
```java
public class Main {

    public static void main(String[] args) {
        RowMarshaller.load(Person.class)
                .setErrorLog(ValidationLogger.CONSOLE)
                .stream("name,age\n" +
                        "Linda Smith,43\n" +
                        "Soren Miller,33\n" +
                        "fred,not a number\n")
                .peek(System.out::println)
                .mapToInt(Person::getAge)
                .max()
                .ifPresent(i -> System.out.println("Max age:" + i));
    }
}
```
Running the application outputs:
```text
Main.Person(name=Linda Smith, age=43)
Main.Person(name=Soren Miller, age=33)
Person problem pushing 'not a number' from row:'4' fieldIndex:'1' targetMethod:'Person#setAge' error:'java.lang.NumberFormatException: For input string: "not a number"'
Max age:43
```

