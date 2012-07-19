package org.apache.ibatis.migration.options;

import java.io.File;

import static org.apache.ibatis.migration.utils.Util.file;

public class SelectedPaths {
    private File basePath = new File("./");
    private File envPath;
    private File scriptPath;
    private File driverPath;

    public File getBasePath() {
        return basePath;
    }

    public File getEnvPath() {
        return envPath == null ? file(basePath, "./environments") : envPath;
    }

    public File getScriptPath() {
        return scriptPath == null ? file(basePath, "./scripts") : scriptPath;
    }

    public File getDriverPath() {
        return driverPath == null ? file(basePath, "./drivers") : driverPath;
    }

    public void setBasePath(File aBasePath) {
        basePath = aBasePath;
    }

    public void setEnvPath(File aEnvPath) {
        envPath = aEnvPath;
    }

    public void setScriptPath(File aScriptPath) {
        scriptPath = aScriptPath;
    }

    public void setDriverPath(File aDriverPath) {
        driverPath = aDriverPath;
    }
}
