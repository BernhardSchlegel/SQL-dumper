package sqldump.etl.esql;

import sqldump.etl.esql.functions.*;
import sqldump.log.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bernhard Schlegel on 18.10.2016.
 */
public class ESQL {
    /**
     * Applies a ESQL function to the given text
     * @param text Text to be transformed. Mandatory format is
     *             $fn_name(parameters)
     * @param log
     * @return
     */
    public static List<String> applyESQLFunction(String text, Log log) {
        if(ESQLspreadNum.testMatch(text)) {
            log.info("esql type spreadNum detected (" + text + ")");
            return ESQLspreadNum.execute(text, log);
        }
        else if(ESQLspreadNumArea.testMatch(text)) {
            log.info("esql type spreadNumArea detected (" + text + ")");
            return ESQLspreadNumArea.execute(text, log);
        }
        else if(ESQLText.testMatch(text)) {
            log.info("esql type text detected (" + text + ")");
            return ESQLText.execute(text, log);
        }
        else if(ESQLTextArray.testMatch(text)) {
            log.info("esql type text array detected (" + text + ")");
            return ESQLTextArray.execute(text, log);
        }
        else if(ESQLNumArray.testMatch(text)) {
            log.info("esql type num array detected (" + text + ")");
            return ESQLNumArray.execute(text, log);
        }
        else {
            log.info("no esql detected (" + text + "), aborting...");
            List<String> tmp = new ArrayList<String>();
            tmp.add(text);
            return tmp;
        }
    }

    public static Boolean containsESQL(String query) {
        return query.contains("$ESQL_");
    }
}
