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
package com.datumbox.framework.machinelearning.common.bases.wrappers;

import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;
import com.datumbox.framework.machinelearning.common.bases.baseobjects.BaseTrainable;
import com.datumbox.framework.machinelearning.common.bases.baseobjects.BaseModelParameters;
import com.datumbox.framework.machinelearning.common.bases.baseobjects.BaseTrainingParameters;
import com.datumbox.framework.machinelearning.common.bases.mlmodels.BaseMLmodel;
import com.datumbox.framework.machinelearning.common.dataobjects.KnowledgeBase;
import com.datumbox.framework.machinelearning.common.bases.datatransformation.DataTransformer;
import com.datumbox.framework.machinelearning.common.bases.featureselection.FeatureSelection;

/**
 * 一个可供训练，使用组合而不是继承来实现机器学习模型。它包含很多种内部对象，比如数据转换、特征
 * 选择和机器学习模型。
 * The BaseWrapper is a trainable object that uses composition instead of inheritance
 * to extend the functionality of a BaseMLmodel. It includes various internal objects
 * such as Data Transformers, Feature Selectors and Machine Learning models which 
 * are combined in the training and prediction process. 
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <MP> 模型参数
 * @param <TP> 训练参数
 */
public abstract class BaseWrapper<MP extends BaseWrapper.ModelParameters, TP extends BaseWrapper.TrainingParameters> extends BaseTrainable<MP, TP, KnowledgeBase<MP, TP>> {
    
    /**
     * The DataTransformer instance of the wrapper.
     */
    protected DataTransformer dataTransformer = null;
    
    /**
     * The FeatureSelection instance of the wrapper.
     */
    protected FeatureSelection featureSelection = null;
    
    /**
     * The Machine Learning model instance of the wrapper.
     */
    protected BaseMLmodel mlmodel = null;
    
    /**
     * The ModelParameters class stores the coefficients that were learned during
     * the training of the algorithm.
     */
    public static abstract class ModelParameters extends BaseModelParameters {

        /**
         * Protected constructor which accepts as argument the DatabaseConnector.
         * 
         * @param dbc 
         */
        protected ModelParameters(DatabaseConnector dbc) {
            super(dbc);
        }
        
    }
    
    /**
     * 包含所有的训练参数的对象<br>
     * The TrainingParameters class stores the parameters that can be changed
     * before training the algorithm.
     * 
     * @param <DT> 数据预处理
     * @param <FS> 特征选择
     * @param <ML> 机器学习模型
     */
    public static abstract class TrainingParameters<DT extends DataTransformer, FS extends FeatureSelection, ML extends BaseMLmodel> extends BaseTrainingParameters {
        
        //Classes
        /**
         * 数据预处理类
         */
        private Class<? extends DT> dataTransformerClass;

        /**
         * 特征选择类
         */
        private Class<? extends FS> featureSelectionClass;

        /**
         * 机器学习类
         */
        private Class<? extends ML> mlmodelClass;
       
        //Parameter Objects
        /**
         * 给数据预处理用的参数
         */
        private DT.TrainingParameters dataTransformerTrainingParameters;

        /**
         * 特征选择参数
         */
        private FS.TrainingParameters featureSelectionTrainingParameters;

        /**
         * 机器学习参数
         */
        private ML.TrainingParameters mlmodelTrainingParameters;

        /**
         * 获取预处理类<br>
         * Getter for the Java class of the Data Transformer.
         * 
         * @return 
         */
        public Class<? extends DT> getDataTransformerClass() {
            return dataTransformerClass;
        }
        
        /**
         * 设置预处理类，接受null<br>
         * Setter for the Java class of the Data Transformer. Pass null for none.
         * 
         * @param dataTransformerClass 
         */
        public void setDataTransformerClass(Class<? extends DT> dataTransformerClass) {
            this.dataTransformerClass = dataTransformerClass;
        }
        
        /**
         * 获取特征选择类<br>
         * Getter for the Java class of the Feature Selector.
         * 
         * @return 
         */
        public Class<? extends FS> getFeatureSelectionClass() {
            return featureSelectionClass;
        }
        
        /**
         * 设置特征选择类<br>
         * Setter for the Java class of the Feature Selector. Pass null for none.
         * 
         * @param featureSelectionClass 
         */
        public void setFeatureSelectionClass(Class<? extends FS> featureSelectionClass) {
            this.featureSelectionClass = featureSelectionClass;
        }
        
        /**
         * 获取机器学习模型类<br>
         * Getter for the Java class of the Machine Learning model which will
         * be used internally.
         * 
         * @return 
         */
        public Class<? extends ML> getMLmodelClass() {
            return mlmodelClass;
        }
        
        /**
         * 设置ML类
         * Setter for the Java class of the Machine Learning model which will
         * be used internally.
         * 
         * @param mlmodelClass 
         */
        public void setMLmodelClass(Class<? extends ML> mlmodelClass) {
            this.mlmodelClass = mlmodelClass;
        }
        
        /**
         * 获取预处理工具的参数
         * Getter for the Training Parameters of the Data Transformer.
         * 
         * @return 
         */
        public DT.TrainingParameters getDataTransformerTrainingParameters() {
            return dataTransformerTrainingParameters;
        }
        
        /**
         * 设置预处理参数<br>
         * Setter for the Training Parameters of the Data Transformer. Pass null
         * for none.
         * 
         * @param dataTransformerTrainingParameters 
         */
        public void setDataTransformerTrainingParameters(DT.TrainingParameters dataTransformerTrainingParameters) {
            this.dataTransformerTrainingParameters = dataTransformerTrainingParameters;
        }

        /**
         * 获取特征选择参数<br>
         * Getter for the Training Parameters of the Feature Selector.
         * 
         * @return 
         */
        public FS.TrainingParameters getFeatureSelectionTrainingParameters() {
            return featureSelectionTrainingParameters;
        }
        
        /**
         * 设置特征选择参数<br>
         * Setter for the Training Parameters of the Feature Selector. Pass null
         * for none.
         * 
         * @param featureSelectionTrainingParameters 
         */
        public void setFeatureSelectionTrainingParameters(FS.TrainingParameters featureSelectionTrainingParameters) {
            this.featureSelectionTrainingParameters = featureSelectionTrainingParameters;
        }

        /**
         * 获取机器学习模型的训练参数<br>
         * Getter for the Training Parameters of the Machine Learning model.
         * 
         * @return 
         */
        public ML.TrainingParameters getMLmodelTrainingParameters() {
            return mlmodelTrainingParameters;
        }
        
        /**
         * 设置训练参数<br>
         * Setter for the Training Parameters of the Machine Learning model.
         * 
         * @param mlmodelTrainingParameters 
         */
        public void setMLmodelTrainingParameters(ML.TrainingParameters mlmodelTrainingParameters) {
            this.mlmodelTrainingParameters = mlmodelTrainingParameters;
        }
        
    }

    /**
     * 不公开的构造<br>
     * Protected constructor of the algorithm.
     * 
     * @param dbName
     * @param dbConf 
     * @param mpClass 
     * @param tpClass 
     */
    protected BaseWrapper(String dbName, DatabaseConfiguration dbConf, Class<MP> mpClass, Class<TP> tpClass) {
        super(dbName, dbConf, mpClass, tpClass);
    }
      
    /**
     * Deletes the database of all the internal algorithms. 
     */
    @Override
    public void erase() {
        if(dataTransformer!=null) {
            dataTransformer.erase();
        }
        if(featureSelection!=null) {
            featureSelection.erase();
        }
        if(mlmodel!=null) {
            mlmodel.erase();
        }
        knowledgeBase.erase();
    }
      
    /**
     * Closes all the resources of all the internal algorithms.  
     */
    @Override
    public void close() {
        if(dataTransformer!=null) {
            dataTransformer.close();
        }
        if(featureSelection!=null) {
            featureSelection.close();
        }
        if(mlmodel!=null) {
            mlmodel.close();
        }
        knowledgeBase.close();
    }

    /**
     * Getter for the Validation Metrics of the algorithm.
     * 
     * @param <VM>
     * @return 
     */
    public <VM extends BaseMLmodel.ValidationMetrics> VM getValidationMetrics() {
        if(mlmodel!=null) {
            return (VM) mlmodel.getValidationMetrics();
        }
        else {
            return null;
        }
    }
    
    /**
     * Setter for the Validation Metrics of the algorithm.
     * 
     * @param <VM>
     * @param validationMetrics 
     */
    public <VM extends BaseMLmodel.ValidationMetrics> void setValidationMetrics(VM validationMetrics) {
        if(mlmodel!=null) {
            mlmodel.setValidationMetrics(validationMetrics);
        }
    }
}
