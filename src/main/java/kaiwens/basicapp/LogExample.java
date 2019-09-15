package kaiwens.basicapp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogExample {
    private static void exampleLog4j2Level() {
        Log log = LogFactory.getLog("com.amazonaws");
        log.debug("DEBUG!");
        log.info("INFO");
        log.warn("WARN");
        log.error("ERROR");
    }

    public static void exampleSlf4j() {
        log.info("using Slf4j log.");
    }
}
