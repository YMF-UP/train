//
package com.first.train.generator.server;

import com.first.train.generator.util.DbUtil;
import com.first.train.generator.util.Field;
import com.first.train.generator.util.FreemarkerUtil;
import freemarker.template.TemplateException;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ServerGenerator{

    static String serverPath = "[module]/src/main/java/com/first/train/[module]/";
    static String pomPath = "generator/pom.xml";
    static String vuepath="web/src/views/main/";
    static boolean readOnly=false;
    static {
        new File(serverPath).mkdirs();
    }

    public static void  main(String[] args) throws Exception {
         //获取mybatis-generator
        String generatorPath = getGeneratorPath();
        // 比如generator-config-member.xml，得到module = member
        String module = generatorPath.replace("src/main/resources/generator-config-", "").replace(".xml", "");
        System.out.println("module: " + module);
        serverPath=serverPath.replace("[module]",module);
        System.out.println("servicePath: " + serverPath);

        // 读取table节点
        Document document = new SAXReader().read("generator/" + generatorPath);//'generator/'模块名
        Node table = document.selectSingleNode("//table");
        System.out.println(table);
        Node tableName = table.selectSingleNode("@tableName");//查属性
        Node domainObjectName = table.selectSingleNode("@domainObjectName");
        System.out.println(tableName.getText() + "/" + domainObjectName.getText());

        //读数据源
        Node connectionURL = document.selectSingleNode("//@connectionURL");
        Node userId = document.selectSingleNode("//@userId");
        Node password = document.selectSingleNode("//@password");
        System.out.println("url: "+connectionURL.getText());
        System.out.println("userID: "+userId.getText());
        System.out.println("password: "+password.getText());
        DbUtil.url = connectionURL.getText();
        DbUtil.user = userId.getText();
        DbUtil.password = password.getText();
        //表名
        String Domain = domainObjectName.getText();

        String domain = Domain.substring(0, 1).toLowerCase() + Domain.substring(1);

        String do_main = tableName.getText().replaceAll("_", "-");
//        // 表中文名
        String tableNameCn = DbUtil.getTableComment(tableName.getText());
        List<Field> fieldList = DbUtil.getColumnByTableName(tableName.getText());
        Set<String> typeSet = getJavaTypes(fieldList);

        // 组装参数:什么来着？ftl里面需要的
        Map<String, Object> param = new HashMap<>();
        param.put("Domain", Domain);
        param.put("domain", domain);
        param.put("do_main", do_main);
        param.put("tableNameCn", tableNameCn);
        param.put("fieldList", fieldList);
        param.put("typeSet", typeSet);
        param.put("module",module);
        param.put("readOnly",readOnly);
        System.out.println("组装参数：" + param);
        /*
         * 8.6 controller生成
         * 模板不一样，重构方法名。tar变量作为辨别，首先是模板，其次是生成的的java。
         * path要改变，变成自己的tar。shift+F6,重命名。//这个怎么改,改path
         * 生成拼接好的文件，mkdirs，防止不存在。
         * */
//        gen(Domain, param,"service","service");
//        gen(Domain, param,"controller","controller");
//        gen(Domain, param,"req","savereq");
//        gen(Domain, param,"req","queryreq");
//        gen(Domain, param,"resp","queryresp");
        genVue(do_main,param);

        /* 8.7实体类 新---25.12.10。有点复杂了，有点复写不出来。不过吗，值得思考。----实在复写不出来，算了看着视频敲吧。
        * 1.读数据源-表里面的字段--需要实体类来描述。对应的要设置数据源。
        * filed是存字段的，一张表会有很多字段，所以filed需要是list类型。
        * 2，因为要连接数据库，所以需要写一个传统链接。以及要获取table的字段，jdbc。获得表的注释即comment，set类型获得。
        *获得所有列的信息，filed类型吗？-对。是个数组
        *数据库类型要转成Java类型，如boolen对应Bollen。
        * 细节需要考虑，比如comment前的竖杠，以及下划线转驼峰，例如member_id转为memberid。
        * 为了获取表的注释以及表的列信息----传表名。
        * */

        /*8.7 实体类   本质是1.连数据库查字段  2.根据字段对应到自己需要的变量名称
        数据库表名--字段-----读数据源；需要读数据源--dbutil--三个属性值
        *filed.java--表里面的字段，list类型
        *表名-字段
         */

        /* 8.8 具体生成实体类
        * 1.按照要生成的实体类来确定类型，javatype之前获得的。每张表的字段不一样
        * 2.根据前面得到的判断类型，生成get和set，以及tostring。
        * 3.获取Java类型需要写新方法---set用来去重---set类型都为string
        * 4.组装参数，--看之前的来尝试写一下
        * 5.具体ftl--判断和循环--定义属性：其实就是获得的表字段
        * 6.生成时的包名和类名
        * */

        /*8.10 前端生成
        * 1.路径
        * 2.单独方法--前端
        * 3.调整前端ftl--选哪一个呢？---passenger.vue
        * */

    }

    private static void gen(String Domain, Map<String, Object> param,String packagename,String target) throws IOException, TemplateException {
        FreemarkerUtil.initConfig(target+".ftl");
        String toPath=serverPath+packagename+"/";//最终生成的位置，所以要加target
        new File(toPath).mkdirs();
        String Target = target.substring(0, 1).toUpperCase() + target.substring(1);
        String fileName = toPath + Domain + Target + ".java";
        System.out.println("开始生成：" + fileName);
        FreemarkerUtil.generator(fileName, param);//传的就是最终文件
    }
    private static void genVue(String do_main, Map<String, Object> param) throws IOException, TemplateException {
        FreemarkerUtil.initConfig("vue.ftl");

        new File(vuepath).mkdirs();
        String fileName = vuepath+do_main+ ".vue";
        System.out.println("开始生成：" + fileName);
        FreemarkerUtil.generator(fileName, param);//传的就是最终文件
    }


    private static String getGeneratorPath() throws DocumentException {
        SAXReader saxReader = new SAXReader();
        Map<String, String> map = new HashMap<String, String>();
        map.put("pom", "http://maven.apache.org/POM/4.0.0");
        saxReader.getDocumentFactory().setXPathNamespaceURIs(map);
        Document document = saxReader.read(pomPath);
        Node node = document.selectSingleNode("//pom:configurationFile");
        System.out.println(node.getText());
        return node.getText();
    }
    private static Set getJavaTypes(List<Field> fieldList){
        Set<String> set=new HashSet<>();
        for (int i = 0; i < fieldList.size(); i++) {
            Field field=fieldList.get(i);
            set.add(field.getJavaType());
        }
        return set;
    }

}



//import com.first.train.generator.util.DbUtil;
//import com.first.train.generator.util.Field;
//import com.first.train.generator.util.FreemarkerUtil;
//import freemarker.template.TemplateException;
//import org.dom4j.Document;
//import org.dom4j.DocumentException;
//import org.dom4j.Node;
//import org.dom4j.io.SAXReader;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.*;
//public class ServerGenerator {
//    static boolean readOnly = false;
//    static String vuePath = "admin/src/views/main/";
//    static String serverPath = "[module]/src/main/java/com/first/train/[module]/";
//    static String pomPath = "generator/pom.xml";
//    static String module = "";
//    // static {
//    //     new File(serverPath).mkdirs();
//    // }
//
//    public static void main(String[] args) throws Exception {
//        // 获取mybatis-generator
//        String generatorPath = getGeneratorPath();
//        // 比如generator-config-member.xml，得到module = member
//        module = generatorPath.replace("src/main/resources/generator-config-", "").replace(".xml", "");
//        System.out.println("module: " + module);
//        serverPath = serverPath.replace("[module]", module);
//        new File(serverPath).mkdirs();
//        System.out.println("servicePath: " + serverPath);
//
//        // 读取table节点
//        Document document = new SAXReader().read("generator/" + generatorPath);
//        Node table = document.selectSingleNode("//table");
//        System.out.println(table);
//        Node tableName = table.selectSingleNode("@tableName");
//        Node domainObjectName = table.selectSingleNode("@domainObjectName");
//        System.out.println(tableName.getText() + "/" + domainObjectName.getText());
//
//        // 为DbUtil设置数据源
//        Node connectionURL = document.selectSingleNode("//@connectionURL");
//        Node userId = document.selectSingleNode("//@userId");
//        Node password = document.selectSingleNode("//@password");
//        System.out.println("url: " + connectionURL.getText());
//        System.out.println("user: " + userId.getText());
//        System.out.println("password: " + password.getText());
//        DbUtil.url = connectionURL.getText();
//        DbUtil.user = userId.getText();
//        DbUtil.password = password.getText();
//
//        // 示例：表名 jiawa_test
//        // Domain = JiawaTest
//        String Domain = domainObjectName.getText();
//        // domain = jiawaTest
//        String domain = Domain.substring(0, 1).toLowerCase() + Domain.substring(1);
//        // do_main = jiawa-test
//        String do_main = tableName.getText().replaceAll("_", "-");
//        // 表中文名
//        String tableNameCn = DbUtil.getTableComment(tableName.getText());
//        List<Field> fieldList = DbUtil.getColumnByTableName(tableName.getText());
//        Set<String> typeSet = getJavaTypes(fieldList);
//
//        // 组装参数
//        Map<String, Object> param = new HashMap<>();
//        param.put("module", module);
//        param.put("Domain", Domain);
//        param.put("domain", domain);
//        param.put("do_main", do_main);
//        param.put("tableNameCn", tableNameCn);
//        param.put("fieldList", fieldList);
//        param.put("typeSet", typeSet);
//        param.put("readOnly", readOnly);
//        System.out.println("组装参数：" + param);
//
//        gen(Domain, param, "service", "service");
//        gen(Domain, param, "controller/admin", "adminController");
//        gen(Domain, param, "req", "saveReq");
//        gen(Domain, param, "req", "queryReq");
//        gen(Domain, param, "resp", "queryResp");
//
//        genVue(do_main, param);
//    }
//
//    private static void gen(String Domain, Map<String, Object> param, String packageName, String target) throws IOException, TemplateException {
//        FreemarkerUtil.initConfig(target + ".ftl");
//        String toPath = serverPath + packageName + "/";
//        new File(toPath).mkdirs();
//        String Target = target.substring(0, 1).toUpperCase() + target.substring(1);
//        String fileName = toPath + Domain + Target + ".java";
//        System.out.println("开始生成：" + fileName);
//        FreemarkerUtil.generator(fileName, param);
//    }
//
//    private static void genVue(String do_main, Map<String, Object> param) throws IOException, TemplateException {
//        FreemarkerUtil.initConfig("vue.ftl");
//        new File(vuePath + module).mkdirs();
//        String fileName = vuePath + module + "/" + do_main + ".vue";
//        System.out.println("开始生成：" + fileName);
//        FreemarkerUtil.generator(fileName, param);
//    }
//
//    private static String getGeneratorPath() throws DocumentException {
//        SAXReader saxReader = new SAXReader();
//        Map<String, String> map = new HashMap<String, String>();
//        map.put("pom", "http://maven.apache.org/POM/4.0.0");
//        saxReader.getDocumentFactory().setXPathNamespaceURIs(map);
//        Document document = saxReader.read(pomPath);
//        Node node = document.selectSingleNode("//pom:configurationFile");
//        System.out.println(node.getText());
//        return node.getText();
//    }
//
//        //获取所有的Java类型，使用Set去重
//
//
//    private static Set<String> getJavaTypes(List<Field> fieldList) {
//        Set<String> set = new HashSet<>();
//        for (int i = 0; i < fieldList.size(); i++) {
//            Field field = fieldList.get(i);
//            set.add(field.getJavaType());
//        }
//        return set;
//    }
//}
