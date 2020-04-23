package com.epam.healenium.extension;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

public class ReportHelper {
    private static final Logger logger = Logger.getLogger(ReportHelper.class.getName());

    public static Properties loadConfig(File file) {
        Properties configProperty = new Properties();
        if (file.length() > 0) {
            try {
                FileInputStream fileIn = new FileInputStream(file);
                configProperty.load(fileIn);
                fileIn.close();
            } catch (IOException ex) {
                logger.warning("Fail to read config file " + ex);
            }
        }
        return configProperty;
    }
}
