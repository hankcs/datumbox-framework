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
package com.datumbox.framework.machinelearning.common.dataobjects;

import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;
import com.datumbox.framework.machinelearning.common.bases.baseobjects.BaseModelParameters;
import com.datumbox.framework.machinelearning.common.bases.baseobjects.BaseTrainingParameters;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;


/**
 * 知识库，或称“数据库”，是通过算法学习到的数据的集合。同时还是两个类的wrapper：模型参数和
 * 训练参数。
 * The KnowledgeBase represents the "database" that the algorithm learned during  
 * training. It is a wrapper of the 2 classes: the model parameters and the 
 * training parameters.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <MP>
 * @param <TP>
 */
public class KnowledgeBase<MP extends BaseModelParameters, TP extends BaseTrainingParameters> implements Serializable {
    /**
     * 数据库名称<br>
     * The name of the database which is used by the Database Connector.
     */
    protected String dbName; 
    
    /**
     * 链接到持久化工具的连接<br>
     * Connection to the Permanent Storage Engine driver.
     */
    protected transient DatabaseConnector dbc;
    
    /**
     * 持久化工具的配置<br>
     * The database configuration of the permanent storage.
     */
    protected transient DatabaseConfiguration dbConf;
    
    /**
     * 模型参数类<br>
     * The class of the ModelParameters class of the algorithm.
     */
    protected Class<MP> mpClass;
    
    /**
     * 训练参数类<br>
     * The class of the TrainingParameters class of the algorithm.
     */
    protected Class<TP> tpClass;
    
    /**
     * 模型参数对象<br>
     * The ModelParameters object of the algorithm.
     */
    protected MP modelParameters;
    
    /**
     * 训练参数对象<br>
     * The TrainingParameters object of the algorithm.
     */
    protected TP trainingParameters;
    
    /**
     * 公开构造
     * Public constructor of the object.
     * 
     * @param dbName
     * @param dbConf 
     * @param mpClass 
     * @param tpClass 
     */
    public KnowledgeBase(String dbName, DatabaseConfiguration dbConf, Class<MP> mpClass, Class<TP> tpClass) {
        this.dbName = dbName;
        this.dbConf = dbConf;
        
        dbc = dbConf.getConnector(dbName);
        
        this.mpClass = mpClass;
        this.tpClass = tpClass;
    }
    
    /**
     * 获取数据库连接<br>
     * Getter for the Database Connector.
     * 
     * @return 
     */
    public DatabaseConnector getDbc() {
        return dbc;
    }
    
    /**
     * 获取数据库配置<br>
     * Getter for the Database Configuration.
     * 
     * @return 
     */
    public DatabaseConfiguration getDbConf() {
        return dbConf;
    }

    /**
     * 将数据库保存到持久化库中<br>
     * Saves a KnowledgeBase to the permanent storage.
     */
    public void save() {
        if(modelParameters==null) {
            throw new IllegalArgumentException("Can not store an empty KnowledgeBase.");
        }
        
        dbc.save("KnowledgeBase", this);
    }
    
    /**
     * 从持久化仓库中加载一个数据库<br>
     * Loads a KnowledgeBase from the permanent storage.
     */
    public void load() {
        if(modelParameters==null) {
            KnowledgeBase kbObject = dbc.load("KnowledgeBase", this.getClass());
            if(kbObject==null) {
                throw new IllegalArgumentException("The KnowledgeBase could not be loaded.");
            }
            
            trainingParameters = (TP) kbObject.trainingParameters;
            modelParameters = (MP) kbObject.modelParameters;
        }
    }
    
    /**
     * 删除数据库<br>
     * Deletes the database of the algorithm. 
     */
    public void erase() {
    	dbc.dropDatabase();
        dbc.close();
        
        modelParameters = null;
        trainingParameters = null;
    }
    
    /**
     * 关闭所有资源<br>
     * Closes all the resources of the algorithm. 
     */
    public void close() {
        dbc.close();
    }
    
    /**
     * 重新打开，会删除上次的数据<br>
     * Deletes and re-initializes KnowledgeBase object. It erases all data from 
     * storage, it releases all resources, reinitializes the internal objects and
     * opens new connection to the permanent storage.
     */
    public void reinitialize() {
        erase();
        dbc = dbConf.getConnector(dbName); //re-open connector
        
        try {
            Constructor<MP> c = mpClass.getDeclaredConstructor(DatabaseConnector.class);
            c.setAccessible(true);
            modelParameters = c.newInstance(dbc);
            trainingParameters = tpClass.getConstructor().newInstance();
        } 
        catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
            throw new RuntimeException(ex);
        }
        
    }

    /**
     * 获取训练参数<br>
     * Getter for the Training Parameters.
     * 
     * @return 
     */
    public TP getTrainingParameters() {
        return trainingParameters;
    }

    /**
     * 设置训练参数<br>
     * Setter for the Training Parameters.
     * 
     * @param trainingParameters 
     */
    public void setTrainingParameters(TP trainingParameters) {
        this.trainingParameters = trainingParameters;
    }

    /**
     * 获取模型参数<br>
     * Getter for the Model Parameters.
     * 
     * @return 
     */
    public MP getModelParameters() {
        return modelParameters;
    }
    
    /**
     * 设置模型参数<br>
     * Setter for the Model Parameters.
     * 
     * @param modelParameters 
     */
    public void setModelParameters(MP modelParameters) {
        this.modelParameters = modelParameters;
    }
    
}
