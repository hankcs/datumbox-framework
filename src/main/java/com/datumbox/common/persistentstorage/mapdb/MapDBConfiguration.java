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
package com.datumbox.common.persistentstorage.mapdb;

import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;
import java.util.Properties;

/**
 * The MapDBConfiguration class is used to configure the MapDB persistence
 * storage and generate new storage connections. MapDB storage uses collections 
 * which are backed by file and thus it does not load all the data in memory. 
 * The data are persisted in MapDB files.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class MapDBConfiguration implements DatabaseConfiguration {
    
    //Mandatory constants
    private static final String DBNAME_SEPARATOR = "_"; //NOT permitted characters are: <>:"/\|?*

    //DB specific properties
    private String outputFolder = "./";
    
    private int cacheSize = 10000;
    
    private boolean compressed = true;
    
    private boolean transacted = false;

    /**
     * It initializes a new connector to the Database.
     * 
     * @param database
     * @return 
     */
    @Override
    public DatabaseConnector getConnector(String database) {
        return new MapDBConnector(database, this);
    }
    
    /**
     * Returns the separator that is used in the DB names.
     * 
     * @return 
     */
    @Override
    public String getDBnameSeparator() {
        return DBNAME_SEPARATOR;
    }
    
    /**
     * Initializes the MapDBConfiguration object by using a property file.
     * 
     * @param properties 
     */
    @Override
    public void load(Properties properties) {
        outputFolder = properties.getProperty("dbConfig.MapDBConfiguration.outputFolder");
        cacheSize = Integer.valueOf(properties.getProperty("dbConfig.MapDBConfiguration.cacheSize"));
        compressed = "true".equalsIgnoreCase(properties.getProperty("dbConfig.MapDBConfiguration.compressed"));
        transacted = "true".equalsIgnoreCase(properties.getProperty("dbConfig.MapDBConfiguration.transacted"));
    }

    /**
     * Getter for the output folder where the MapDB data files are stored.
     * 
     * @return 
     */
    public String getOutputFolder() {
        return outputFolder;
    }

    /**
     * Setter for the output folder where the MapDB data files are stored.
     * 
     * @param outputFolder 
     */
    public void setOutputFolder(String outputFolder) {
        this.outputFolder = outputFolder;
    }
    
    /**
     * Getter for the size of items stored in the LRU cache by MapDB.
     * 
     * @return 
     */
    public int getCacheSize() {
        return cacheSize;
    }
    
    /**
     * Setter for the size of items stored in LRU cache by MapDB. Set it to 0 to
     * turn off caching.
     * 
     * @param cacheSize 
     */
    public void setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
    }

    /**
     * Getter for the compression option.
     * 
     * @return 
     */
    public boolean isCompressed() {
        return compressed;
    }
    
    /**
     * Setter for the compression option. If turned on the records will be compressed.
     * It is turned on by default.
     * 
     * @param compressed 
     */
    public void setCompressed(boolean compressed) {
        this.compressed = compressed;
    }
    
    /**
     * Getter for the transaction support option.
     * 
     * @return 
     */
    public boolean isTransacted() {
        return transacted;
    }

    /**
     * Setter for the transaction support option. Turning off transactions can
     * speed up significantly the algorithms nevertheless it could potentially
     * lead to corrupted MapDB files. Transactions are disabled by default.
     * 
     * @param transacted 
     */
    public void setTransacted(boolean transacted) {
        this.transacted = transacted;
    }
    
    
}
