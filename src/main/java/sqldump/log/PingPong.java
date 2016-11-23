package sqldump.log;

import sqldump.log.Log.LogLevel;

/**
 * Class used for tracking duration
 */
public class PingPong {

    /**
     * A GUID to track which text was printed last by the logging class
     */
    public java.util.UUID guid = this.guid();

    /**
     * The last text text printed.
     */
    public String text;

    /**
     * The date, where the tracked process started (the "ping")
     */
    public java.util.Date old = java.util.Calendar.getInstance().getTime();

    /**
     * the log level of the tracked process
     */
    public LogLevel level;

    /**
     * Default, overloaded constructor
     * @param txt The output of the logging call
     * @param lvl the level of the tracked process
     */
    public PingPong(String txt, LogLevel lvl) {
        this.text = txt;
        this.level = lvl;
    }

    /**
     * Private function for generating randum GUIDs
     * @return a new, randomly generated GUID
     */
    private java.util.UUID guid() {
        return java.util.UUID.randomUUID();
    }
}
