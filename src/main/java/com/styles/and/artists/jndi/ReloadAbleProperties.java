package com.styles.and.artists.jndi;

import lombok.experimental.Delegate;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.Hashtable;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

public class ReloadAbleProperties extends Properties {
    private static final long serialVersionUID = 1L;
    @Delegate
    private Properties properties = new Properties();
    private String propertiesFileName;
    private long watchInterval = 5000;
    private long maxNumberOfWatches = -1;

    public ReloadAbleProperties(String propertiesFileName) {
        this(propertiesFileName, 5000,  -1);
    }

    public ReloadAbleProperties(String propertiesFileName, long watchInterval, long maxNumberOfWatches) {
        this.propertiesFileName = propertiesFileName;
        this.watchInterval = watchInterval;
        this.maxNumberOfWatches = maxNumberOfWatches;
    }

    public synchronized void load() throws IOException {
        Path propertiesFile = Paths.get(propertiesFileName);

        loadProperties(propertiesFile);
        try {
            initializeWatcher(propertiesFile);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private synchronized void loadProperties(Path propertiesFile) throws IOException {
        if(Files.exists(propertiesFile) && Files.isRegularFile(propertiesFile) && Files.isReadable(propertiesFile)) {
            StringReader reader = new StringReader(Files.readString(propertiesFile, Charset.defaultCharset()));
            properties.load(reader);
        }
         else {
            throw new RuntimeException("properties file does not exist or is not readable or not a reqular file");
        }
    }


    private void initializeWatcher(Path propertiesFile) throws IOException, InterruptedException {
        FileSystem fs = propertiesFile.getParent().getFileSystem();

        WatchService service = fs.newWatchService();
        propertiesFile.getParent().register(service, ENTRY_MODIFY);

        // Start the infinite polling loop
        CompletableFuture.runAsync(() -> {
            while (maxNumberOfWatches != 0) {
                try {
                    Thread.sleep(watchInterval);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                try {
                    for (WatchEvent<?> watchEvent : service.take().pollEvents()) {
                        WatchEvent<Path> ev = (WatchEvent<Path>) watchEvent;
                        Path evFile = ev.context();

                        if (evFile.toAbsolutePath().getFileName().compareTo(propertiesFile.toAbsolutePath().getFileName())
                        == 0 && ENTRY_MODIFY == watchEvent.kind()) {
                            try {
                                loadProperties(propertiesFile);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if(maxNumberOfWatches != -1) {
                    maxNumberOfWatches--;
                }
            }
        });
    }
}
