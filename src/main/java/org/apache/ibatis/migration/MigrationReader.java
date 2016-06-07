/**
 *    Copyright 2010-2016 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.migration;

import org.apache.ibatis.migration.commands.BaseCommand;
import org.apache.ibatis.migration.utils.Util;
import org.apache.ibatis.parsing.PropertyParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class MigrationReader extends Reader {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator", "\n");

    private Reader target;

    public MigrationReader(File file, String charset, boolean undo, Properties properties) throws IOException {
      this(new FileInputStream(file), null, charset, undo, properties);
    }

    public MigrationReader(File file, File referencedFile, String charset, boolean undo, Properties properties) throws IOException {
        this(new FileInputStream(file), referencedFile, charset, undo, properties);
    }

    public MigrationReader(InputStream inputStream, File referencedFile, String charset,
                           boolean undo, Properties properties) throws IOException {
        final Reader source = scriptFileReader(inputStream, charset);
        final Properties variables = filterVariables(properties == null ? new Properties() : properties);
        try {
            BufferedReader reader = new BufferedReader(source);
            StringBuilder doBuilder = new StringBuilder();
            StringBuilder undoBuilder = new StringBuilder();
            StringBuilder currentBuilder = doBuilder;
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().matches("^--\\s*//.*$")) {
                    if (line.contains(BaseCommand.UNDO)) {
                        currentBuilder = undoBuilder;
                    }
                    line = line.replaceFirst("--\\s*//", "-- ");
                }
                if (line.contains(BaseCommand.FILE)) {
                    currentBuilder.append(getScriptFromFile(line.replace(BaseCommand.FILE, ""), referencedFile, charset));
                } else {
                    currentBuilder.append(line);
                    currentBuilder.append(LINE_SEPARATOR);
                }
            }
            if (undo) {
                target = new StringReader(PropertyParser.parse(undoBuilder.toString(), variables));
            } else {
                target = new StringReader(PropertyParser.parse(doBuilder.toString(), variables));
            }
        } finally {
            source.close();
        }
    }

    private String getScriptFromFile(String fileName, File referencedFileDir, String charset) throws IOException {
        StringBuilder outsideFile = new StringBuilder();
        Reader source = null;
        try {
            source = scriptFileReader(new FileInputStream(Util.file(referencedFileDir, fileName)), charset);
            BufferedReader reader = new BufferedReader(source);
            String line;
            while ((line = reader.readLine()) != null) {
                outsideFile.append(line);
                outsideFile.append(LINE_SEPARATOR);
            }
        } finally {
            if (source != null) {
                source.close();
            }
        }

        return outsideFile.toString();
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        return target.read(cbuf, off, len);
    }

    @Override
    public void close() throws IOException {
        target.close();
    }

    protected Reader scriptFileReader(InputStream inputStream, String charset) throws FileNotFoundException, UnsupportedEncodingException {
        if (charset == null || charset.length() == 0) {
            return new InputStreamReader(inputStream);
        } else {
            return new InputStreamReader(inputStream, charset);
        }
    }

    @SuppressWarnings("serial")
    private Properties filterVariables(final Properties properties) {
        final Set<String> KNOWN_PROPERTIES_TO_IGNORE = new HashSet<String>() {{
            addAll(Arrays.asList(
                    "time_zone", "script_char_set",
                    "driver", "url", "username", "password",
                    "send_full_script", "delimiter", "full_line_delimiter",
                    "auto_commit", "driver_path"));
        }};
        return new Properties() {
            @Override
            public synchronized boolean containsKey(Object o) {
                return !KNOWN_PROPERTIES_TO_IGNORE.contains(o) && properties.containsKey(o);
            }

            @Override
            public String getProperty(String key) {
                return KNOWN_PROPERTIES_TO_IGNORE.contains(key) ? null : properties.getProperty(key);
            }
        };
    }


}

