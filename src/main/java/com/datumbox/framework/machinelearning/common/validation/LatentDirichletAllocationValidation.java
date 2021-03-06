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

import com.datumbox.framework.machinelearning.common.bases.validation.ModelValidation;
import com.datumbox.framework.machinelearning.topicmodeling.LatentDirichletAllocation;
import java.util.List;

/**
 * Validation class for Latent Dirichlet Allocation.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class LatentDirichletAllocationValidation extends ModelValidation<LatentDirichletAllocation.ModelParameters, LatentDirichletAllocation.TrainingParameters, LatentDirichletAllocation.ValidationMetrics> {
    
    /**
     * Calculates the average validation metrics by combining the results of the
     * provided list.
     * 
     * @param validationMetricsList
     * @return 
     */        
    @Override
    protected LatentDirichletAllocation.ValidationMetrics calculateAverageValidationMetrics(List<LatentDirichletAllocation.ValidationMetrics> validationMetricsList) {

        if(validationMetricsList.isEmpty()) {
            return null;
        }
        
        int k = validationMetricsList.size(); //number of samples
        
        //create a new empty ValidationMetrics Object
        LatentDirichletAllocation.ValidationMetrics avgValidationMetrics = (LatentDirichletAllocation.ValidationMetrics) validationMetricsList.iterator().next().getEmptyObject();
        
        for(LatentDirichletAllocation.ValidationMetrics vmSample : validationMetricsList) {
            avgValidationMetrics.setPerplexity(avgValidationMetrics.getPerplexity()+ vmSample.getPerplexity()/k);
        }
        
        return avgValidationMetrics;
    }
}
