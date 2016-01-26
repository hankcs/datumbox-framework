/**
 * Copyright (C) 2013-2016 Vasilis Vryniotis <bbriniotis@datumbox.com>
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hankcs;

import com.datumbox.applications.nlp.TextClassifier;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.persistentstorage.ConfigurationFactory;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.utilities.PHPfunctions;
import com.datumbox.common.utilities.RandomGenerator;
import com.datumbox.framework.machinelearning.classification.MultinomialNaiveBayes;
import com.datumbox.framework.machinelearning.common.bases.mlmodels.BaseMLmodel;
import com.datumbox.framework.machinelearning.featureselection.categorical.ChisquareSelect;
import com.datumbox.framework.utilities.text.extractors.NgramsExtractor;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;


/**
 * 文本分类示例
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class TextClassification
{

    /**
     * 演示如何使用 TextClassifier 类.
     *
     * @param args 命令行
     * @throws URISyntaxException
     */
    public static void main(String[] args) throws URISyntaxException
    {
        /**
         * resources 文件夹中有两个配置文件:
         *
         * - datumbox.config.properties: 储存引擎使用的配置文件 (必需)
         * - logback.xml: 日志使用的配置文件 (可选)
         */

        //初始化
        //--------------
        RandomGenerator.setGlobalSeed(42L); //设置所有随机数发生器使用的种子
        DatabaseConfiguration dbConf = ConfigurationFactory.INMEMORY.getConfiguration(); //in-memory maps
        //DatabaseConfiguration dbConf = ConfigurationFactory.MAPDB.getConfiguration(); //mapdb maps


        //读取数据
        //Reading Data
        //------------
        Map<Object, URI> dataset = new HashMap<>();
        //每个分类的文档都存在同一个文件里，每一行代表一个文档
        //The examples of each category are stored on the same file, one example per row.
        dataset.put("positive", TextClassification.class.getClassLoader().getResource("datasets/sentiment-analysis/rt-polarity.pos").toURI());
        dataset.put("negative", TextClassification.class.getClassLoader().getResource("datasets/sentiment-analysis/rt-polarity.neg").toURI());


        //设置训练参数
        //Setup Training Parameters
        //-------------------------
        TextClassifier.TrainingParameters trainingParameters = new TextClassifier.TrainingParameters();

        //Classifier configuration
        trainingParameters.setMLmodelClass(MultinomialNaiveBayes.class);
        trainingParameters.setMLmodelTrainingParameters(new MultinomialNaiveBayes.TrainingParameters());

        //Set data transfomation configuration
        trainingParameters.setDataTransformerClass(null);
        trainingParameters.setDataTransformerTrainingParameters(null);

        //Set feature selection configuration
        trainingParameters.setFeatureSelectionClass(ChisquareSelect.class);
        trainingParameters.setFeatureSelectionTrainingParameters(new ChisquareSelect.TrainingParameters());

        //Set text extraction configuration
        trainingParameters.setTextExtractorClass(NgramsExtractor.class);
        trainingParameters.setTextExtractorParameters(new NgramsExtractor.Parameters());


        //Fit the classifier
        //------------------
        TextClassifier classifier = new TextClassifier("SentimentAnalysis", dbConf);
        classifier.fit(dataset, trainingParameters);


        //Use the classifier
        //------------------

        //Get validation metrics on the training set
        BaseMLmodel.ValidationMetrics vm = classifier.validate(dataset);
        classifier.setValidationMetrics(vm); //store them in the model for future reference

        //Classify a single sentence
        String sentence = "Datumbox is amazing!";
        Record r = classifier.predict(sentence);

        System.out.println("Classifing sentence: \"" + sentence + "\"");
        System.out.println("Predicted class: " + r.getYPredicted());
        System.out.println("Probability: " + r.getYPredictedProbabilities().get(r.getYPredicted()));

        System.out.println("Classifier Statistics: " + PHPfunctions.var_export(vm));


        //Clean up
        //--------

        //Erase the classifier. This removes all files.
        classifier.erase();
    }

}
