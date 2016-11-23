package sqldump.etl.esql.functions;

import sqldump.log.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Spreads a range of numbers, syntas e.g.
 * $spreadNum(1:488)
 */
public class ESQLspreadNum extends FunctionESQL {
    public static boolean testMatch(String potentialMatch) {
        return Pattern.matches("\\$ESQL_spreadNum\\(.*\\)", potentialMatch);
    }

    public static List<String> execute(String match, Log log) {

        List<String> returnBlocks = new ArrayList<String>();
        if(ESQLspreadNum.testMatch(match)) {
            Pattern p = Pattern.compile("\\s*\\$ESQL_spreadNum\\((.*?)\\)\\s*");
            Matcher m = p.matcher(match);

            if (m.find()) {
                for (Integer i = 0; i < m.groupCount(); i++) {
                    String block = m.group(1 + i);

                    String[] limits = block.split(":",2);

                    int lowerLimit = Integer.parseInt(limits[0]);
                    int upperLimit = Integer.parseInt(limits[1]);

                    // add all numbers in range
                    for (int currentNumber = lowerLimit; currentNumber <= upperLimit; currentNumber++) {
                        returnBlocks.add(String.valueOf(currentNumber));
                    }
                }
            }
        } else {
            returnBlocks.add(match);
        }

        return returnBlocks;
    }
}
