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
package com.datumbox.framework.machinelearning.common.validation;

import com.datumbox.framework.machinelearning.classification.OrdinalRegression;
import java.util.List;

/**
 * Validation class for Ordinal Regression.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class OrdinalRegressionValidation extends ClassifierValidation<OrdinalRegression.ModelParameters, OrdinalRegression.TrainingParameters, OrdinalRegression.ValidationMetrics> {
    
    /**
     * Calculates the average validation metrics by combining the results of the
     * provided list.
     * 
     * @param validationMetricsList
     * @return 
     */        
    @Override
    protected OrdinalRegression.ValidationMetrics calculateAverageValidationMetrics(List<OrdinalRegression.ValidationMetrics> validationMetricsList) {
        OrdinalRegression.ValidationMetrics avgValidationMetrics = super.calculateAverageValidationMetrics(validationMetricsList);
        if(avgValidationMetrics==null) {
            return null;
        }
        
        int k = validationMetricsList.size(); //number of samples
        for(OrdinalRegression.ValidationMetrics vmSample : validationMetricsList) {
            avgValidationMetrics.setCountRSquare(avgValidationMetrics.getCountRSquare() + vmSample.getCountRSquare()/k);
            avgValidationMetrics.setSSE(avgValidationMetrics.getSSE() + vmSample.getSSE()/k);
        }
        
        return avgValidationMetrics;
    }
}
