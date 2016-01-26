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

import com.datumbox.common.persistentstorage.AutoCloseConnector;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import com.datumbox.common.utilities.DeepCopy;
import java.io.Serializable;
import java.util.HashMap;


/**
 * 用来在内存中保存和加载数据、创建BigMaps并且储存数据<br>
 * The InMemoryConnector is responsible for saving and loading data in memory,
 * creating BigMaps and persisting data. The InMemoryConnector loads all the
 * data in memory and persists all data in serialized files.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class InMemoryConnector extends AutoCloseConnector {
        
    private final InMemoryConfiguration dbConf;
    private final String database;
    
    /**
     * 创建连接<br>
     * Non-public constructor used by InMemoryConfiguration class to generate
     * new connections.
     * 
     * @param database
     * @param dbConf 
     */
    protected InMemoryConnector(String database, InMemoryConfiguration dbConf) {  
        super();
        this.dbConf = dbConf;
        this.database = database;
    }
    
    /**
     * 负责将可序列化对象存入数据库<br>
     * This method is responsible for storing serializable objects in the
     * database.
     * 
     * @param <T>
     * @param name
     * @param serializableObject 
     */
    @Override
    public <T extends Serializable> void save(String name, T serializableObject) {
        ensureNotClosed();
        try { 
            Files.write(getDefaultPath(), DeepCopy.serialize(serializableObject));
        } 
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Loads serializable objects from the database.
     * 
     * @param <T>
     * @param name
     * @param klass
     * @return 
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends Serializable> T load(String name, Class<T> klass) {
        ensureNotClosed();
        try { 
            //read the stored serialized object
            T serializableObject = (T)DeepCopy.deserialize(Files.readAllBytes(getDefaultPath()));
            return serializableObject;
        } 
        catch (NoSuchFileException ex) {
            return null;
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * Closes the connection and clean ups the resources.
     */
    @Override
    public void close() {
        if(isClosed()){
            return; 
        }
        super.close();
    }
    
    /**
     * Checks if a particular database exists.
     * 
     * @return 
     */
    @Override
    public boolean existsDatabase() {
        ensureNotClosed();
        return Files.exists(getDefaultPath());
    }
    
    /**
     * Drops the particular database.
     */
    @Override
    public void dropDatabase() {
        ensureNotClosed();
        if(!existsDatabase()) {
            return;
        }
        
        try {
            Files.deleteIfExists(getDefaultPath());
        } 
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * Creates or loads a Big Map which is capable of storing large number of 
     * records. 
     * 
     * @param <K>
     * @param <V>
     * @param name
     * @param isTemporary
     * @return 
     */
    @Override
    public <K,V> Map<K,V> getBigMap(String name, boolean isTemporary) {
        ensureNotClosed();
        return new HashMap<>();
    }  
    
    /**
     * Drops a particular Big Map.
     * 
     * @param <T>
     * @param name
     * @param map 
     */
    @Override
    public <T extends Map> void dropBigMap(String name, T map) {
        ensureNotClosed();
        map.clear();
    } 

    private Path getDefaultPath() {
        //get the default filepath of the permanet db file
        String outputFolder = this.dbConf.getOutputFolder();
        
        Path filepath = null;
        if(outputFolder == null || outputFolder.isEmpty()) {
            filepath= FileSystems.getDefault().getPath(database); //write them to the default accessible path
        }
        else {
            filepath= Paths.get(outputFolder + File.separator + database);
        }
        
        return filepath;
    }
}
