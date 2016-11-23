package sqldump.etl.esql.functions;

import sqldump.log.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ESQLText function returns the text, syntax e.g.
 * $text(STR), where
 *      STR: string to be outputted, encapsulated in "mytext"
 * e.g. $ESQL_text("AND mw.WERT_CODE = ")
 */
public class ESQLText extends FunctionESQL {
    public static boolean testMatch(String potentialMatch) {
        return Pattern.matches("\\s*\\$ESQL_text\\(.*\\)\\s*", potentialMatch);
    }

    public static List<String> execute(String match, Log log) {

        List<String> returnBlocks = new ArrayList<String>();
        if(ESQLText.testMatch(match)) {
            Pattern p = Pattern.compile("\\$ESQL_text\\(\"(.*?)\"\\)");
            Matcher m = p.matcher(match);

            if (m.find()) {
                for (Integer i = 0; i < m.groupCount(); i++) {
                    String block = m.group(1 + i);

                    // text will just add output the text
                    returnBlocks.add(block);
                }
            }
        } else {
            returnBlocks.add(match);
        }

        return returnBlocks;
    }
}
