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
package org.mybatis.generator.codegen.mybatis3.model;

import static org.mybatis.generator.internal.util.JavaBeansUtil.*;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.Plugin;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.lombok.ConstructorTypeEnums;
import org.mybatis.generator.api.dom.utils.MyStringUtils;
import org.mybatis.generator.codegen.AbstractJavaGenerator;
import org.mybatis.generator.codegen.RootClassInfo;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.JavaModelGeneratorConfiguration;
import org.mybatis.generator.internal.util.JavaBeansUtil;

/**
 * 
 * @author Jeff Butler
 * 
 */
public class BaseRecordGenerator extends AbstractJavaGenerator {

    private static final String DTO =  "DTO";

    private static final String CONVERT =  "Convert";

    private static final String LIST =  "List";

    private static final String LIST_PACKAGE =  "java.util.List";

    private static final String ARRAY_PACKAGE =  "java.util.ArrayList";

    private static final String BLANK_STR = "";

    private static final String MANAGER =  "Manager";

    private static final String SLF4J = "lombok.extern.slf4j.Slf4j";

    private static final String COMPONENT = " org.springframework.stereotype.Component";



    public BaseRecordGenerator() {
        super();
    }

    @Override
    public List<CompilationUnit> getCompilationUnits() {
        FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
        progressCallback.startTask(getString(
                "Progress.8", table.toString())); //$NON-NLS-1$
        Plugin plugins = context.getPlugins(); //容器全局插件
        //注释生成器
        CommentGenerator commentGenerator = context.getCommentGenerator();

        //设值类型信息 包名.类名
        String baseRecordType = introspectedTable.getBaseRecordType();
        //生成具体PO类
        TopLevelClass topLevelClass = getTopLevelClass(plugins, commentGenerator, baseRecordType);
        //加载插件
        List<CompilationUnit> answer = new ArrayList<CompilationUnit>();
        if (context.getPlugins().modelBaseRecordClassGenerated(topLevelClass, introspectedTable)) {
            answer.add(topLevelClass);
        }
        //获取targetPackage配置的上级包路径
        String dtoBaseRecordType = getPreviousPackage();
        //自定义生成DTO类名
        String domainObjectName = introspectedTable.getFullyQualifiedTable().getDomainObjectName();
        dtoBaseRecordType  =  dtoBaseRecordType +"."+DTO.toLowerCase()+"."+domainObjectName+DTO;
        TopLevelClass topLevelClassDTO = getTopLevelClass(plugins, commentGenerator, dtoBaseRecordType);
        answer.add(topLevelClassDTO);
        //自定义生成Convert
        TopLevelClass convertClazz = createConvertClazz(plugins, commentGenerator);
        answer.add(convertClazz);
        //自定义生成Manager
        TopLevelClass managerClazz = createManagerClazz(plugins, commentGenerator);
        answer.add(managerClazz);
        return answer;
    }

    private TopLevelClass createManagerClazz(Plugin plugins, CommentGenerator commentGenerator) {

        String domainObjectName = introspectedTable.getFullyQualifiedTable().getDomainObjectName();
        //生成类
        String baseRecordType = getPreviousPackage()+"."+MANAGER.toLowerCase()+"."+domainObjectName+MANAGER;
        TopLevelClass topLevelClassManager = getSimpleTopLevelClass(commentGenerator, baseRecordType);
        //Lombok和Spring支持
        commentGenerator.addLombokSLF4J(topLevelClassManager);
        commentGenerator.addSpringComponent(topLevelClassManager);


        Field field = new Field();
        field.setVisibility(JavaVisibility.PRIVATE);
        String mapperClazz = getPreviousPackage()+".mapper."+domainObjectName+"Mapper";
        FullyQualifiedJavaType fqjt = new FullyQualifiedJavaType(mapperClazz);
        field.setType(fqjt);
        field.setName(MyStringUtils.uncapitalize(fqjt.getShortName()));
        field.addJavaDocLine("@Autowired");
        topLevelClassManager.addField(field); //类添加全局变量
        topLevelClassManager.addImportedType("org.springframework.beans.factory.annotation");
        return topLevelClassManager;
    }



    private TopLevelClass createConvertClazz(Plugin plugins, CommentGenerator commentGenerator) {

        String domainObjectName = introspectedTable.getFullyQualifiedTable().getDomainObjectName();
        String baseRecordType =   getPreviousPackage()+"."+CONVERT.toLowerCase()+"."+domainObjectName+CONVERT;
        TopLevelClass topLevelClassConvert = getTopLevelConvertClass(plugins, commentGenerator, baseRecordType);
        topLevelClassConvert.addImportedType(LIST_PACKAGE); //导入 java.util.List
        topLevelClassConvert.addImportedType(ARRAY_PACKAGE); //导入 java.util.List
        //请求参数
        String poJavaName = introspectedTable.getBaseRecordType();                       //包名.TableName
        topLevelClassConvert.addImportedType(poJavaName);
        FullyQualifiedJavaType poJavaType = new FullyQualifiedJavaType(poJavaName);      //TableName对象
        String poShortName =poJavaType.getShortName();
        String po = MyStringUtils.uncapitalize(poJavaType.getShortName());              //tableName
        //返回参数
        String dtoJavaName = getPreviousPackage()+"."+DTO.toLowerCase()+"."+domainObjectName+DTO;//包名.TableName
        topLevelClassConvert.addImportedType(dtoJavaName);
        FullyQualifiedJavaType dtoJavaType = new FullyQualifiedJavaType(dtoJavaName);   //TableName对象
        String dtoShortName = dtoJavaType.getShortName();
        String dto = MyStringUtils.uncapitalize(dtoShortName); //tableName

        String toDTO = "toDTO"; //生成 toDTO(...)
        Method toDTOmethod = convertMethod(toDTO,poJavaType,po,dtoJavaType,dtoShortName,dto);
        topLevelClassConvert.addMethod(toDTOmethod);

        String toPO = "toPO"; //生成 toPo(...)
        Method method = convertMethod(toPO,dtoJavaType,dto,poJavaType,poShortName,po);
        topLevelClassConvert.addMethod(method);
        //生成 toDTOList(...)
        Method toDTOListMethod = convertListMethod(toDTO, poJavaType, dtoJavaType);
        topLevelClassConvert.addMethod(toDTOListMethod);
        //生成 toPOList(...)
        Method toPOListMethod = convertListMethod(toPO, dtoJavaType,poJavaType);
        topLevelClassConvert.addMethod(toPOListMethod);
        return topLevelClassConvert;
    }


    public static Method convertListMethod(String methodName,FullyQualifiedJavaType requestJavaType,FullyQualifiedJavaType returnJavaType){

        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setStatic(true);
        method.setName(methodName+"List");
        //入参
        FullyQualifiedJavaType requestParam = new FullyQualifiedJavaType(LIST);
        requestParam.addTypeArgument(requestJavaType);
        String request = MyStringUtils.uncapitalize(requestJavaType.getShortName());
        String requestListName = request+LIST;
        Parameter parameter = new Parameter(requestParam,requestListName);
        method.addParameter(parameter);
        //返参
        FullyQualifiedJavaType reponseParam = new FullyQualifiedJavaType(LIST);
        reponseParam.addTypeArgument(returnJavaType);
        String response = MyStringUtils.uncapitalize(returnJavaType.getShortName());
        method.setReturnType(reponseParam);

        String responeListName = response+LIST;
        method.addBodyLine("List<"+returnJavaType.getShortName()+"> "+responeListName+" = new ArrayList<>();");
        method.addBodyLine("if("+requestListName+" != null){");
        method.addBodyLine(BLANK_STR+"for("+requestJavaType.getShortName()+" "+request+" : "+requestListName+"){");
        method.addBodyLine(BLANK_STR+BLANK_STR+responeListName+".add("+methodName+"("+request+"));");
        method.addBodyLine(BLANK_STR+"}");
        method.addBodyLine("}");
        //返回
        StringBuilder returnSB = new StringBuilder();
        returnSB.append("return "); //$NON-NLS-1$
        returnSB.append(responeListName);
        returnSB.append(";");
        method.addBodyLine(returnSB.toString());
        return method;

    }





    private Method convertMethod(String methodName,
                                 FullyQualifiedJavaType requestJavaType,
                                 String request,
                                 FullyQualifiedJavaType returnJavaType,
                                 String responseShortName,
                                 String response) {
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setStatic(true);
        method.setName(methodName);

        Parameter parameter = new Parameter(requestJavaType,request);
        method.addParameter(parameter);
        method.addJavaDocLine("/**"); //$NON-NLS-1$
        method.addJavaDocLine("* @param "+request);
        method.addJavaDocLine("* @return "+request+"为null则返回null");
        method.addJavaDocLine("*/"); //$NON-NLS-1$

        method.setReturnType(returnJavaType);

        StringBuilder sb = new StringBuilder(); //方法体
        sb.append(responseShortName+ " " + response + " = " + "new " +responseShortName+"();");
        method.addBodyLine(sb.toString());
        //获取本类所有字段
        List<IntrospectedColumn> introspectedColumns = getColumnsInThisClass();
        for(IntrospectedColumn introspectedColumn : introspectedColumns){
            String setStr = JavaBeansUtil.getSetterMethodName(introspectedColumn.getJavaProperty());
            String getStr = JavaBeansUtil.getGetterMethodName(introspectedColumn.getJavaProperty(),
                    introspectedColumn.getFullyQualifiedJavaType());
            method.addBodyLine(response+"."+setStr+"("+request+"."+getStr+"());");
        }
        //返回
        StringBuilder returnSB = new StringBuilder();
        returnSB.append("return "); //$NON-NLS-1$
        returnSB.append(response);
        returnSB.append(";");
        method.addBodyLine(returnSB.toString());
        return method;
    }

    private TopLevelClass getTopLevelConvertClass(Plugin plugins, CommentGenerator commentGenerator, String baseRecordType) {
        //通用生成类方法
        TopLevelClass topLevelClass = getSimpleTopLevelClass(commentGenerator, baseRecordType);
        //添加Lombok Constructor构造函数
        commentGenerator.addLombokConstructor(topLevelClass, ConstructorTypeEnums.NOARGS,JavaVisibility.PRIVATE);
        return topLevelClass;
    }


    /**
     *获取targetPackage配置的上级包路径
     * @return
     */
    private String getPreviousPackage(){
        String dtoBaseRecordType;
        JavaModelGeneratorConfiguration javaModelGeneratorConfiguration = context.getJavaModelGeneratorConfiguration();
        String targetPackage = javaModelGeneratorConfiguration.getTargetPackage();
        if(targetPackage.lastIndexOf(".") != -1){
            dtoBaseRecordType = targetPackage.substring(0,targetPackage.lastIndexOf("."));
        }else{
            dtoBaseRecordType = targetPackage;
        }
        return dtoBaseRecordType;
    }

    /**
     * 生成PO/DTO共同方法
     * @param plugins
     * @param commentGenerator
     * @param baseRecordType
     * @return
     */
    private TopLevelClass getTopLevelClass(Plugin plugins, CommentGenerator commentGenerator, String baseRecordType) {
        //通用生成类方法
        TopLevelClass topLevelClass = getSimpleTopLevelClass(commentGenerator, baseRecordType);
        //类头注释
        commentGenerator.addModelClassComment(topLevelClass, introspectedTable);
        //Lombok表达式
        commentGenerator.addLombokData(topLevelClass);
        commentGenerator.addLombokToString(topLevelClass,Boolean.FALSE);

        //获取本类所有字段
        List<IntrospectedColumn> introspectedColumns = getColumnsInThisClass();
        //table属性 immutable 是否true(是否添加构造函数)
        if (introspectedTable.isConstructorBased()) {
            addParameterizedConstructor(topLevelClass, introspectedTable.getNonBLOBColumns());
            if (includeBLOBColumns()) {
                addParameterizedConstructor(topLevelClass, introspectedTable.getAllColumns());
            }
            if (!introspectedTable.isImmutable()) { //这代码永远都不执行？
                addDefaultConstructor(topLevelClass);
            }
        }
        String rootClass = getRootClass(); //获取父类
        //迭代所有字段(生成全局变量)
        for (IntrospectedColumn introspectedColumn : introspectedColumns) {
            if (RootClassInfo.getInstance(rootClass, warnings).containsProperty(introspectedColumn)) {
                continue;
            }
            //生成全局变量
            Field field = getJavaBeansField(introspectedColumn, context, introspectedTable);
            if (plugins.modelFieldGenerated(field, topLevelClass,
                    introspectedColumn, introspectedTable,
                    Plugin.ModelClassType.BASE_RECORD)) {
                topLevelClass.addField(field); //类添加全局变量
                topLevelClass.addImportedType(field.getType()); //全局变量导包
            }
            //get set用 lombok.Data替换
             /*//生成get
           Method method = getJavaBeansGetter(introspectedColumn, context, introspectedTable);
            if (plugins.modelGetterMethodGenerated(method, topLevelClass,
                    introspectedColumn, introspectedTable,
                    Plugin.ModelClassType.BASE_RECORD)) {
                topLevelClass.addMethod(method);
            }

            //生成set
            if (!introspectedTable.isImmutable()) {
                method = getJavaBeansSetter(introspectedColumn, context, introspectedTable);
                if (plugins.modelSetterMethodGenerated(method, topLevelClass,
                        introspectedColumn, introspectedTable,
                        Plugin.ModelClassType.BASE_RECORD)) {
                    topLevelClass.addMethod(method);
                }
            }*/
        }
        return topLevelClass;
    }

    /**
     * 通用生成类方法
     * @param commentGenerator
     * @param baseRecordType
     * @return
     */
    private TopLevelClass getSimpleTopLevelClass(CommentGenerator commentGenerator, String baseRecordType) {
        //设值类型信息 包名.类名
        FullyQualifiedJavaType type = new FullyQualifiedJavaType(baseRecordType);
        //生成实现类
        TopLevelClass topLevelClass = new TopLevelClass(type);
        topLevelClass.setVisibility(JavaVisibility.PUBLIC); //类访问级别是public
        commentGenerator.addJavaFileComment(topLevelClass);

        FullyQualifiedJavaType superClass = getSuperClass();
        if (superClass != null) { //设值父类
            topLevelClass.setSuperClass(superClass);
            topLevelClass.addImportedType(superClass);
        }
        return topLevelClass;
    }

    private FullyQualifiedJavaType getSuperClass() {
        FullyQualifiedJavaType superClass;
        if (introspectedTable.getRules().generatePrimaryKeyClass()) {
            superClass = new FullyQualifiedJavaType(introspectedTable
                    .getPrimaryKeyType());
        } else {
            String rootClass = getRootClass();
            if (rootClass != null) {
                superClass = new FullyQualifiedJavaType(rootClass);
            } else {
                superClass = null;
            }
        }

        return superClass;
    }

    private boolean includePrimaryKeyColumns() {
        return !introspectedTable.getRules().generatePrimaryKeyClass()
                && introspectedTable.hasPrimaryKeyColumns();
    }

    private boolean includeBLOBColumns() {
        return !introspectedTable.getRules().generateRecordWithBLOBsClass()
                && introspectedTable.hasBLOBColumns();
    }

    private void addParameterizedConstructor(TopLevelClass topLevelClass, List<IntrospectedColumn> constructorColumns) {
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setConstructor(true);
        method.setName(topLevelClass.getType().getShortName());
        context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);

        for (IntrospectedColumn introspectedColumn : constructorColumns) {
            method.addParameter(new Parameter(introspectedColumn.getFullyQualifiedJavaType(),
                    introspectedColumn.getJavaProperty()));
            topLevelClass.addImportedType(introspectedColumn.getFullyQualifiedJavaType());
        }

        StringBuilder sb = new StringBuilder();
        List<String> superColumns = new LinkedList<String>();
        if (introspectedTable.getRules().generatePrimaryKeyClass()) {
            boolean comma = false;
            sb.append("super("); //$NON-NLS-1$
            for (IntrospectedColumn introspectedColumn : introspectedTable.getPrimaryKeyColumns()) {
                if (comma) {
                    sb.append(", "); //$NON-NLS-1$
                } else {
                    comma = true;
                }
                sb.append(introspectedColumn.getJavaProperty());
                superColumns.add(introspectedColumn.getActualColumnName());
            }
            sb.append(");"); //$NON-NLS-1$
            method.addBodyLine(sb.toString());
        }

        for (IntrospectedColumn introspectedColumn : constructorColumns) {
            if (!superColumns.contains(introspectedColumn.getActualColumnName())) {
                sb.setLength(0);
                sb.append("this."); //$NON-NLS-1$
                sb.append(introspectedColumn.getJavaProperty());
                sb.append(" = "); //$NON-NLS-1$
                sb.append(introspectedColumn.getJavaProperty());
                sb.append(';');
                method.addBodyLine(sb.toString());
            }
        }

        topLevelClass.addMethod(method);
    }

    private List<IntrospectedColumn> getColumnsInThisClass() {
        List<IntrospectedColumn> introspectedColumns;
        if (includePrimaryKeyColumns()) {
            if (includeBLOBColumns()) {
                introspectedColumns = introspectedTable.getAllColumns();
            } else {
                introspectedColumns = introspectedTable.getNonBLOBColumns();
            }
        } else {
            if (includeBLOBColumns()) {
                introspectedColumns = introspectedTable
                        .getNonPrimaryKeyColumns();
            } else {
                introspectedColumns = introspectedTable.getBaseColumns();
            }
        }

        return introspectedColumns;
    }
}
