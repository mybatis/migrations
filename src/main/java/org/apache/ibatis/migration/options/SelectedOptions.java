package org.apache.ibatis.migration.options;

import java.io.File;

public class SelectedOptions {
    private File repository = new File("./");
    private String environment = "development";
    private String template;
    private boolean force;
    private boolean trace;
    private String command;
    private String params;
    private String parseError;
    private boolean help;

    public File getRepository() {
        return repository;
    }

    public void setRepository(File aRepository) {
        repository = aRepository;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String aEnvironment) {
        environment = aEnvironment;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String aTemplate) {
        template = aTemplate;
    }

    public boolean isForce() {
        return force;
    }

    public void setForce(boolean aForce) {
        force = aForce;
    }

    public boolean isTrace() {
        return trace;
    }

    public void setTrace(boolean aTrace) {
        trace = aTrace;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String aCommand) {
        command = aCommand;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String aParams) {
        params = aParams;
    }

    public String getParseError() {
        return parseError;
    }

    public void setParseError(String aParseError) {
        parseError = aParseError;
    }

    public boolean needsHelp() {
        return help;
    }

    public void setHelp(boolean aHelp) {
        help = aHelp;
    }
}
