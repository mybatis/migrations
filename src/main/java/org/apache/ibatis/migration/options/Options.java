package org.apache.ibatis.migration.options;

import java.io.File;

public enum Options {
    PATH,
    ENV,
    FORCE,
    TRACE,
    HELP,
    TEMPLATE;

    public static boolean parse(String arg, SelectedOptions aSelectedOptions) {
        final boolean isOption = isOption(arg);

        if (isOption) {
            final String[] argParts = arg.substring(2).split("=");
            final Options option = valueOf(argParts[0].toUpperCase());

            switch (option) {
                case PATH:
                    aSelectedOptions.setRepository(new File(argParts[1]));
                    break;
                case ENV:
                    aSelectedOptions.setEnvironment(argParts[1]);
                    break;
                case FORCE:
                    aSelectedOptions.setForce(true);
                    break;
                case TRACE:
                    aSelectedOptions.setTrace(true);
                    break;
                case HELP:
                    aSelectedOptions.setHelp(true);
                    break;
                case TEMPLATE:
                    aSelectedOptions.setTemplate(argParts[1]);
                    break;
            }
        }

        return isOption;
    }

    private static boolean isOption(String arg) {
        return arg.startsWith("--") && !arg.trim().endsWith("=");
    }
}
