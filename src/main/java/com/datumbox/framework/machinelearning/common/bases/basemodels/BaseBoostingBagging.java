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
package com.datumbox.framework.machinelearning.common.bases.basemodels;

import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.DataTable2D;
import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.FlatDataList;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;
import com.datumbox.common.utilities.MapFunctions;
import com.datumbox.framework.machinelearning.common.bases.mlmodels.BaseMLmodel;
import com.datumbox.framework.machinelearning.common.bases.mlmodels.BaseMLclassifier;
import com.datumbox.framework.machinelearning.common.validation.ClassifierValidation;
import com.datumbox.framework.machinelearning.ensemblelearning.FixedCombinationRules;
import com.datumbox.framework.statistics.descriptivestatistics.Descriptives;
import com.datumbox.framework.statistics.sampling.SRS;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Base class for Adaboost and BoostrapAgregating.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <MP>
 * @param <TP>
 * @param <VM>
 */
public abstract class BaseBoostingBagging<MP extends BaseBoostingBagging.ModelParameters, TP extends BaseBoostingBagging.TrainingParameters, VM extends BaseBoostingBagging.ValidationMetrics> extends BaseMLclassifier<MP, TP, VM> {

    private static final String DB_INDICATOR = "Cmp";
    private static final int maxNumberOfRetries = 2;
    
    /**
     * The ModelParameters class stores the coefficients that were learned during
     * the training of the algorithm.
     */
    public static abstract class ModelParameters extends BaseMLclassifier.ModelParameters {
        
        private List<Double> weakClassifierWeights = new ArrayList<>();

        /**
         * Protected constructor which accepts as argument the DatabaseConnector.
         * 
         * @param dbc 
         */
        protected ModelParameters(DatabaseConnector dbc) {
            super(dbc);
        }
        
        /**
         * Getter for the weights of the weak classifiers.
         * 
         * @return 
         */
        public List<Double> getWeakClassifierWeights() {
            return weakClassifierWeights;
        }
        
        /**
         * Setter for the weights of the weak classifiers.
         * 
         * @param weakClassifierWeights 
         */
        protected void setWeakClassifierWeights(List<Double> weakClassifierWeights) {
            this.weakClassifierWeights = weakClassifierWeights;
        }
        
    } 

    /**
     * The TrainingParameters class stores the parameters that can be changed
     * before training the algorithm.
     */
    public static abstract class TrainingParameters extends BaseMLclassifier.TrainingParameters {      
        
        //primitives/wrappers
        private int maxWeakClassifiers = 5; //the total number of classifiers that Bagging will initialize
        
        //Classes
        private Class<? extends BaseMLclassifier> weakClassifierClass; //the class name of weak classifiear
        
        //Parameter Objects
        private BaseMLclassifier.TrainingParameters weakClassifierTrainingParameters; //the parameters of the weak classifier
        
        /**
         * Getter for the maximum number of weak classifiers.
         * 
         * @return 
         */
        public int getMaxWeakClassifiers() {
            return maxWeakClassifiers;
        }
        
        /**
         * Setter for the maximum number of weak classifiers.
         * 
         * @param maxWeakClassifiers 
         */
        public void setMaxWeakClassifiers(int maxWeakClassifiers) {
            this.maxWeakClassifiers = maxWeakClassifiers;
        }
        
        /**
         * Getter for the Java class of the weak classifier.
         * 
         * @return 
         */
        public Class<? extends BaseMLclassifier> getWeakClassifierClass() {
            return weakClassifierClass;
        }
        
        /**
         * Setter for the Java class of the weak classifier.
         * 
         * @param weakClassifierClass 
         */
        public void setWeakClassifierClass(Class<? extends BaseMLclassifier> weakClassifierClass) {
            this.weakClassifierClass = weakClassifierClass;
        }
        
        /**
         * Getter for the Training Parameters of the weak classifier.
         * 
         * @return 
         */
        public BaseMLclassifier.TrainingParameters getWeakClassifierTrainingParameters() {
            return weakClassifierTrainingParameters;
        }

        /**
         * Setter for the Training Parameters of the weak classifier.
         * 
         * @param weakClassifierTrainingParameters 
         */
        public void setWeakClassifierTrainingParameters(BaseMLclassifier.TrainingParameters weakClassifierTrainingParameters) {
            this.weakClassifierTrainingParameters = weakClassifierTrainingParameters;
        }
        
    } 
    
    /**
     * The ValidationMetrics class stores information about the performance of the
     * algorithm.
     */
    public static abstract class ValidationMetrics extends BaseMLclassifier.ValidationMetrics {

    }

    /**
     * Protected constructor of the algorithm.
     * 
     * @param dbName
     * @param dbConf 
     * @param mpClass 
     * @param tpClass 
     * @param vmClass 
     */
    protected BaseBoostingBagging(String dbName, DatabaseConfiguration dbConf, Class<MP> mpClass, Class<TP> tpClass, Class<VM> vmClass) {
        super(dbName, dbConf, mpClass, tpClass, vmClass, new ClassifierValidation<>());
    } 
    
    @Override
    protected void predictDataset(Dataset newData) { 
        Class<? extends BaseMLclassifier> weakClassifierClass = knowledgeBase.getTrainingParameters().getWeakClassifierClass();
        List<Double> weakClassifierWeights = knowledgeBase.getModelParameters().getWeakClassifierWeights();
        
        //create a temporary map for the observed probabilities in training set
        DatabaseConnector dbc = knowledgeBase.getDbc();
        Map<Object, Object> tmp_recordDecisions = dbc.getBigMap("tmp_recordDecisions", true);
        
        //initialize array of recordDecisions
        AssociativeArray recordDecisionsArray = new AssociativeArray(tmp_recordDecisions);
        for(Integer rId : newData) {
            recordDecisionsArray.put(rId, new DataTable2D());
        }
        
        //using the weak classifiers
        AssociativeArray classifierWeightsArray = new AssociativeArray();
        int totalWeakClassifiers = weakClassifierWeights.size();
        for(int t=0;t<totalWeakClassifiers;++t) {
            BaseMLclassifier mlclassifier = BaseMLmodel.newInstance(weakClassifierClass, dbName+knowledgeBase.getDbConf().getDBnameSeparator()+DB_INDICATOR+String.valueOf(t), knowledgeBase.getDbConf());
            mlclassifier.predict(newData);
            mlclassifier.close();
            mlclassifier = null;
            
            classifierWeightsArray.put(t, weakClassifierWeights.get(t));
            
            for(Integer rId : newData) {
                Record r = newData.get(rId);
                AssociativeArray classProbabilities = r.getYPredictedProbabilities();
                
                DataTable2D currentRecordDecisions = (DataTable2D) recordDecisionsArray.get(rId);
                
                currentRecordDecisions.put(t, classProbabilities);
            }
        }
        
        //for each record find the combined classification by majority vote
        for(Integer rId : newData) {
            Record r = newData.get(rId);
            DataTable2D currentRecordDecisions = (DataTable2D) recordDecisionsArray.get(rId);
            
            AssociativeArray combinedClassVotes = FixedCombinationRules.weightedAverage(currentRecordDecisions, classifierWeightsArray);
            Descriptives.normalize(combinedClassVotes);
            
            newData.set(rId, new Record(r.getX(), r.getY(), MapFunctions.selectMaxKeyValue(combinedClassVotes).getKey(), combinedClassVotes));
        }
        
        //Drop the temporary Collection
        dbc.dropBigMap("tmp_recordDecisions", tmp_recordDecisions);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    protected void _fit(Dataset trainingData) {
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
        
        int n = modelParameters.getN();
        
        Set<Object> classesSet = modelParameters.getClasses();
        
        //first we need to find all the classes
        for(Integer rId : trainingData) { 
            Record r = trainingData.get(rId);
            Object theClass=r.getY();
            
            classesSet.add(theClass); 
        }
        
        
        
        AssociativeArray observationWeights = new AssociativeArray();
        
        //calculate the training parameters of bagging
        for(Integer rId : trainingData) { 
            observationWeights.put(rId, 1.0/n); //initialize observation weights
        }
        
        Class<? extends BaseMLclassifier> weakClassifierClass = trainingParameters.getWeakClassifierClass();
        BaseMLclassifier.TrainingParameters weakClassifierTrainingParameters = trainingParameters.getWeakClassifierTrainingParameters();
        int totalWeakClassifiers = trainingParameters.getMaxWeakClassifiers();
        
        //training the weak classifiers
        int t=0;
        int retryCounter = 0;
        while(t<totalWeakClassifiers) {
            logger.debug("Training Weak learner {}", t);
            //We sample a list of Ids based on their weights
            FlatDataList sampledIDs = SRS.weightedSampling(observationWeights, n, true).toFlatDataList();
            
            //We construct a new Dataset from the sampledIDs
            Dataset sampledTrainingDataset = trainingData.generateNewSubset(sampledIDs);
            
            //WARNING: The ids of the new sampledTrainingDataset are not the same
            //as the ones on the original dataset.
            //To link back the new ids to the old ones we must use the mapping
            //as stored in the sampledIDs list.
            
            BaseMLclassifier mlclassifier = BaseMLmodel.newInstance(weakClassifierClass, dbName+knowledgeBase.getDbConf().getDBnameSeparator()+DB_INDICATOR+String.valueOf(t), knowledgeBase.getDbConf());
            
            mlclassifier.fit(sampledTrainingDataset, weakClassifierTrainingParameters); 
            sampledTrainingDataset.erase();
            sampledTrainingDataset = null;
            
            
            Dataset validationDataset = trainingData;
            mlclassifier.predict(validationDataset);
            mlclassifier.close();
            mlclassifier = null;
            
            Status status = updateObservationAndClassifierWeights(validationDataset, observationWeights, sampledIDs);
            validationDataset = null;
            
            if(status==Status.STOP) {
                logger.debug("Skipping further training due to low error");
                break;
            }
            else if(status==Status.IGNORE) {
                if(retryCounter<maxNumberOfRetries) {
                    logger.debug("Ignoring last weak learner due to high error");
                    ++retryCounter;
                    continue; 
                }
                else {
                    logger.debug("Too many retries, skipping further training");
                    break;
                }
            }
            else if(status==Status.NEXT) {
                retryCounter = 0; //reset the retry counter
            }
            
            ++t; //increase counter here. This is because some times we might want to redo the 
        }
        
    }
    
    /**
     * The status of the weight estimation process.
     */
    protected enum Status {
        /**
         * Keep the weak learner and move to the next one.
         */
        NEXT,
        
        /**
         * Keep the weak learner and stop.
         */
        STOP,
        
        /**
         * Ignore the weak learner and move to the next one.
         */
        IGNORE;
    }
    
    /**
     * Updates the weights of observations and the weights of the classifiers.
     * 
     * @param validationDataset
     * @param observationWeights
     * @param idMapping
     * @return 
     */
    protected abstract Status updateObservationAndClassifierWeights(Dataset validationDataset, AssociativeArray observationWeights, FlatDataList idMapping);
    
    /**
     * Deletes the database of all the weak algorithms. 
     */
    @Override
    public void erase() {
        eraseWeakClassifiers();
        super.erase();
    }
    
    private void eraseWeakClassifiers() {
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
        
        if(modelParameters==null) {
            return;
        }
        
        Class<? extends BaseMLclassifier> weakClassifierClass = trainingParameters.getWeakClassifierClass();
        //the number of weak classifiers is the minimum between the classifiers that were defined in training parameters AND the number of the weak classifiers that were kept +1 for the one that was abandoned due to high error
        int totalWeakClassifiers = Math.min(modelParameters.getWeakClassifierWeights().size()+1, trainingParameters.getMaxWeakClassifiers());
        for(int t=0;t<totalWeakClassifiers;++t) {
            BaseMLclassifier mlclassifier = BaseMLmodel.newInstance(weakClassifierClass, dbName+knowledgeBase.getDbConf().getDBnameSeparator()+DB_INDICATOR+String.valueOf(t), knowledgeBase.getDbConf());
            mlclassifier.erase();
        }
    }
}
