package org.jk.utils;

import org.jk.core.ResourceManager;
import org.jk.core.ResourceManagerImpl;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

public class ApplicationContextUtils {
    private static volatile ApplicationContext applicationContext = null;
    private static volatile String projectName;
    private static volatile ResourceManager resourceManager;

    public void setApplicationContext(ApplicationContext applicationContext){
        projectName = applicationContext.getBean(Environment.class).getProperty("spring.application.name");
        resourceManager = (ResourceManager)applicationContext.getBean(ResourceManagerImpl.class);
        this.applicationContext = applicationContext;
    }

    public static ApplicationContext getApplicationContext(){
        return applicationContext;
    }

    public static String getProjectName(){
        return projectName;
    }

    public static ResourceManager getResourceManager() {
        return resourceManager;
    }
}
