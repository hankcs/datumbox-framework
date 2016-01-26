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
package com.datumbox.common.persistentstorage.inmemory;

import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;
import java.util.Properties;

/**
 * 用来配置和创建内存数据连接，将所有数据加载到内存或序列化到文件<br>
 * The InMemoryConfiguration class is used to configure the InMemory persistence
 * storage and generate new storage connections. InMemory storage loads all the
 * data in memory and persists them in serialized files.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class InMemoryConfiguration implements DatabaseConfiguration {
    
    //Mandatory constants
    /**
     * 数据库名称分隔符
     */
    private static final String DBNAME_SEPARATOR = "_"; //NOT permitted characters are: <>:"/\|?*

    //DB specific properties
    /**
     * 默认的输出路径
     */
    private String outputFolder = "./";
    
    /**
     * 初始化到数据库的一个连接<br>
     * It initializes a new connector to the Database.
     * 
     * @param database
     * @return 
     */
    @Override
    public DatabaseConnector getConnector(String database) {
        return new InMemoryConnector(database, this);
    }
    
    /**
     * 获取分隔符<br>
     * Returns the separator that is used in the DB names.
     * 
     * @return 
     */
    @Override
    public String getDBnameSeparator() {
        return DBNAME_SEPARATOR;
    }
    
    /**
     * 通过一个配置文件初始化配置<br>
     * Initializes the InMemoryConfiguration object by using a property file.
     * 
     * @param properties 
     */
    @Override
    public void load(Properties properties) {
        outputFolder = properties.getProperty("dbConfig.InMemoryConfiguration.outputFolder");
    }
    
    /**
     * 获取输出文件夹<br>
     * Getter for the output folder where the InMemory data files are stored.
     * 
     * @return 
     */
    public String getOutputFolder() {
        return outputFolder;
    }
    
    /**
     * 设置输出文件夹<br>
     * Setter for the output folder where the InMemory data files are stored.
     * 
     * @param outputFolder 
     */
    public void setOutputFolder(String outputFolder) {
        this.outputFolder = outputFolder;
    }
}
