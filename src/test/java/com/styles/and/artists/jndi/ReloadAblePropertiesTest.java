package com.styles.and.artists.jndi;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

class ReloadAblePropertiesTest {
    @Test
    void load1() throws Exception {
        URL testFile = this.getClass().getClassLoader().getResource("test1.properties");
        ReloadAbleProperties underTest = new ReloadAbleProperties(Paths.get(testFile.toURI()).toAbsolutePath().toString());

        underTest.load();

        Assertions.assertEquals("value1" , underTest.get("key1"));
        Assertions.assertEquals("value2" , underTest.get("key2"));
        Assertions.assertEquals("valuePrefix=valuePostfix" , underTest.get("key3"));
    }
    @Test
    void load2() throws Exception {
        File file = Files.createTempFile("test", ".properties").toFile();FileWriter writer = new FileWriter(file);

        writer.write("key1=value1\n");
        writer.write("key2=value2\n");
        writer.write("key3=value3\n");

        writer.close();

        ReloadAbleProperties underTest = new ReloadAbleProperties(file.getAbsolutePath(),30, 10);

        underTest.load();

        Assertions.assertEquals("value1" , underTest.get("key1"));
        Assertions.assertEquals("value2" , underTest.get("key2"));
        Assertions.assertEquals("value3" , underTest.get("key3"));

        writer = new FileWriter(file);

        writer.write("key1=value1a\n");
        writer.write("key2=value2a\n");
        writer.write("key3=value3a\n");

        writer.close();

        Thread.sleep(3000);

        Assertions.assertEquals("value1a" , underTest.get("key1"));
        Assertions.assertEquals("value2a" , underTest.get("key2"));
        Assertions.assertEquals("value3a" , underTest.get("key3"));
    }
}