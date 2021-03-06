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
package com.datumbox.common.utilities;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * This class contains a number of convenience methods which have an API similar
 * to PHP functions and their are implemented in Java.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class PHPfunctions {
    
    private static final Pattern LTRIM = Pattern.compile("^\\s+");
    private static final Pattern RTRIM = Pattern.compile("\\s+$");
    
    /**
     * Trims spaces on the left.
     * 
     * @param s
     * @return 
     */
    public static String ltrim(String s) {
        return LTRIM.matcher(s).replaceAll("");
    }
    
    /**
     * Trims spaces on the right.
     * 
     * @param s
     * @return 
     */
    public static String rtrim(String s) {
        return RTRIM.matcher(s).replaceAll("");
    }    
    
    /**
     * Count the number of substring occurrences.
     * 
     * @param string
     * @param substring
     * @return 
     */
    public static int substr_count(final String string, final String substring) {
        if(substring.length()==1) {
            return substr_count(string, substring.charAt(0));
        }
        
        int count = 0;
        int idx = 0;

        while ((idx = string.indexOf(substring, idx)) != -1) {
           ++idx;
           ++count;
        }

        return count;
    }
    
    /**
     * Count the number of times a character appears in the string.
     * 
     * @param string
     * @param character
     * @return 
     */
    public static int substr_count(final String string, final char character) {
        int count = 0;
        
        for(char c : string.toCharArray()) {
            if(c==character) {
                ++count;
            }
        }

        return count;
    }
    
    /**
     * Matches a string with a regex and replaces the matched components with 
     * a provided string.
     * 
     * @param regex
     * @param replacement
     * @param subject
     * @return 
     */
    public static String preg_replace(String regex, String replacement, String subject) {
        Pattern p = Pattern.compile(regex);
        return preg_replace(p, replacement, subject);
    }
    
    /**
     * Matches a string with a pattern and replaces the matched components with 
     * a provided string.
     * 
     * @param pattern
     * @param replacement
     * @param subject
     * @return 
     */
    public static String preg_replace(Pattern pattern, String replacement, String subject) {
        Matcher m = pattern.matcher(subject);
        StringBuffer sb = new StringBuffer(subject.length());
        while(m.find()){ 
            m.appendReplacement(sb, replacement);
        }
        m.appendTail(sb);
        return sb.toString();
    }
    
    /**
     * Matches a string with a regex.
     * 
     * @param regex
     * @param subject
     * @return 
     */
    public static int preg_match(String regex, String subject) {
        Pattern p = Pattern.compile(regex);
        return preg_match(p, subject);
    }
    
    /**
     * Matches a string with a pattern.
     * 
     * @param pattern
     * @param subject
     * @return 
     */
    public static int preg_match(Pattern pattern, String subject) {
        int matches=0;
        Matcher m = pattern.matcher(subject);
        while(m.find()){ 
            ++matches;
        }
        return matches;
    }
    
    /**
     * Rounds a number to a specified precision.
     * 
     * @param d
     * @param i
     * @return 
     */
    public static double round(double d, int i) {
        double multiplier = Math.pow(10, i);
        return Math.round(d*multiplier)/multiplier;
    }
    
    /**
     * Returns the logarithm of a number at an arbitrary base.
     * 
     * @param d
     * @param base
     * @return 
     */
    public static double log(double d, double base) {
        if(base==1.0 || base<=0.0) {
            throw new RuntimeException("Invalid base for logarithm");
        }
        return Math.log(d)/Math.log(base);
    }
    
    /**
     * Returns a random positive integer
     * 
     * @return 
     */
    public static int mt_rand() {
        return PHPfunctions.mt_rand(0,Integer.MAX_VALUE);
    }
    
    /**
     * Returns a random integer between min and max
     * 
     * @param min
     * @param max
     * @return 
     */
    public static int mt_rand(int min, int max) {
        return min + (int)(RandomGenerator.getThreadLocalRandom().nextDouble() * ((max - min) + 1));
    }
    
    
    /**
     * Returns a random double between min and max
     * 
     * @param min
     * @param max
     * @return 
     */
    public static double mt_rand(double min, double max) {
        return min + (RandomGenerator.getThreadLocalRandom().nextDouble() * (max - min));
    }
    
    /**
     * It flips the key and values of a map.
     * 
     * @param <K>
     * @param <V>
     * @param map
     * @return 
     */
    public static <K,V> Map<V,K> array_flip(Map<K,V> map) {
        Map<V,K> flipped = new HashMap<>();
        for(Map.Entry<K,V> entry : map.entrySet()) {
            flipped.put(entry.getValue(), entry.getKey());
        }
        return flipped;
    }
    
    /**
     * Shuffles the values of any array in place.
     * 
     * @param <T>
     * @param array 
     */
    public static <T> void shuffle(T[] array) {
        //Implementing Fisher-Yates shuffle
        T tmp;
        Random rnd = RandomGenerator.getThreadLocalRandom();
        for (int i = array.length - 1; i > 0; --i) {
            int index = rnd.nextInt(i + 1);
            
            tmp = array[index];
            array[index] = array[i];
            array[i] = tmp;
        }
    }
    
    /**
     * Returns the contexts of an Objects in a human readable format.
     * 
     * @param <T>
     * @param object
     * @return 
     */
    public static <T> String var_export(T object) {
        return ToStringBuilder.reflectionToString(object);
    }
    
    /**
     * Sorts an array in ascending order and returns an array with indexes of 
     * the original order.
     * 
     * @param <T>
     * @param array
     * @return 
     */
    public static <T extends Comparable<T>> Integer[] asort(T[] array) {
        //sort the indexes first
        ArrayIndexComparator<T> comparator = new ArrayIndexComparator<>(array);
        Integer[] indexes = comparator.createIndexArray();
        Arrays.sort(indexes, comparator);
        
        //sort the array based on the indexes
        //sortArrayBasedOnIndex(array, indexes);
        Arrays.sort(array);
        
        return indexes;
    }
    
    /**
     * Sorts an array in descending order and returns an array with indexes of 
     * the original order.
     * 
     * @param <T>
     * @param array
     * @return 
     */
    public static <T extends Comparable<T>> Integer[] arsort(T[] array) {
        //sort the indexes first
        ArrayIndexReverseComparator<T> comparator = new ArrayIndexReverseComparator<>(array);
        Integer[] indexes = comparator.createIndexArray();
        Arrays.sort(indexes, comparator);
        
        //sort the array based on the indexes
        Arrays.sort(array,Collections.reverseOrder());
        
        return indexes;
    }
    
}

/*
 * Modified code found at:
 * http://stackoverflow.com/questions/4859261/get-the-indices-of-an-array-after-sorting
 */
class ArrayIndexComparator<T extends Comparable<T>> implements Comparator<Integer> {
    protected final T[] array;

    protected ArrayIndexComparator(T[] array) {
        this.array = array;
    }

    protected Integer[] createIndexArray() {
        Integer[] indexes = new Integer[array.length];
        for (int i = 0; i < array.length; ++i) {
            indexes[i] = i;
        }
        return indexes;
    }

    @Override
    public int compare(Integer index1, Integer index2) {
        return array[index1].compareTo(array[index2]);
    }
}

class ArrayIndexReverseComparator<T extends Comparable<T>> extends ArrayIndexComparator<T> {

    protected ArrayIndexReverseComparator(T[] array) {
        super(array);
    }
    
    @Override
    public int compare(Integer index1, Integer index2) {
        return array[index2].compareTo(array[index1]);
    }
}
