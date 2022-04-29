package others;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LoggerUtil {



  public static void main(String[] args) {
    Logger logger = LogManager.getLogger();

    logger.trace("TraceMessage");
    logger.debug("DebugMessage");
    logger.info("InfoMessage {}!", "Info Test Log4j2");
    logger.warn("WarnMessage {}!", "Warn Test Log4j2");
    logger.error("ErrorMessage {}!", "Error Message");
    logger.fatal("FatalMessage");
    logger.info("Exception", new Exception("Error EX"));
  }
}
