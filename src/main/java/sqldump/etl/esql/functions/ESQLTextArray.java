package sqldump.etl.esql.functions;

import sqldump.log.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ESQLTextArray splits the given text and transforms each to a single sub query
 * $ESQL_textArray(GROUPSIZE, "foo","bar",...), where
 *  GROUPSIZE: Integer indicating how many strings are grouped into a ('foo', 'bar') block (2)
 * use in combination with $ESQL_text,
 * -- $ESQL_text("AND MY_COL IN ") $ESQL_textArray(2, "VALA", "VALB", "VALC")
 */
public class ESQLTextArray extends FunctionESQL {
    public static boolean testMatch(String potentialMatch) {
        return Pattern.matches("\\$ESQL_textArray\\(\\d*,.*\\)", potentialMatch);
    }

    public static List<String> execute(String match, Log log) {

        List<String> returnBlocks = new ArrayList<String>();
        if(ESQLTextArray.testMatch(match)) {
            Pattern p = Pattern.compile("\\s*\\$ESQL_textArray\\((\\d*,.*?)\\)\\s*");
            Matcher m = p.matcher(match);

            if (m.find()) {
                for (int i = 0; i < m.groupCount(); i++) {
                    String block = m.group(1 + i);

                    // get number of elements
                    int count = 0;
                    Pattern p2 = Pattern.compile(",");
                    Matcher m2 = p2.matcher(block);
                    while (m2.find())
                        count++;
                    log.info("found " + String.valueOf(count) + " elements");

                    String[] values = block.split(",", 0);  // no limit, may cause stability issue on small hardware

                    // get blocksize
                    int blocksize =  Integer.parseInt(values[0]);

                    // add all numbers in range
                    for (int j = 1; j < (count+1);) {
                        String superString = "(";
                        for (int k = 0; k < blocksize && j <= (count+1); k++) {
                            String tempstr = values[j].toString();
                            tempstr = tempstr.replace("\"", "'");
                            if ((k + 1) < blocksize && (j + 1) < (count)) {
                                tempstr += ", ";
                            }
                            superString += tempstr;
                            j++;    // increment j as well
                        }
                        superString += ")";
                        returnBlocks.add(superString);
                    }
                }
            }
        } else {
            returnBlocks.add(match);
        }

        return returnBlocks;
    }
}
