# CSV Compiler

A simple to use efficient java csv marshalling library driven by annotations. Converts a csv source into a
stream of java beans for processing within an application, equivalent to a handwritten marshaller in performance.
The annotation processor generates CSV marshallers at build time for classes annotated with ```@CsvMarshaller```, 
supported features:

- Simple to use annotations
- CSV data pipeline transformations
- Fully compliant csv parser for reading and writing
- Integrates with lombok to reduce boilerplate code
- Configurable mappings for headers and columns
- Index or named column support for input files
- Fields can be optional, default value substituted when absent and auto trimmed 
- Full escaping support for quoted style fields, configurable
- Missing column support allows partial evaluation
- Comments ignored, configurable
- Empty lines ignored, configurable
- Pluggable custom serializers framework
- Pluggable validation framework
- Pluggable lookup support to convert field values
- Error handling and reporting
- Designed to integrate with java.util.stream.Stream
- Compiles AOT, zero startup cost
- Zero GC field serializers for primitive types provided
- No external runtime dependencies
- No runtime byte code generation

# Dependencies

- CSV compiler annotation processor: executes at build time to generate a marshaller. **Not required at runtime**, 
use provided scope
- CSV compiler runtime: runtime library providing zerogc utilities and interface definitions

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

# Examples

## CSV data pipeline
This example loads a CSV file transforms it and writes the results to another CSV file. The pipeline demonstrates:
- Extracting a subset of columns from the source data
- Replaces missing values with default values
- Columns are one of, input only, input/output or derived output only
- Header names have spaces
- Rows are filtered from output if they do not meet criteria
- Derived value transforms a textual field to numerical representation
- Derived value transforms a numerical value by an operation
- The output is written to a new file excluding the input only columns 

Solving this problem requires two steps; firstly annotate a POJO's fields with ```@ColumnMapping```, secondly use the 
utility method ```RowMarshaller#transform``` to load the input file, transform/filter records with a java.util.stream.Stream 
and then finally write to an output file

### Csv record file
The HousingData class represents the mapping of input, output and derived fields using annotations. The ```@CsvMarshaller```
annotation is used in conjunction with lombok to generate a POJO that has fluent accessors, removing the need for
boilerplate code. 


```java
@Data
@CsvMarshaller(fluent = true)
@Accessors(fluent = true)
public class HouseSaleRecord {
    //input only
    @ColumnMapping(outputField = false)
    private int Order;
    @ColumnMapping(outputField = false, columnName = "Lot Frontage", defaultValue = "-1")
    private int Lot_Frontage;
    @ColumnMapping(outputField = false, columnName = "MS Zoning")
    private String MS_Zoning;

    //input and output
    private int PID;
    @ColumnMapping(columnName = "MS SubClass")
    private int MS_SubClass;

    //derived no input field
    @ColumnMapping(optionalField = true)
    private int Lot_Frontage_Squared;
    @ColumnMapping(optionalField = true)
    private int ms_zone_category;
}
```

- Input only fields are annotated with ```@ColumnMapping(outputField = false)```
- Output only derived fields are annotated with ```@ColumnMapping(optionalField = true)```
- Column names are mapped with ```@ColumnMapping(columnName = "MS SubClass")```
- Default values are marked with ``` @ColumnMapping(defaultValue = "-1")```
- Annotations can be combined - ```@ColumnMapping(outputField = false, columnName = "Lot Frontage", defaultValue = "-1")```

### CSV data pipeline

The main method creates a csv data pipeline using the utility function from the ```RowMarshaller#transform``` class

```java
public class AimesHousingCsvPipeline {

    @SneakyThrows
    public static void main(String[] args) {
        RowMarshaller.load(HouseSaleRecord.class).transform(
                Path.of("data/AmesHousing.csv"),
                Path.of("data/PostProcess.csv"),
                s -> s.filter(record -> record.Lot_Frontage() > 0)
                        .map(MainTest::squareLotFrontage)
                        .map(MainTest::ms_zone_to_category)
                        .filter(record -> record.ms_zone_category() > 0));
    }

    public static HouseSaleRecord squareLotFrontage(HouseSaleRecord houseSaleRecord) {
        int lotFrontage = houseSaleRecord.Lot_Frontage();
        houseSaleRecord.Lot_Frontage_Squared(lotFrontage * lotFrontage);
        return houseSaleRecord;
    }

    public static HouseSaleRecord ms_zone_to_category(HouseSaleRecord houseSaleRecord) {
        switch (houseSaleRecord.MS_Zoning()) {
            case "A" -> houseSaleRecord.ms_zone_category(1);
            case "FV" -> houseSaleRecord.ms_zone_category(2);
            case "RL" -> houseSaleRecord.ms_zone_category(3);
            case "RM" -> houseSaleRecord.ms_zone_category(4);
            default -> houseSaleRecord.ms_zone_category(-1);
        }
        return houseSaleRecord;
    }
}
```

## Load CSV and process each record with java.util.stream.Stream

This example converts csv -> bean -> process each bean record in a java stream. The example is available [here]()

### Code

Mark a java bean with annotation ```@CSVMarshaller``` use lombok ```@Data``` to remove the boilerplate getter/setter 
methods

```java

@Data
@CsvMarshaller
public class Person {
    private String name;
    private int age;
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

## Performance

The CSV compiler annotation processor generates a marshaller during compilation. When deployed as a stateless function
in the cloud the only cpu cycles billed are used to parse the data. For smaller documents CSV compiler can
finish well before interpreting parsers have completed. Combined with Graal native results in
low startup times as marshalling code is statically compiled AOT.

### Example for calculating a sum of doubles in a column. Single column and 10 Columns

![](docs/images/CsvCompilerPerformanceGraphRelative.png)

10 column version, 9 columns are ignored.

The parser itself employs several optimisations at runtime:

- Can operate as a zerogc source, re-using the target bean as a flyweight
- Converts primitives in a zerogc manner
- Highly optimised number parsers
- Native support of CharSequence, reusing buffers
- Zero cost abstraction only generating features if specified in annotations
- Skips columns if they are not required in the target bean
- Reduced internal conditional execution to aid branch prediction

If reducing costs and energy consumption are important to you please consider using or help improve CVS compiler.


