package com.styles.and.artists.jndi;

import lombok.experimental.Delegate;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Hashtable;
import java.util.Properties;

public class ReloadAbleProperties extends Properties {
    private static final long serialVersionUID = 1L;
    @Delegate
    private Properties properties = new Properties();
    private String propertiesFileName;

    public ReloadAbleProperties(String propertiesFileName) {
        this.propertiesFileName = propertiesFileName;
    }

    @Override
    public Object get(Object key) {
        try {
            load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return properties.get(key);
    }

    public synchronized void load() throws IOException {
        Path propertiesFile = Paths.get(propertiesFileName);

        if(Files.exists(propertiesFile) && Files.isRegularFile(propertiesFile) && Files.isReadable(propertiesFile)) {
            StringReader reader = new StringReader(Files.readString(propertiesFile, Charset.defaultCharset()));
            properties.load(reader);
        }
        else {
            throw new RuntimeException("properties file does not exist or is not readable or not a reqular file");
        }
    }
}
