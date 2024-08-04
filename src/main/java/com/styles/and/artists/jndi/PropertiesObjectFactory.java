package com.styles.and.artists.jndi;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;
import java.util.Hashtable;

public class PropertiesObjectFactory implements ObjectFactory {
    final static private String fileLocationProperty = "fileLocation";

    private ReloadAbleProperties properties = null;

    @Override
    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) throws Exception {
        if(properties == null) {
            properties = new ReloadAbleProperties(getPropertiesFileLocationFromEnvironment(environment));
            properties.load();
        }

        return properties;
    }

    private String getPropertiesFileLocationFromEnvironment(Hashtable<?, ?> environment) {
        if(environment.containsKey(fileLocationProperty)) {
            return (String) environment.get(fileLocationProperty);
        }
        else {
            throw new RuntimeException("property 'fileLocation' not defined as environment variable");
        }
    }
}
