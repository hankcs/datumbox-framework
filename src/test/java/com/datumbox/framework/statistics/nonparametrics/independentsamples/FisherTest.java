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
package com.datumbox.framework.statistics.nonparametrics.independentsamples;


import com.datumbox.tests.bases.BaseTest;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class FisherTest extends BaseTest {
    
    /**
     * Test of test method, of class Fisher.
     */
    @Test
    public void testTest() {
        logger.info("test");
        //Example from Mpesmpeas Notes, rejests null hypothesis
        int n11 = 1;
        int n12 = 5;
        int n21 = 4;
        int n22 = 0;
        double aLevel = 0.05;
        boolean expResult = true;
        boolean result = Fisher.test(n11, n12, n21, n22, aLevel);
        assertEquals(expResult, result);
    }
    
}
