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
package com.datumbox.framework.statistics.timeseries;

import com.datumbox.common.dataobjects.FlatDataList;

/**
 * This class provides several Smoothing and moving average methods.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class Smoothing {
    
    /**
     * Simple Moving Average
     * 
     * @param flatDataList
     * @param N
     * @return 
     */
    public static double simpleMovingAverage(FlatDataList flatDataList, int N) {
        double SMA=0;

        int counter=0;
        for(int i=flatDataList.size()-1;i>=0;--i) {
            double Yti = flatDataList.getDouble(i);
            if(counter>=N) {
                break;
            }
            SMA+=Yti; //possible numeric overflow 
            ++counter;
        }

        SMA/=counter;

        return SMA;
    }
    
    /**
     * Simple Moving Average: Calculates Ft+1 by using previous results. A quick version of the simpleMovingAverage
     * 
     * @param Yt
     * @param YtminusN
     * @param Ft
     * @param N
     * @return 
     */
    public static double simpleMovingAverageQuick(double Yt, double YtminusN, double Ft, int N) {
        double SMA=(Yt-YtminusN)/N+Ft;

        return SMA;
    }
    
    /**
     * Weighted Moving Average
     * 
     * @param flatDataList
     * @param N
     * @return 
     */
    public static double weightedMovingAverage(FlatDataList flatDataList, int N) {
        double WMA=0;

        double denominator=0.0;
        int counter=0;
        for(int i=flatDataList.size()-1;i>=0;--i) {
            double Yti = flatDataList.getDouble(i);
            
            if(counter>=N) {
                break;
            }

            double weight=(N-counter);
            WMA+=weight*Yti; //possible numeric overflow 

            denominator+=weight;
            ++counter;
        }

        WMA/=denominator;

        return WMA;
    }
    
    /**
     * Simple Explonential Smoothing
     * 
     * @param flatDataList
     * @param a
     * @return 
     */
    public static double simpleExponentialSmoothing(FlatDataList flatDataList, double a) {
        double EMA=0;
        int count=0;
        for(int i=flatDataList.size()-1;i>=0;--i) {
            double Yti = flatDataList.getDouble(i);
            EMA+=a*Math.pow(1-a,count)*Yti;

            ++count;
        }

        return EMA;
    }
    
    /**
     * Simple Explonential Smoothing: Calculates Ft+1 by using previous results. A quick version of the simpleExponentialSmoothing
     * 
     * @param Ytminus1
     * @param Stminus1
     * @param a
     * @return 
     */
    public static double simpleExponentialSmoothingQuick(double Ytminus1, double Stminus1, double a) {
        double EMA=a*Ytminus1+(1-a)*Stminus1;

        return EMA;
    }

    /**
     * Smooth supplied timeline internalData 3 ways - overall, by trend and by season. 
     * Ported from https://github.com/ianbarber/PHPIR/blob/master/holtwinters.php
     * 
     * @param flatDataList - list of internalData
     * @param season_length - the number of entries that represent a 'season'. example = 7
     * @param alpha - internalData smoothing factor. example = 0.2
     * @param beta - trend smoothing factor. example = 0.01
     * @param gamma - seasonality smoothing factor. example = 0.01
     * @param dev_gamma - smoothing factor for deviations. example = 0.1
     * @return 
     */
    public static double holtWintersSmoothing(FlatDataList flatDataList, int season_length, double alpha, double beta, double gamma, double dev_gamma) {
        int n = flatDataList.size();
        // Calculate an initial trend level
        double trend1 = 0.0;
        for(int i = 0; i < season_length; ++i) {
            trend1 += flatDataList.getDouble(i);
        }
        trend1 /= season_length;

        double trend2 = 0.0;
        for(int i = season_length; i < 2*season_length; ++i) {
            trend2 += flatDataList.getDouble(i);
        }
        trend2 /= season_length;

        double initial_trend = (trend2 - trend1) / season_length;

        // Take the first value as the initial level
        double initial_level = flatDataList.getDouble(0);
        
        // Build index
        double[] index = new double[n];
        for(int i = 0; i<n; ++i) {
            double val = flatDataList.getDouble(i);
            index[i] = val / (initial_level + (i + 1.0) * initial_trend);
        }

        // Build season buffer
        double[] season = new double[n+season_length];
        double sum=0.0;
        for(int i = 0; i < season_length; ++i) {
            season[i] = (index[i] + index[i+season_length]) / 2;
            sum+=season[i];
        }
        index=null;

        // Normalise season
        double season_factor = season_length / sum;
        for(int i = 0; i < season_length; ++i) {
            season[i] *= season_factor;
        }

        double alpha_level = initial_level;
        double beta_trend = initial_trend;
        int i = 0;
        for(; i < n; ++i) {
            double value = flatDataList.getDouble(i);
            double temp_level = alpha_level;
            double temp_trend = beta_trend;

            alpha_level = alpha * value / season[i] + (1.0 - alpha) * (temp_level + temp_trend);
            beta_trend = beta * (alpha_level - temp_level) + ( 1.0 - beta ) * temp_trend;

            season[i + season_length] = gamma * value / alpha_level + (1.0 - gamma) * season[i];
        }

        double holtWinterTplus1=alpha_level + beta_trend * season[i + 1];

        return holtWinterTplus1;
    }

}
