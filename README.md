# SQL-dumper

Command line tool, that dumps the result of SQL to an CSV on the filesystem. Queries can be using "extended SQL"
split into subqueries, reducing the complexity on the server side.

Usage is easy:

    java -jar sql_dumper.jar sqlPath outPath user pass jdbc

with the arguments being:

- `sqlPath`: Path to a folder holding all .SQL files that will be executed, e.g. `"C:\path\to\folder\with\sql\files\"`
- `outPath`: Path to a folder, where the .CSV will be dumped, e.g. `"C:\path\to\csv\"`
- `user`: Your username to access the database, if in doubt sorrounded by `""`, e.g. `"myusername"`
- `pass`: Your password to access the database, e.g. `"123myPassword"`
- `jdbc`: The `JDBC` connection string to establish the connection, e.g. `"jdbc:oracle:thin:@sdwh.company.com:1333:sdwh"`

The outputted CSVs will be placed in a sub directory following the convention `YYYY_MM_DD` and be named after the SQL script 
file in the input dir (`myScript.SQL` will lead to a resultsfile named `myScript.CSV`)

# extended SQL

The following "extended SQL" commands are supported. Since being hidden as a comment (indicated by `--`, which is mandatory), 
the SQL files are still executable by every other .SQL Tool (like the Oracle SQL Developer). Extended SQL is likely used in the `WHERE` clause 
of your SQL script.

All subquery results will be concatenated into the same, output CSV file.

The use of extended SQL is completely **optional**. Querying standard SQL will work, too.

### Output text (`$ESQL_text`)

Inserts the given text. This is necessary because extended SQL is hidden in comments.

#### Example

The following code 

      -- ("WHERE mw.WERT_CODE = 123")
      
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

Gerates a subquery for every number in the given range.

#### Example

The following code

    -- $ESQL_text("AND MY_COL = ") $spreadNum(1:100)
    
Will generate 100 subqueries, the first one having the line `AND MY_COL = 1`. The last one will be `AND MY_COL = 100`.


### Generate subqueries by splitting a range into multiple subqueries (`$ESQL_spreadNumArea`)

Generates a given number of subqueries by slicing the given range into ranges of equal size.

#### Example

The following code

    -- $ESQL_text("AND MY_COL ") $ESQL_spreadNumArea(1:100:10)
    
Will generate 10 subqueries, with the first one being `AND MY_COL BETWEEN 1 AND 9`.

# Caveats / TODO

- Right now, only oracle thin connection are supported. Feel free to enhance and support your pull request :)
