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
package com.datumbox.framework.machinelearning.common.bases.mlmodels;

import com.datumbox.framework.machinelearning.common.bases.validation.ModelValidation;
import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.framework.machinelearning.common.bases.baseobjects.BaseTrainable;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;

import com.datumbox.framework.machinelearning.common.bases.baseobjects.BaseModelParameters;
import com.datumbox.framework.machinelearning.common.bases.baseobjects.BaseTrainingParameters;
import com.datumbox.framework.machinelearning.common.bases.baseobjects.BaseValidationMetrics;
import com.datumbox.framework.machinelearning.common.dataobjects.MLmodelKnowledgeBase;

/**
 * Base Class for Machine Learning algorithms.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <MP>
 * @param <TP>
 * @param <VM>
 */
public abstract class BaseMLmodel<MP extends BaseMLmodel.ModelParameters, TP extends BaseMLmodel.TrainingParameters, VM extends BaseMLmodel.ValidationMetrics> extends BaseTrainable<MP, TP, MLmodelKnowledgeBase<MP, TP, VM>> {

    private final ModelValidation<MP, TP, VM> modelValidator;
    
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
     * The TrainingParameters class stores the parameters that can be changed
     * before training the algorithm.
     */
    public static abstract class TrainingParameters extends BaseTrainingParameters {
        
    } 

    /**
     * The ValidationMetrics class stores information about the performance of the
     * algorithm.
     */
    public static abstract class ValidationMetrics extends BaseValidationMetrics {   
        
    }
    
    /**
     * Protected constructor of the algorithm.
     * 
     * @param dbName
     * @param dbConf 
     * @param mpClass 
     * @param tpClass 
     * @param vmClass 
     * @param modelValidator 
     */
    protected BaseMLmodel(String dbName, DatabaseConfiguration dbConf, Class<MP> mpClass, Class<TP> tpClass, Class<VM> vmClass, ModelValidation<MP, TP, VM> modelValidator) {
        super(dbName, dbConf);
        
        knowledgeBase = new MLmodelKnowledgeBase<>(this.dbName, dbConf, mpClass, tpClass, vmClass);
        this.modelValidator = modelValidator;
    } 
    
    /**
     * Performs k-fold cross validation on the dataset and returns the ValidationMetrics
     * Object.
     * 
     * @param trainingData
     * @param trainingParameters
     * @param k
     * @return  
     */
    @SuppressWarnings("unchecked")
    public VM kFoldCrossValidation(Dataset trainingData, TP trainingParameters, int k) {
        logger.info("kFoldCrossValidation()");
        
        return modelValidator.kFoldCrossValidation(trainingData, k, dbName, knowledgeBase.getDbConf(), this.getClass(), trainingParameters);
    }
    
    /**
     * Calculates the predictions for the newData and stores the predictions
     * inside the object.
     * 
     * @param newData 
     */
    public void predict(Dataset newData) { 
        logger.info("predict()");
        
        knowledgeBase.load();
        
        predictDataset(newData);

    }
    
    /**
     * Validate the model against the testingData and returns the validationMetrics;
     * It does not update the validationMetrics.
     * 
     * @param testingData
     * @return 
     */
     public VM validate(Dataset testingData) {  
        logger.info("validate()");
        
        knowledgeBase.load();

        //validate the model with the testing data and update the validationMetrics
        VM validationMetrics = validateModel(testingData);
        
        return validationMetrics;
    }
    
    
    /**
     * Setter for the Validation Metrics of the algorithm. Usually used to set 
     * the metrics after running a validate() or when doing K-fold cross validation.
     * 
     * @param validationMetrics 
     */
    public void setValidationMetrics(VM validationMetrics) {
        knowledgeBase.setValidationMetrics(validationMetrics);
        
        logger.info("Updating model");
        knowledgeBase.save();
    }
    
    /**
     * Getter for the Validation Metrics of the algorithm.
     * 
     * @return 
     */
    public VM getValidationMetrics() {
        return knowledgeBase.getValidationMetrics();
    }
    
    /**
     * Validates the model with the provided dataset and returns the validation
     * metrics.
     * 
     * @param validationData
     * @return 
     */
    protected abstract VM validateModel(Dataset validationData);
    
    /**
     * Estimates the predictions for a new Dataset.
     * 
     * @param newData 
     */
    protected abstract void predictDataset(Dataset newData);
}
