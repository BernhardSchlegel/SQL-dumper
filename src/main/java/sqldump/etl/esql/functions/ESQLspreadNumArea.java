package sqldump.etl.esql.functions;

import sqldump.log.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Spreads a range of numbers into areas
 * $spreadNumArea(MIN:MAX:NUM), where
 *      MIN: minimum number to start
 *      MAX: maximum number to start
 *      NUM: number of areas (= number of generated subqueries), recommendation is 20
 */
public class ESQLspreadNumArea extends FunctionESQL {
    public static boolean testMatch(String potentialMatch) {
        return Pattern.matches("\\$ESQL_spreadNumArea\\(.*\\)", potentialMatch);
    }

    public static List<String> execute(String match, Log log) {

        List<String> returnBlocks = new ArrayList<String>();
        if(ESQLspreadNumArea.testMatch(match)) {
            Pattern p = Pattern.compile("\\s*\\$ESQL_spreadNumArea\\((.*?)\\)\\s*");
            Matcher m = p.matcher(match);

            if (m.find()) {
                for (Integer i = 0; i < m.groupCount(); i++) {
                    String block = m.group(1 + i);

                    String[] limits = block.split(":",3);

                    if (limits.length != 3) {
                        log.error("Number of levels (" + limits.length + ") doesnt match requirement (3)");
                        returnBlocks.add(match);
                        return returnBlocks;
                    }
                    int lowerLimit = Integer.parseInt(limits[0]);
                    int upperLimit = Integer.parseInt(limits[1]);
                    int areas = Integer.parseInt(limits[2]);

                    int increment = (int)Math.round(1.0 * (upperLimit - lowerLimit) / areas + 0.5);

                    // add all numbers in range
                    for (int currentNumber = lowerLimit; currentNumber <= upperLimit; currentNumber = currentNumber + increment) {
                        returnBlocks.add("BETWEEN " + String.valueOf(currentNumber) + " AND " + String.valueOf(currentNumber+increment-1));
                    }
                }
            }
        } else {
            returnBlocks.add(match);
        }

        return returnBlocks;
    }
}
