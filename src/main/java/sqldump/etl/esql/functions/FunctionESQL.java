package sqldump.etl.esql.functions;

import sqldump.log.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bernhard Schlegel on 18.10.2016.
 */
public abstract class FunctionESQL {
    public static boolean testMatch(String potentialMatch) {
        return false;
    }

    public static List<String> execute(String match, Log log) {
        List<String> tmp = new ArrayList<String>();
        tmp.add(match);
        return tmp;
    }
}
