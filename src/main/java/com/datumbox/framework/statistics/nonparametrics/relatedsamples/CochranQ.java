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
package com.datumbox.framework.statistics.nonparametrics.relatedsamples;

import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.DataTable2D;
import com.datumbox.common.dataobjects.TypeInference;
import com.datumbox.framework.statistics.distributions.ContinuousDistributions;
import java.util.HashMap;
import java.util.Map;

/**
 * CochranQ related sample non-parametric test.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class CochranQ {
        
    /**
     * Calculates the p-value of null Hypothesis.
     * 
     * @param dataTable
     * @return 
     * @throws IllegalArgumentException 
     */
    public static double getPvalue(DataTable2D dataTable) throws IllegalArgumentException {
        if(dataTable.isValid()==false) {
            throw new IllegalArgumentException();
        }

        //Estimate marginal scores and sum
        Map<Object, Double> XdotJ = new HashMap<>();
        Map<Object, Double> XIdot = new HashMap<>();
        double Xdotdot=0.0;

        for(Map.Entry<Object, AssociativeArray> entry1 : dataTable.entrySet()) {
            Object i = entry1.getKey();
            AssociativeArray row = entry1.getValue();
            
            for(Map.Entry<Object, Object> entry2 : row.entrySet()) {
                Object j = entry2.getKey();
                
                double v = TypeInference.toDouble(entry2.getValue());
                
                //Summing the columns
                if(XdotJ.containsKey(j)==false) {
                    XdotJ.put(j, v);
                }
                else {
                    XdotJ.put(j, XdotJ.get(j) + v);
                }

                //Summing the rows
                if(XIdot.containsKey(i)==false) {
                    XIdot.put(i, v);
                }
                else {
                    XIdot.put(i, XIdot.get(i) + v);
                }
                
                Xdotdot+=v;
            }
        }

        int k=XdotJ.size();
        int n=XIdot.size();

        //Calculating Qscore        
        double SumOfSquaredXdotJ=0.0;
        for(Double value : XdotJ.values()) {
            SumOfSquaredXdotJ+=value*value;
        }
        XdotJ=null;

        double SumOfSquaredXIdot=0;
        double SumOfXIdot=0;
        for(Double value : XIdot.values()) {
            SumOfSquaredXIdot+=value*value;
            SumOfXIdot+=value;
        }
        XIdot=null;

        double Qscore=(k-1.0)*(k*SumOfSquaredXdotJ-Xdotdot*Xdotdot)/(k*SumOfXIdot-SumOfSquaredXIdot);

        double pvalue = scoreToPvalue(Qscore, n, k);

        return pvalue;
    }

    /**
     * Tests the rejection of null Hypothesis for a particular confidence level 
     * 
     * @param dataTable
     * @param aLevel
     * @return 
     */
    public static boolean test(DataTable2D dataTable, double aLevel) {
        double pvalue= getPvalue(dataTable);

        boolean rejectH0=false;
        if(pvalue<=aLevel) {
            rejectH0=true; 
        }

        return rejectH0;
    }

    /**
     * Returns the Pvalue for a particular score
     * 
     * @param score
     * @param n
     * @param k
     * @return 
     */
    protected static double scoreToPvalue(double score, int n, int k) {
        if(n<4 || n*k<24) {
            //calculate it from tables too small values
            //EXPAND: waiting for tables from Dimaki
        }
        return 1.0-ContinuousDistributions.ChisquareCdf(score, k-1);
    }
}
