/**
 * Copyright (C) 2013-2016 Vasilis Vryniotis <bbriniotis@datumbox.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.datumbox.common.persistentstorage;

import com.datumbox.common.persistentstorage.inmemory.InMemoryConfiguration;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.persistentstorage.mapdb.MapDBConfiguration;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

/**
 * 通过配置文件生成配置对象的工厂类<br>
 * Factory that initializes and returns the DatabaseConfiguration based on the
 * configuration file. 
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public enum ConfigurationFactory {
    /**
     * 内存是默认的储存引擎，非常快，但必须考虑内存是否足够<br>
     * InMemory is the default storage engine, it is very fast and it should be used when the data fit the memory.
     */
    INMEMORY(InMemoryConfiguration.class),
    
    /**
     * MapDB is an alternative storage engine, which should be used when the data don't fit in memory.
     */
    MAPDB(MapDBConfiguration.class);
    
    private final Class<? extends DatabaseConfiguration> klass;
    
    /**
     * Private constructor for the enum.
     * 
     * @param klass 
     */
    private ConfigurationFactory(Class<? extends DatabaseConfiguration> klass) {
        this.klass = klass;
    }
    
    /**
     * 通过配置文件初始化配置对象<br>
     * Initializes the Configuration Object based on the config file.
     * 
     * @return 
     */
    public DatabaseConfiguration getConfiguration() {
        //初始化dbConfig对象
        //Initialize dbConfig object
        DatabaseConfiguration dbConf;
        try {
            dbConf = klass.getConstructor().newInstance();
        } 
        catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
            throw new RuntimeException(ex);
        }
        
        Properties properties = new Properties();
        
        ClassLoader cl = ConfigurationFactory.class.getClassLoader();

        //从jar中加载默认配置
        //Load default properties from jar
        try (InputStream in = cl.getResourceAsStream("datumbox.config.default.properties")) {
            properties.load(in);
        }
        catch(IOException ex) {
            throw new RuntimeException(ex);
        }

        //查找用户配置
        //Look for user defined properties
        if(cl.getResource("datumbox.config.properties")!=null) {
            //如果存在则覆盖默认的
            //Override the default if they exist
            try (InputStream in = cl.getResourceAsStream("datumbox.config.properties")) {
                properties.load(in);
            }
            catch(IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        
        dbConf.load(properties);
        
        return dbConf;
    }
}
