package org.apache.ibatis.migration.options;

public final class OptionsParser {
    public static SelectedOptions parse(String[] args) {
        final SelectedOptions selectedOptions = new SelectedOptions();

        for (String arg : args) {
            boolean parsableArg = Options.parse(arg, selectedOptions);
            if (!parsableArg) {
                setCommandOrAppendParams(arg, selectedOptions);
            }
        }

        return selectedOptions;
    }

    private static void setCommandOrAppendParams(String arg, SelectedOptions options) {
        if (options.getCommand() == null) {
            options.setCommand(arg);
        } else {
            final String myParams = options.getParams() == null ? arg : options.getParams() + " " + arg;
            options.setParams(myParams);
        }
    }
}
