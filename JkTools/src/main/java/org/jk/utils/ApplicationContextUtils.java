package org.jk.utils;

import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

public class ApplicationContextUtils {
    private static volatile ApplicationContext applicationContext = null;
    private static volatile String projectName;

    public void setApplicationContext(ApplicationContext applicationContext){
        projectName = applicationContext.getBean(Environment.class).getProperty("spring.application.name");
        this.applicationContext = applicationContext;
    }

    public static ApplicationContext getApplicationContext(){
        return applicationContext;
    }

    public static String getProjectName(){
        return projectName;
    }

}
