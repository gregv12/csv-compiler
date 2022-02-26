# CSV Compiler

A highly optimised and simple to use java csv marshalling library driven by annotations. Converts a csv source into a
stream of java beans for processing within an application, equivalent to a handwritten marshaller in performance.
Executes as an annotation processor generating CSV marshallers at build time for any classes annotated
with ```@CsvMarshaller```.

- Probably the fastest java csv parser that operates in the cloud
- Zero GC field marshaller
- No external runtime dependencies
- Simple to use annotations
- No runtime byte code generation

## Performance

The CSV compiler annotation processor generates a marshaller during compilation. When deployed as a stateless function
in the cloud the only cpu cycles billed are used to parse the data. For smaller documents CSV compiler will have
finished well before interpreting parsers have completed. CSV compiler can be effectively combined with AOT Graal for
low startup times as the only code executing is statically compiled parsing logic with no indirection costs.

The parser itself employs several optimisations at runtime:

- Can operate as a zerogc source, re-using the target bean as a flyweight
- Converts primitives in a zerogc manner
- Highly optimised number parsers
- Native support of CharSequence, reusing buffers
- Zero cost abstraction only generating features if specified in annotations
- Skips columns if they are not required in the target bean
- Reduced internal conditional execution to aid branch prediction

If reducing costs and energy consumption are important to you please consider using or help improve CVS compiler.

![](images/CsvCompilerPerformanceGraphRelative.png)

## Dependencies

- CSV compiler annotation processor: executes at build time generating custom marshallers. Not required in runtime
  operation, use provided scope
- CSV compiler runtime: small library that provides zerogc utilities and interface definitions

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

## Example

This example converts csv -> bean -> process each bean record in a java stream. The example is available [here]()

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

Load CSV marshaller for the bean, set error listener, stream from a reader or String and push records to a consumer.

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

Application execution output:

```text
Main.Person(name=Linda Smith, age=43)
Main.Person(name=Soren Miller, age=33)
Person problem pushing 'not a number' from row:'4' fieldIndex:'1' targetMethod:'Person#setAge' error:'java.lang.NumberFormatException: For input string: "not a number"'
Max age:43
```

steps to process a CSV source:

1. Add CVS compiler dependencies to you project.
2. Create a java bean with getters and setter for persistent properties
3. Add a ```@CSVMarshaller``` annotation to the java bean source file
4. Load marshaller using ```RowMarshaller.load([Bean.class])```
5. Optionally supply an error listener to handle any marshalling errors. ```.setErrorLog(ValidationLogger.CONSOLE)```
6. Stream from a reader or a String to the marshaller add a consumer that will process marshalled instances
   ```.stream(Consumer<[Bean.class]>, [Reader])```


