/**
 *    Copyright 2006-2018 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.generator.api.dom.utils;

/**
 * User:
 * Description:
 * Date: 2018-07-10
 * Time: 15:36
 */
public class MyStringUtils {

    /**
     * 首字母小写
     * @param str
     * @return
     */
    public static String uncapitalize(String str) {
        int strLen;
        if (str != null && (strLen = str.length()) != 0) {
            if(strLen == 1){
                return str.toLowerCase();
            }
            String target = str.substring(0,1);
            return target.toLowerCase() + str.substring(1,strLen);
        } else {
            return str;
        }
    }

    /**
     * @param str
     * @param length
     * @param padStr 仅仅支持单字节
     * @return
     */
    public static String rightPad(String str,int length,String padStr) {
        if(str== null){
            return null;
        }
        if(str.length() >= length){
            return str+" ";
        }
        StringBuilder stringBuilder = new StringBuilder(str);
        int appendStr =  length - str.length();
        for(int i =0;i<appendStr;i++){
            stringBuilder.append(padStr);
        }
        return stringBuilder.toString();
    }


}
