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
package com.datumbox.framework.machinelearning.common.bases.baseobjects;

import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.objecttypes.Trainable;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.framework.machinelearning.common.dataobjects.KnowledgeBase;
import java.lang.reflect.InvocationTargetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 所有模型的基类，包含机器学习模型、数据预处理、特征选择等等<br>
 * Base class for every Model of the Framework. This includes Machine Learning
 * Models, Data Transformers, Feature Selectors etc.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <MP>
 * @param <TP>
 * @param <KB>
 */
public abstract class BaseTrainable<MP extends BaseModelParameters, TP extends BaseTrainingParameters, KB extends KnowledgeBase<MP, TP>> implements Trainable<MP, TP> {
    
    /**
     * 所有算法的logger，通过使用非静态的形式，打印所有继承类的名称<br>
     * The Logger of all algorithms.
     * We want this to be non-static in order to print the names of the inherited classes.
     */
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    /**
     * 数据库实例<br>
     * The KnowledgeBase instance of the algorithm.
     */
    protected KB knowledgeBase;
    
    /**
     * 数据库名称<br>
     * The name of the Database where we persist our data.
     */
    protected String dbName;
    
    /**
     * 给定算法类，创建一个可训练对象出来<br>
     * Generates a new instance of a BaseTrainable by providing the Class of the
     * algorithm.
     * 
     * @param <BT>
     * @param aClass
     * @param dbName
     * @param dbConfig
     * @return 
     */
    public static <BT extends BaseTrainable> BT newInstance(Class<BT> aClass, String dbName, DatabaseConfiguration dbConfig) {
        BT algorithm = null;
        try {
            algorithm = aClass.getConstructor(String.class, DatabaseConfiguration.class).newInstance(dbName, dbConfig);
        } 
        catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
            throw new RuntimeException(ex);
        }
        
        return algorithm;
    }

    /**
     * 不包含知识库初始化的构造函数<br>
     * Protected Constructor which does not include the initialization of the
     * KnowledgeBase.
     * 
     * @param dbName
     * @param dbConf 
     */
    protected BaseTrainable(String dbName, DatabaseConfiguration dbConf) {
        String methodName = this.getClass().getSimpleName();
        String dbNameSeparator = dbConf.getDBnameSeparator();
        if(!dbName.contains(methodName+dbNameSeparator)) { //patch for the K-fold cross validation which already contains the name of the algorithm in the dbname
            dbName += dbNameSeparator + methodName;
        }
        
        this.dbName = dbName;
    }
    
    /**
     * 初始化知识库的构造函数<br>
     * Protected Constructor which includes the initialization of the
     * KnowledgeBase.
     * 
     * @param dbName
     * @param dbConf
     * @param mpClass
     * @param tpClass 
     */
    protected BaseTrainable(String dbName, DatabaseConfiguration dbConf, Class<MP> mpClass, Class<TP> tpClass) {
        this(dbName, dbConf);
        
        knowledgeBase = (KB) new KnowledgeBase(this.dbName, dbConf, mpClass, tpClass);
    }
    
    /**
     * 获取训练后的模型参数<br>
     * Returns the model parameters that were estimated after training.
     * 
     * @return 
     */
    @Override
     public MP getModelParameters() {
       return knowledgeBase.getModelParameters();

    } 
    
    /**
     * 获取训练参数<br>
     * It returns the training parameters that configure the algorithm.
     * 
     * @return 
     */
    @Override
    public TP getTrainingParameters() {
        return knowledgeBase.getTrainingParameters();
    }
    
    /**
     * 使用
     * Trains a Machine Learning model using the provided training data. This
     * method is responsible for initializing appropriately the algorithm and then
     * calling the _fit() method which performs the learning.
     * 
     * @param trainingData
     * @param trainingParameters 
     */
    @Override
    public void fit(Dataset trainingData, TP trainingParameters) {
        logger.info("fit()");
        
        //reset knowledge base
        knowledgeBase.reinitialize();
        knowledgeBase.setTrainingParameters(trainingParameters);
        
        MP modelParameters = knowledgeBase.getModelParameters();
        modelParameters.setN(trainingData.getRecordNumber());
        modelParameters.setD(trainingData.getVariableNumber());
        
        _fit(trainingData);
        
        logger.info("Saving model");
        knowledgeBase.save();
    }
      
    /**
     * Deletes the database of the algorithm. 
     */
    @Override
    public void erase() {
        knowledgeBase.erase();
    }
            
    /**
     * Closes all the resources of the algorithm. 
     */
    @Override
    public void close() {
        knowledgeBase.close();
    }
    
    /**
     * This method estimates the actual coefficients of the algorithm.
     * 
     * @param trainingData 
     */
    protected abstract void _fit(Dataset trainingData);
    
}
