package sqldump.log;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Class used for logging
 */
public class Log {

	/**
	 * Enum holding the different logging levels.
	 */
    public enum LogLevel {
		/**
		 * Error, most critical. Used e.g. for exceptions
		 */
		Error("ERR"),

		/**
		 * Warning, less critical than ERR. But still negatively affects
		 * programm execution
		 */
		Warning("WRN"),

		/**
		 * Information to keep track what the programm is currently up to.
		 */
		Info("INF"),

		/**
		 * Detailled output. Should only be used for debugging.
		 */
		Verbose("VRB"),

		/**
		 * Very detaillled output. Should not be used in production because the log
		 * will grow to large in size
		 */
		Ultrose("ULT");
		
		private String str;

		LogLevel(String setup) {
	        this.str = setup;
	    }
		
		public String getName(){
			return this.str;
		}
		
		public Boolean isHigherOrEqual(LogLevel check) {
			
			switch (check) {
				case Error:
					if (this == LogLevel.Error || this == LogLevel.Warning || this == LogLevel.Info || this == LogLevel.Verbose || this == LogLevel.Ultrose) {
						return true;
					}					
					break;
				case Warning:
					if (this == LogLevel.Warning || this == LogLevel.Info || this == LogLevel.Verbose || this == LogLevel.Ultrose) {
						return true;
					}
					break;
				case Info:
					if (this == LogLevel.Info || this == LogLevel.Verbose || this == LogLevel.Ultrose) {
						return true;
					}
					break;
				case Verbose:
					if (this == LogLevel.Verbose || this == LogLevel.Ultrose) {
						return true;
					}
					break;
				case Ultrose:
					if (this == LogLevel.Ultrose) {
						return true;
					}
					break;
			}
			
			return false;
		}
	}

	/**
	 * Log target
	 */
	public enum LogTarget {
		/**
		 * log to both, console and file.
		 */
		BOTH,

		/**
		 * log only to console
		 */
		CONSOLE,

		/**
		 * log only to file
		 */
		FILE
	}

	/**
	 * UUID of the last logged text. This member is used for tracking if another text (B) is outputted
	 * in the meantime while the process for text (A) still runs. If so, the text of A will be outputted
	 * again before printing the duration.
	 */
	private UUID lastLogged;

	/**
	 * Logging level for the console. Any message with a criticality higher or equal will be logged.
	 */
	private static LogLevel levelConsole = LogLevel.Verbose;

	/**
	 * See levelConsole - same but for the file.
	 */
	private static LogLevel levelFile = LogLevel.Info;

	/**
	 * Specifies, where the log output is put
	 */
	private static LogTarget target = LogTarget.BOTH;

	/**
	 * Formatting used for outputting dates and times to console and file
	 */
	private static DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

	/**
	 * attemps to log txt to file.
	 * @param lvl the criticality of the logged text
	 * @param txt the texted to be loggged
	 * @param showDate Show the date in the output
	 */
	private void logToFile(LogLevel lvl, String txt, Boolean showDate) {
		try {
			if (levelFile.isHigherOrEqual(lvl)) {
				Calendar cal = Calendar.getInstance();
				String logString = "";
				
				if (showDate) {
					logString = "\n" + dateFormat.format(cal.getTime()) + "," + lvl.getName() + "," + txt;
				}
				else {
					logString = "" + txt + "\r\n";
				}

				File yourFile = new File("log.txt");
				if(!yourFile.exists()) {
				    yourFile.createNewFile();
				} 
			    Files.write(Paths.get("log.txt"), logString.getBytes(), StandardOpenOption.APPEND);
			}
			
		}catch (IOException e) {
		    //exception handling left as an exercise for the reader
			Integer i = 123;
			i = i +1;
		}
	}

	/**
	 * attemps to log txt to the console.
	 * @param lvl the criticality of the logged text
	 * @param txt the texted to be loggged
	 * @param showDate Show the date in the output
	 */
	private void logToConsole(LogLevel lvl, String txt, Boolean showDate) {
		if (levelConsole.isHigherOrEqual(lvl)) {
			Calendar cal = Calendar.getInstance();
			if (showDate) {
				System.out.print("\n" + dateFormat.format(cal.getTime()) + " [" + lvl.getName() + "]: " + txt);
			}
			else {
				System.out.print("" + txt);
			}
		}
	}

	/**
	 * attemps to log txt to both, console and file
	 * @param lvl the criticality of the logged text
	 * @param txt the texted to be loggged
	 * @param showDate Show the date in the output
	 */
	private PingPong logToBoth(LogLevel lvl, String txt, Boolean showDate) {
		if (target == LogTarget.BOTH || target == LogTarget.CONSOLE) {
			logToConsole(lvl,txt,showDate);
		}
		if (target == LogTarget.BOTH || target == LogTarget.FILE) {
			logToFile(lvl, txt, showDate);
		}

        PingPong pp = new PingPong(txt, lvl);
        lastLogged = pp.guid;
        return pp;
	}

    /**
     * Get a diff between two dates
     *
     * @param date1    the oldest date
     * @param date2    the newest date
     * @param timeUnit the unit in which you want the diff
     * @return the diff value, in the provided unit
     */
    public Long getDateDiff(java.util.Date date1, java.util.Date date2, TimeUnit timeUnit) {
        Long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

	/**
	 * Logs the time difference between the captured ping object and the current time
	 * @param ping the PingPong object, marking the start of the process
	 */
	public void finished(PingPong ping ) {
        java.util.Date now = java.util.Calendar.getInstance().getTime();

        Long millis = getDateDiff(ping.old, now, java.util.concurrent.TimeUnit.MILLISECONDS);

        String durationString = String.format("%02d:%02d:%02d.%03d",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)),
                TimeUnit.MILLISECONDS.toMillis(millis) - TimeUnit.SECONDS.toMillis(TimeUnit.MILLISECONDS.toSeconds(millis))
        );

        if(ping.guid != lastLogged) {
            // if there has been different log outputs in the meantime,
            // log the old message again
            this.logToBoth(ping.level, ping.text + " took " + durationString, true);
        }
        else {
            this.logToBoth(ping.level, " took " + durationString, false);
        }
    }

	/**
	 * Logs an error
	 * @param str string to be logged
	 */
	public void error(String str) {
		logToBoth(LogLevel.Error,str,true);
	}

	/**
	 * Logs a warning
	 * @param str string to be logged
	 */
	public void warning(String str) {
		logToBoth(LogLevel.Warning,str,true);	
	}

	/**
	 * Logs an information
	 * @param str string to be logged
	 * @return a PingPong object, used for duration tracking
	 */
	public PingPong info(String str) {
		return logToBoth(LogLevel.Info,str,true);
	}

	/**
	 * Does not print date, just appends to log
	 * @param str string to be logged
	 */
    public void infoAppend(String str) {
        logToBoth(LogLevel.Info,str,false);
    }

    public void infoIdent(String str) {
        logToBoth(LogLevel.Info,"\n                        " + str,false);
    }
	
	public void verbose(String str) {
		logToBoth(LogLevel.Verbose,str,true);			
	}
	
	/**
	 * Logging, so detailled that its barely readable
	 * @param str
	 */
	public void ultrose(String str) {
		logToBoth(LogLevel.Ultrose,str,true);				
	}

	/**
	 * loggs the logo
	 */
	public void logLogo() {
        System.out.println("welcome to");
		System.out.println(
		        "   ________    __     ___                         \n" +
				"  / __/ __ \\  / /    / _ \\__ ____ _  ___  ___ ____\n" +
				" _\\ \\/ /_/ / / /__  / // / // /  ' \\/ _ \\/ -_) __/\n" +
				"/___/\\___\\_\\/____/ /____/\\_,_/_/_/_/ .__/\\__/_/   \n" +
				"                                  /_/              v.0.0.1");
		System.out.print("(c) by Bernhard Schlegel, 2016, github.com/BernhardSchlegel");
	}
}
