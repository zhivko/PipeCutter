package com.kz.pipeCutter;

import java.io.PrintStream;

import org.apache.log4j.Logger;

//I set up a ConsoleAppender in Log4J to format Stdout/Stderr
	/*
log4j.rootLogger=DEBUG, CONSOLE
log4j.appender.CONSOLE=org.apache.log4j.ConsoleAppender
log4j.appender.CONSOLE.layout=org.apache.log4j.PatternLayout
log4j.appender.CONSOLE.layout.ConversionPattern=[%t] %-5p %c - %m%n
*/

//And I call this StdOutErrLog.tieSystemOutAndErrToLog() on startup

public class StdOutErrLog {

   private static final Logger logger = Logger.getLogger(StdOutErrLog.class);

   public static void tieSystemOutAndErrToLog() {
       System.setOut(createLoggingProxy(System.out));
       System.setErr(createLoggingProxy(System.err));
   }

   public static PrintStream createLoggingProxy(final PrintStream realPrintStream) {
       return new PrintStream(realPrintStream) {
           public void print(final String string) {
               realPrintStream.println(string);
               logger.info(string);
           }
       };
   }
}
