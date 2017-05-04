# SQL-dumper

Command line tool, that dumps the result of SQL to an CSV on the filesystem. Queries can be using "extended SQL"
split into subqueries, reducing the complexity on the server side.

**Installation**: 
Download [Oracle JDBC Driver 12c](http://www.oracle.com/technetwork/database/features/jdbc/jdbc-drivers-12c-download-1958347.html) (tested with this version.) and put sql-dumper.jar and ojdbc7.jar in one folder.

**Usage** is easy:

    java -jar sql_dumper.jar sqlPath outPath user pass jdbc

with the arguments being:

- `sqlPath`: Path to a folder holding all .SQL files that will be executed, e.g. `"C:\path\to\folder\with\sql\files\"`
- `outPath`: Path to a folder, where the .CSV will be dumped, e.g. `"C:\path\to\csv\"`
- `user`: Your username to access the database, if in doubt surrounded by `""`, e.g. `"myusername"`
- `pass`: Your password to access the database, e.g. `"123myPassword"`
- `jdbc`: The `JDBC` connection string to establish the connection, e.g. `"jdbc:oracle:thin:@sdwh.company.com:1333:sdwh"`

The outputted CSV files will be saved to a sub directory of `outpath` following the convention `YYYY_MM_DD\` and be
named after the SQL script file in the input directory (`myScript.SQL` will lead to a resultfile named `myScript.CSV`).

## extended SQL

The following "extended SQL" commands are supported. Since being hidden as a comment (indicated by `--`, which is mandatory), 
the SQL files are still executable by every other SQL Tool (like the Oracle SQL Developer). Extended SQL is likely used 
in the `WHERE` clause of your SQL script.

All subquery results will be concatenated into a single, output CSV file.

The use of extended SQL is completely **optional**. Querying standard SQL will work, too.

### Output text (`$ESQL_text`)

Inserts the given text. This is necessary because extended SQL is hidden in comments.

#### Example

The following code 

      -- $ESQL_text("WHERE mw.WERT_CODE = 123")
      
Will simply output the text `"WHERE mw.WERT_CODE = 123` to sql query.

### Generate subqueries by an array of strings (`$ESQL_textArray`)

Generates subqueries by querying for values from the given array. The number of values
queried at a time can be set explicitly.

#### Example

The following code

     -- $ESQL_textArray("AND MY_COL IN ") $ESQL_textArray(2, "VALA", "VALB", "VALC", "VALD")
     
Will result in two subqueries, with the first one having `AND MY_COL IN ('VALA', 'VALB')`.


### Generate `IN` clause for a given number of numbers from an array (`$ESQL_numArray`)

Generates a subquery for the given numbers. Encapsulates multiple numbers into one subquery using the given blocksize.

#### Example

The following code

    -- $ESQL_text("AND MY_COL IN ") $ESQL_numArray(2, 117, 118, 119)
    
Will generate two subqueries from the given SQL file. The first one will contain `AND MY_COL IN (2, 117)` and the second 
one `AND MY_COL IN (118, 119)`.

### Generate subquery for every number in range (`$ESQL_spreadNum`)

Generates a subquery for every number in the given range.

#### Example

The following code

    -- $ESQL_text("AND MY_COL = ") $ESQL_spreadNum(1:100)
    
Will generate 100 subqueries, the first one having the line `AND MY_COL = 1`. The last one will be `AND MY_COL = 100`.


### Generate subqueries by splitting a range into multiple subqueries (`$ESQL_spreadNumArea`)

Generates a given number of subqueries by slicing the given range into ranges of equal size.

#### Example

The following code

    -- $ESQL_text("AND MY_COL ") $ESQL_spreadNumArea(1:100:10)
    
Will generate 10 subqueries, with the first one being `AND MY_COL BETWEEN 1 AND 9`.

## Caveats / TODO

- Only Oracle thin connection are supported.

Feel free to contribute and support your pull request :)

## History
v.0.0.3 (02.05.2017)

- dynamic linking of JDBC driver (ojdbc7.jar) 
- Minor Bugfixes in last iteration of NumArray and TextArray functions 

v.0.0.2 (30.11.2016)

- Improved performance using `statement.setFetchSize()` and improving string creation using a `StringBuilder`
- Minor Bugfixes in last iteration of NumArray and TextArray functions 
- Added compiled binary to /bin folder
- Documentation when calling the jar without parameters

v.0.0.1 (23.11.2016)

- initial commit

## License: The MIT License (MIT)

Copyright (c) 2016 Bernhard Schlegel

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.