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


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 通过序列化和反序列化创建一个对象的深度拷贝<br>
 * Creates a Deep Copy of an object by serializing and deserializing it.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class DeepCopy {
    
    /**
     * Serialized the Object to byte array.
     * 
     * @param obj
     * @return 
     */
    public static byte[] serialize(Object obj) {
        try(ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(obj);
            return bos.toByteArray();
        } 
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * Deserializes the byte array.
     * 
     * @param arr
     * @return 
     */
    public static Object deserialize(byte[] arr) {
        try (InputStream bis = new ByteArrayInputStream(arr);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            return ois.readObject();
        } 
        catch (IOException | ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * Deep clone Object by serialization and deserialization.
     * 
     * @param <T>
     * @param obj
     * @return 
     */
    @SuppressWarnings("unchecked")
    public static <T> T clone(T obj) {
        return (T)deserialize(serialize((Object)obj));
    }
}
