package com.borsche.signalstrength;

import java.io.File;

import org.apache.log4j.Level;
import android.os.Environment;
import de.mindpipe.android.logging.log4j.LogConfigurator;
/**
 * Simply place a class like this in your Android applications classpath.
 */
public class ConfigureLog4j {
	
    public ConfigureLog4j() {
        final LogConfigurator logConfigurator = new LogConfigurator();
        logConfigurator.setUseLogCatAppender(true);
        
        String appLogPathDir = Environment.getExternalStorageDirectory() + "/QoeImprovment/";
        File destDir = new File(appLogPathDir);
        if (!destDir.exists()) {
         destDir.mkdirs();
        }
        
        String appLogName = "QoeImprovment.log"; // + (new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")).format(new Date()) + ".log";
        String appLogPath = appLogPathDir + appLogName;
        logConfigurator.setFileName(appLogPath);
        logConfigurator.setRootLevel(Level.DEBUG);
        logConfigurator.configure();
    }
}