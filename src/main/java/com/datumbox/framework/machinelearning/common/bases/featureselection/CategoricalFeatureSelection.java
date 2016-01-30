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
package com.datumbox.framework.machinelearning.common.bases.featureselection;

import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;
import com.datumbox.common.persistentstorage.interfaces.BigMap;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.dataobjects.TypeInference;
import com.datumbox.common.dataobjects.TypeInference.DataType;


import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 根据类别的特征选择器<br>
 * Abstract class which is the base of every Categorical Feature Selection algorithm.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <MP>
 * @param <TP>
 */
public abstract class CategoricalFeatureSelection<MP extends CategoricalFeatureSelection.ModelParameters, TP extends CategoricalFeatureSelection.TrainingParameters> extends FeatureSelection<MP, TP> {
    
    /**
     * 模型参数的基类<br>
     * Base class for the Model Parameters of the algorithm.
     */
    public static abstract class ModelParameters extends FeatureSelection.ModelParameters {

        /**
         * 储存所有的特征<br>
         */
        @BigMap
        private Map<Object, Double> featureScores; //map which stores the scores of the features

        /**
         * 以数据连接器作为参数的构造方法<br>
         * Protected constructor which accepts as argument the DatabaseConnector.
         * 
         * @param dbc 
         */
        protected ModelParameters(DatabaseConnector dbc) {
            super(dbc);
        }
        
        /**
         * 获取特征分数<br>
         * Getter of the Feature Scores.
         * 
         * @return 
         */
        public Map<Object, Double> getFeatureScores() {
            return featureScores;
        }
        
        /**
         * 设置特征分数<br>
         * Setter of the Feature Scores.
         * 
         * @param featureScores 
         */
        protected void setFeatureScores(Map<Object, Double> featureScores) {
            this.featureScores = featureScores;
        }
        
    }
    
    /**
     * 这个算法的训练参数<br>
     * Base class for the Training Parameters of the algorithm.
     */
    public static abstract class TrainingParameters extends FeatureSelection.TrainingParameters {

        /**
         * 特征阀值
         */
        private Integer rareFeatureThreshold = null;
        /**
         * 最多特征数
         */
        private Integer maxFeatures=null;
        /**
         * 忽略数值类型的特征
         */
        private boolean ignoringNumericalFeatures = true;
        
        /**
         * 获取特征频次阀值，任何少于该值的特征将会被删除<br>
         * Getter for the rare feature threshold. Any feature that exists
         * in the training dataset less times than this number will be removed
         * directly. 
         * 
         * @return 
         */
        public Integer getRareFeatureThreshold() {
            return rareFeatureThreshold;
        }
        
        /**
         * 设置特征频次阀值<br>
         * Setter for the rare feature threshold. Set to null to deactivate this 
         * feature. Any feature that exists in the training dataset less times 
         * than this number will be removed directly. 
         * 
         * @param rareFeatureThreshold 
         */
        public void setRareFeatureThreshold(Integer rareFeatureThreshold) {
            this.rareFeatureThreshold = rareFeatureThreshold;
        }
        
        /**
         * 获取数据库中最多特征频数目<br>
         * Getter for the maximum number of features that should be kept in the
         * dataset.
         * 
         * @return 
         */
        public Integer getMaxFeatures() {
            return maxFeatures;
        }
        
        /**
         * 设置数据库中最多特征数目<br>
         * Setter for the maximum number of features that should be kept in the
         * dataset. Set to null for unlimited.
         * 
         * @param maxFeatures 
         */
        public void setMaxFeatures(Integer maxFeatures) {
            this.maxFeatures = maxFeatures;
        }
        
        /**
         * 获取算法是否应该忽略数值类型的特征<br>
         * Getter for whether the algorithm should ignore numerical features.
         * 
         * @return 
         */
        public boolean isIgnoringNumericalFeatures() {
            return ignoringNumericalFeatures;
        }
        
        /**
         * 设置算法是否应该忽略数值类型的特征<br>
         * Setter for whether the algorithm should ignore numerical features.
         * 
         * @param ignoringNumericalFeatures 
         */
        public void setIgnoringNumericalFeatures(boolean ignoringNumericalFeatures) {
            this.ignoringNumericalFeatures = ignoringNumericalFeatures;
        }
        
    }
    
    /**
     * 构造<br>
     * Protected constructor of the algorithm.
     * 
     * @param dbName
     * @param dbConf
     * @param mpClass
     * @param tpClass 
     */
    protected CategoricalFeatureSelection(String dbName, DatabaseConfiguration dbConf, Class<MP> mpClass, Class<TP> tpClass) {
        super(dbName, dbConf, mpClass, tpClass);
    }
    
    @Override
    protected void _fit(Dataset data) {
        
        DatabaseConnector dbc = knowledgeBase.getDbc();
        
        Map<Object, Integer> tmp_classCounts = dbc.getBigMap("tmp_classCounts", true); //map which stores the counts of the classes
        Map<List<Object>, Integer> tmp_featureClassCounts = dbc.getBigMap("tmp_featureClassCounts", true); //map which stores the counts of feature-class combinations.
        Map<Object, Double> tmp_featureCounts = dbc.getBigMap("tmp_featureCounts", true); //map which stores the counts of the features

        //build the maps with the feature statistics and counts
        buildFeatureStatistics(data, tmp_classCounts, tmp_featureClassCounts, tmp_featureCounts);

        //call the overriden method to get the scores of the features.
        //WARNING: do not use feature scores for any weighting. Sometimes the features are selected based on a minimum and others on a maximum criterion.
        estimateFeatureScores(tmp_classCounts, tmp_featureClassCounts, tmp_featureCounts);
        

        //drop the unnecessary stastistics tables
        dbc.dropBigMap("tmp_classCounts", tmp_classCounts);
        dbc.dropBigMap("tmp_featureClassCounts", tmp_featureClassCounts);
        dbc.dropBigMap("tmp_featureCounts", tmp_featureCounts);
    }
    
    @Override
    protected void filterFeatures(Dataset newdata) {
        //now filter the data by removing all the features that are not selected
        filterData(newdata, knowledgeBase.getDbc(), knowledgeBase.getModelParameters().getFeatureScores(), knowledgeBase.getTrainingParameters().isIgnoringNumericalFeatures());
    }
    
    private static void filterData(Dataset data, DatabaseConnector dbc, Map<Object, Double> featureScores, boolean ignoringNumericalFeatures) {
        Logger logger = LoggerFactory.getLogger(CategoricalFeatureSelection.class);
        logger.debug("filterData()");
        
        Map<Object, Boolean> tmp_removedColumns = dbc.getBigMap("tmp_removedColumns", true);
        
        for(Map.Entry<Object, DataType> entry: data.getXDataTypes().entrySet()) {
            Object feature = entry.getKey();
            
            if(ignoringNumericalFeatures) {
                if(entry.getValue()==TypeInference.DataType.NUMERICAL) { //is it numerical? 
                    continue; //skip any further analysis
                }
            }
            
            if(!featureScores.containsKey(feature)) {
                tmp_removedColumns.put(feature, true);
            }
        }
        
        logger.debug("Removing Columns");
        data.removeColumns(tmp_removedColumns.keySet());
        
        //Drop the temporary Collection
        dbc.dropBigMap("tmp_removedColumns", tmp_removedColumns);
        
        
        
    }
    
    private void removeRareFeatures(Dataset data, Map<Object, Double> featureCounts) {
        logger.debug("removeRareFeatures()");
        DatabaseConnector dbc = knowledgeBase.getDbc();
        TP trainingParameters = knowledgeBase.getTrainingParameters();
        Integer rareFeatureThreshold = trainingParameters.getRareFeatureThreshold();
        boolean ignoringNumericalFeatures = trainingParameters.isIgnoringNumericalFeatures();
        
        Map<Object, TypeInference.DataType> columnTypes = data.getXDataTypes();
        
        //find the featureCounts
        
        logger.debug("Estimating featureCounts");
        for(Integer rId : data) {
            Record r = data.get(rId);
            for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
                Object feature = entry.getKey();
                
                if(ignoringNumericalFeatures) { //if we ignore the numerical features, investigate further if we must skip the feature
                    if(columnTypes.get(feature)==TypeInference.DataType.NUMERICAL) { //is it numerical? 
                        continue; //skip any further analysis
                    }
                }
                
                Double value = TypeInference.toDouble(entry.getValue());
                if(value==null || value==0.0) {
                    continue;
                }


                
                //feature counts
                Double featureCounter = featureCounts.get(feature);
                if(featureCounter==null) {
                    featureCounter=0.0;
                }
                featureCounts.put(feature, ++featureCounter);
                
            }
        }

        //remove rare features
        if(rareFeatureThreshold != null && rareFeatureThreshold>0) {
            logger.debug("Removing rare features");
            //remove features from the featureCounts list
            Iterator<Map.Entry<Object, Double>> it = featureCounts.entrySet().iterator();
            while(it.hasNext()) {
                Map.Entry<Object, Double> entry = it.next();
                if(entry.getValue()<=rareFeatureThreshold) {
                    it.remove(); 
                }
            }
            
            //then remove the features in dataset that do not appear in the list
            filterData(data, dbc, featureCounts, ignoringNumericalFeatures);
        }
    }

    /**
     * 统计特征
     * @param data
     * @param classCounts
     * @param featureClassCounts
     * @param featureCounts
     */
    private void buildFeatureStatistics(Dataset data, Map<Object, Integer> classCounts, Map<List<Object>, Integer> featureClassCounts, Map<Object, Double> featureCounts) {        
        logger.debug("buildFeatureStatistics()");
        TP trainingParameters = knowledgeBase.getTrainingParameters();
        boolean ignoringNumericalFeatures = trainingParameters.isIgnoringNumericalFeatures();
        
        //the method below does not only removes the rare features but also
        //first and formost calculates the contents of featureCounts map. 
        removeRareFeatures(data, featureCounts);
        
        Map<Object, TypeInference.DataType> columnTypes = data.getXDataTypes();
        //now find the classCounts and the featureClassCounts
        logger.debug("Estimating classCounts and featureClassCounts");
        for(Integer rId : data) {
            Record r = data.get(rId);
            Object theClass = r.getY();

            //class counts
            Integer classCounter = classCounts.get(theClass);
            if(classCounter==null) {
                classCounter=0;
            }
            classCounts.put(theClass, ++classCounter);


            for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
                Object feature = entry.getKey();
                
                if(ignoringNumericalFeatures) { //if we ignore the numerical features, investigate further if we must skip the feature
                    if(columnTypes.get(feature)==TypeInference.DataType.NUMERICAL) { //is it numerical? 
                        continue; //skip any further analysis
                    }
                }
                
                Double value = TypeInference.toDouble(entry.getValue());
                if(value==null || value==0.0) {
                    continue;
                }



                //featureClass counts
                List<Object> featureClassTuple = Arrays.<Object>asList(feature, theClass);
                Integer featureClassCounter = featureClassCounts.get(featureClassTuple);
                if(featureClassCounter==null) {
                    featureClassCounter=0;
                }
                featureClassCounts.put(featureClassTuple, ++featureClassCounter);
            }


        }
        
    }
    
    /**
     * Abstract method which is responsible for estimating the score of each
     * Feature.
     * 
     * @param classCounts
     * @param featureClassCounts
     * @param featureCounts 
     */
    protected abstract void estimateFeatureScores(Map<Object, Integer> classCounts, Map<List<Object>, Integer> featureClassCounts, Map<Object, Double> featureCounts);
}
