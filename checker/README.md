# CSV cli
A command line utility for CSV validation and transformation driven from a yaml configuration. The yaml
file defines all the conversions and validation rules to apply to the incoming data file and the output
is written to a results directory

# Yaml config
The processConfig.yaml defines:
- columns:
  types, conversion code, validation code, default values, optional columns, lookup tables etc.
- derived columns:
  Always processed after Columns
- conversionFunctions:
  Reusable library conversion function
- validationFunctions:
  Reusable library validation functions
  have access to validationLog to log validation problems
- lookupTables:
  Access to keyed data in a map that columns can use to transform incoming data
- meta lookup
  A meta lookup table is added that contains global entries such as filename


## Install
- download to same directory:
    - csvChecker.jar - the tool
    - data.csv - sample data
    - processConfig.yaml - sample configuration to drive the csvChecker
- install java 17 or above
- **unix validate install** in a terminal run ```./csvChecker.jar -h```
- **windows validate install** in a command window run ```java -jar ./csvChecker.jar -h```

```
./csvChecker.jar -h
Usage: csvCheck [-dfhsV] <check config> <csv data>
      <check config>   Configuration of csv check logic
      <csv data>       csv data file
  -d, --sampleData     prints data for sample config
  -f, --fullConfig     prints a sample yaml config showing all options
  -h, --help           Show this help message and exit.
  -s, --simpleConfig   prints a minimal sample yaml config
  -V, --version        Print version information and exit.
```
## Execute a sample

```
java -jar csvChecker.jar 
config: /Users/greg/IdeaProjects/csv-compiler/server/src/test/sample/processConfig.yaml
data  : /Users/greg/IdeaProjects/csv-compiler/server/src/test/sample/data.csv
Valid count  : 3
Invalid count: 2
```

### Output files in the results directory

```
ls results 
invalid.txt	valid.csv
```

- valid.csv transformed rows that meet the validation criteria
- invalid.txt failed validation for rows report

## Sample config
The csvChecker provides a sample config that can be displayed to the screen:

```
java -jar csvChecker.jar -s
sample yaml, showing minimal fields:
--------------------------------------
name: Royalty
trim: true

columns:
  ageInYears: {type: int, csvColumnName: 'latest age', optional: true, defaultValue: 50, validationFunction: checkAge}
  name:
    defaultValue: testing
    type: string
    converterCode:  |
      String myString = input.toString();
      return myString.toUpperCase();
  registered: {type: int, lookupTable: registeredId, defaultValue: unknown, validationFunction: checkRegistered}
  resident: {type: boolean}
  town: {type: string, converterFunction: toLowerCase}

derivedColumns:
  nameAndTown:
    type: string
    converterCode: return name + "->" + town;
  dataFile: {type: String, lookupTable: meta, defaultValue: dataFile}

conversionFunctions:
  toLowerCase:
    convertsTo: string
    code:  |
      String myString = input.toString();
      return myString.toLowerCase();

validationFunctions:
  checkAge:
    code: |
      if(ageInYears > 100){
        validationLog.accept(ageInYears +  " way too old!!", false);
        return false;
      }
      return true;

  checkRegistered:
    code: |
      if(registered > 4){
        validationLog.accept("Unsupported registration description", false);
        return false;
      }
      return true;

lookupTables:
  registeredId:
    registered: 1
    unregistered: 2
    waiting: 3
    unknown: 4
    default: 5
```

