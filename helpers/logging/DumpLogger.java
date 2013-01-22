package helpers.logging;

import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.apache.log4j.PropertyConfigurator;

public class DumpLogger {
	private Logger logger;
	private StringBuilder out = new StringBuilder("\n");
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss,SSS");
    private DumpLogger(Class caller) {
        logger = Logger.getLogger(caller);
    }
    
    public static DumpLogger getLogger(Class caller) {
        return new DumpLogger(caller);
    }
    
    private String decodePriority(int p) {
        switch (p) {
        case Priority.DEBUG_INT: return "DEBUG";
        case Priority.INFO_INT : return "INFO ";
        case Priority.WARN_INT : return "WARN ";
        case Priority.ERROR_INT: return "ERROR";
        case Priority.FATAL_INT: return "FATAL";
        default: return null;
        }
    }
    
    public void log(Priority priority, Object message, Throwable t) {
    	if (priority.toInt() >= Logger.getRootLogger().getLevel().toInt()) {
    		logger.log(priority, message, t);
    	}
		out.append("[");
		out.append(sdf.format(new Date()));
		out.append("] ");
		out.append(decodePriority(priority.toInt()));
		out.append(" ");
		out.append(message);
		if (t != null) {
			out.append("\n");
			out.append(t.getClass().getCanonicalName());
			out.append(" ");
			out.append(t.getMessage());
			out.append("\n");
			StringWriter sw = null;
			StackTraceElement[] ste = t.getStackTrace();
			for (int i = 0; i < ste.length; i++) {
				sw = new StringWriter();
				sw.write("\tat ");
				sw.write(ste[i].getClassName());
				sw.write('.');
				sw.write(ste[i].getMethodName());
				sw.write('(');
				sw.write(ste[i].getFileName());
				sw.write(':');
				sw.write(""+ste[i].getLineNumber());
				sw.write(")\n");
				out.append(sw.getBuffer().toString());
			}
			if (t.getCause() != null) {
				out.append("Caused by: ");
				out.append(t.getCause().getClass().getCanonicalName());
				out.append(" ");
    			out.append(t.getCause().getMessage());
    			out.append("\n");
    			ste = t.getCause().getStackTrace();
    			for (int i = 0; i < ste.length; i++) {
    				sw = new StringWriter();
    				sw.write("\tat ");
    				sw.write(ste[i].getClassName());
    				sw.write('.');
    				sw.write(ste[i].getMethodName());
    				sw.write('(');
    				sw.write(ste[i].getFileName());
    				sw.write(':');
    				sw.write(""+ste[i].getLineNumber());
    				sw.write(")\n");
    				out.append(sw.getBuffer().toString());
    			}
			}
		} else {
			out.append("\n");
		}
    }
    
    public void debug(Object message) {
        log(Level.DEBUG, message, null);
    }
    public void debug(Object message, Throwable error) {
        log(Level.DEBUG, message, error);
    }
    public void info(Object message) {
        log(Level.INFO , message, null);
    }
    public void info(Object message, Throwable error) {
        log(Level.INFO , message, error);
    }
    public void warn(Object message) {
    	log(Level.WARN , message, null);
    }
    public void warn(Object message, Throwable error) {
    	log(Level.WARN , message, error);
    }
    public void error(Object message) {
    	log(Level.ERROR, message, null);
    }
    public void error(Object message, Throwable error) {
    	log(Level.ERROR, message, error);
    }
    public void fatal(Object message) {
    	log(Level.FATAL, message, null);
    }
    public void fatal(Object message, Throwable error) {
    	log(Level.FATAL, message, error);
    }
    
    public void dump() {
    	log(Level.FATAL, out.toString(), null);
    }
    
    private static Exception thrower1() {
    	return new Exception("from thrower1");
    }
    private static Exception thrower2() {
    	return new Exception("from thrower2", thrower1());
    }
    
    public static void main(String[] args) throws Exception {
    	PropertyConfigurator.configure(new Properties() {
    	private static final long serialVersionUID = 1L;
		{
    		setProperty("log4j.rootLogger", "ERROR, consoleApp");
    		setProperty("log4j.appender.consoleApp", "org.apache.log4j.ConsoleAppender");
    		setProperty("log4j.appender.consoleApp.Threshold", "ERROR");
    		setProperty("log4j.appender.consoleApp.layout", "org.apache.log4j.PatternLayout");
    		setProperty("log4j.appender.consoleApp.layout.ConversionPattern", "[%d] %-2p [%t] %c{3} - %m%n");
    	}});
    	DumpLogger log = DumpLogger.getLogger(DumpLogger.class);
    	log.debug("questo è debug");
    	log.error("questo è error");
    	log.debug("questo è debug con errore", thrower2());
    	
    	System.out.println("\n\n\n");
    	
    	log.dump();
    }
}
