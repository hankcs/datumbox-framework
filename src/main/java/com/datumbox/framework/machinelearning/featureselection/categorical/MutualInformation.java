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
package com.datumbox.framework.machinelearning.featureselection.categorical;

import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;
import com.datumbox.framework.machinelearning.common.bases.featureselection.CategoricalFeatureSelection;
import com.datumbox.common.utilities.PHPfunctions;
import com.datumbox.framework.machinelearning.common.bases.featureselection.ScoreBasedFeatureSelection;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the Mutual Information Feature Selection algorithm which can be used
 * for evaluating categorical and boolean variables.
 * 
 * References: 
 * http://nlp.stanford.edu/IR-book/html/htmledition/mutual-information-1.html
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class MutualInformation extends CategoricalFeatureSelection<MutualInformation.ModelParameters, MutualInformation.TrainingParameters>{
    
    /**
     * The ModelParameters class stores the coefficients that were learned during
     * the training of the algorithm.
     */
    public static class ModelParameters extends CategoricalFeatureSelection.ModelParameters {

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
    public static class TrainingParameters extends CategoricalFeatureSelection.TrainingParameters {

    }
    
    /**
     * Public constructor of the algorithm.
     * 
     * @param dbName
     * @param dbConf 
     */
    public MutualInformation(String dbName, DatabaseConfiguration dbConf) {
        super(dbName, dbConf, MutualInformation.ModelParameters.class, MutualInformation.TrainingParameters.class);
    }
    
    
    @Override
    protected void estimateFeatureScores(Map<Object, Integer> classCounts, Map<List<Object>, Integer> featureClassCounts, Map<Object, Double> featureCounts) {
        logger.debug("estimateFeatureScores()");
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
        
        Map<Object, Double> featureScores = modelParameters.getFeatureScores();
        
        double N = modelParameters.getN();
        for(Map.Entry<Object, Double> featureCount : featureCounts.entrySet()) {
            Object feature = featureCount.getKey();
            double N1_ = featureCount.getValue(); //calculate the N1. (number of records that has the feature)
            double N0_ = N - N1_; //also the N0. (number of records that DONT have the feature)
            
            for(Map.Entry<Object, Integer> classCount : classCounts.entrySet()) {
                Object theClass = classCount.getKey();
                
                double N_1 = classCount.getValue();
                double N_0 = N - N_1;
                Integer featureClassC = featureClassCounts.get(Arrays.<Object>asList(feature, theClass));                
                double N11 = (featureClassC!=null)?featureClassC:0.0; //N11 is the number of records that have the feature and belong on the specific class
                
                double N01 = N_1 - N11; //N01 is the total number of records that do not have the particular feature BUT they belong to the specific class
                
                double N00 = N0_ - N01;
                double N10 = N1_ - N11;
                
                //calculate Mutual Information
                //Note we calculate it partially because if one of the N.. is zero the log will not be defined and it will return NAN.
                double MI=0.0;
                if(N11>0.0) {
                    MI+=(N11/N)*PHPfunctions.log((N/N1_)*(N11/N_1),2.0);
                }
                if(N01>0.0) {
                    MI+=(N01/N)*PHPfunctions.log((N/N0_)*(N01/N_1),2.0);
                }
                if(N10>0.0) {
                    MI+=(N10/N)*PHPfunctions.log((N/N1_)*(N10/N_0),2.0);
                }
                if(N00>0.0) {
                    MI+=(N00/N)*PHPfunctions.log((N/N0_)*(N00/N_0),2.0);
                }

                
                //REMEMBER! larger scores means more important keywords.
                Double previousMI = featureScores.get(feature);
                if(previousMI==null || previousMI<MI) { //add or update score
                    featureScores.put(feature, MI);
                }
                
            }
        }
        
        Integer maxFeatures = trainingParameters.getMaxFeatures();
        if(maxFeatures!=null && maxFeatures<featureScores.size()) {
            ScoreBasedFeatureSelection.selectHighScoreFeatures(featureScores, maxFeatures);
        }
        

    }
    
}
