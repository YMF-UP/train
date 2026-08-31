package com.first.train.generator.util;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class FreemarkerUtil {

    static String ftlPath = "generator/src/main/java/com/first/train/generator/ftl/";

    static Template temp;

    /**
     * 读模板
     */
    public static void  initConfig(String ftlName) throws IOException {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_31);
        cfg.setDefaultEncoding("UTF-8");
        cfg.setDirectoryForTemplateLoading(new File(ftlPath));
        cfg.setObjectWrapper(new DefaultObjectWrapper(Configuration.VERSION_2_3_31));
        temp = cfg.getTemplate(ftlName, "UTF-8");
    }

    /**
     * 根据模板，生成文件
     */
    public static void generator(String fileName, Map<String, Object> map) throws IOException, TemplateException {
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), StandardCharsets.UTF_8));
        temp.process(map, bw);
        bw.flush();
        bw.close();
    }
}
