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
package com.datumbox.framework.machinelearning.common.bases.baseobjects;

import com.datumbox.common.objecttypes.Learnable;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;
import com.datumbox.common.persistentstorage.interfaces.BigMap;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * ModelParameter类的基类，通过反射自动初始化所有的BigMap域<br>
 * Base class for every ModelParameter class in the framework. It automatically
 * initializes all the BidMap fields by using reflection.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public abstract class BaseModelParameters implements Learnable {
    //number of data points used for training
    /**
     * 所有的数据量
     */
    private Integer n = 0;

    //number of features in data points used for training
    /**
     * 数据点中的特征数
     */
    private Integer d = 0;
        
    /**
     * 使用DatabaseConnector的构造函数
     * Protected constructor which accepts as argument the DatabaseConnector.
     * 
     * @param dbc 
     */
    public BaseModelParameters(DatabaseConnector dbc) {
        //Initialize all the BigMap fields
        //初始化所有的BigMap域
        bigMapInitializer(dbc);
    }

    /**
     * 获取训练实例数<br>
     * Getter for the total number of records used in training.
     * 
     * @return 
     */
    public Integer getN() {
        return n;
    }

    /**
     * 设置训练实例数<br>
     * Setter for the total number of records used in training.
     * 
     * @param n 
     */
    protected void setN(Integer n) {
        this.n = n;
    }

    /**
     * 获取样本的维度数<br>
     * Getter for the dimension of the dataset used in training.
     * 
     * @return 
     */
    public Integer getD() {
        return d;
    }

    /**
     * 设置样本维度数<br>
     * Setter for the dimension of the dataset used in training.
     * 
     * @param d 
     */
    protected void setD(Integer d) {
        this.d = d;
    }

    /**
     * 初始化所有被标记为BigMap的域
     * Initializes all the fields of the class which are marked with the BigMap
     * annotation automatically.
     * 
     * @param dbc 
     */
    private void bigMapInitializer(DatabaseConnector dbc) {
        //get all the fields from all the inherited classes
        //获取所有父类的域
        for(Field field : getAllFields(new LinkedList<>(), this.getClass())){
            
            //if the field is annotated with BigMap
            //如果这个域是个BigMap
            if (field.isAnnotationPresent(BigMap.class)) {
                field.setAccessible(true);
                
                try {
                    //call the getBigMap method to load it
                    field.set(this, dbc.getBigMap(field.getName(), false));
                } 
                catch (IllegalArgumentException | IllegalAccessException ex) {
                    throw new RuntimeException(ex);
                }
                
            }
        }
    }
    
    /**
     * 递归地获取所有父类的域<br>
     * Gets all the fields recursively from all the parent classes.
     * 
     * @param fields 储存
     * @param type 类
     * @return 
     */
    private List<Field> getAllFields(List<Field> fields, Class<?> type) {
        fields.addAll(Arrays.asList(type.getDeclaredFields()));

        if (type.getSuperclass() != null) {
            fields = getAllFields(fields, type.getSuperclass());
        }

        return fields;
    }
}
