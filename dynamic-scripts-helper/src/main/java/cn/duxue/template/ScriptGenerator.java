package cn.duxue.template;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.Version;

import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 从指定目录加载模板
 *
 * @author Tony
 * 2024-03-08
 * refer to: https://zhuanlan.zhihu.com/p/573968240
 */
public class ScriptGenerator {
    private final static String VERSION = "2.3.32";
    private static ConcurrentHashMap<String, Template> templates = new ConcurrentHashMap<>();

    /**
     * 初始化组件
     *
     * @param templateLoadingWay 加载方式
     * @param templateFolder     放置模板文件的目录
     * @param templateContents   模板数据
     */
    public static synchronized void init(TemplateLoadingWay templateLoadingWay, String templateFolder, Map<String, String> templateContents) {
        if (templateLoadingWay == TemplateLoadingWay.FILE) {
            loadTemplatesFromFileSystem(templateFolder);
        } else if (templateLoadingWay == TemplateLoadingWay.STRING) {
            loadTemplatesFromString(templateContents);
        } else {

        }
        System.out.println("模板加载成功......");
        // todo 监控目录变化

    }

    private static synchronized void loadTemplatesFromFileSystem(String templateFolder) {
        Configuration cfg = new Configuration(new Version(VERSION));
        cfg.setDefaultEncoding("utf-8");
        try {
            cfg.setDirectoryForTemplateLoading(new File("dsl-templates"));
        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.exit(-1);
        }

        // 将所有模板加载到内存（模板名称： ID.ftl）
        File folder = new File("dsl-templates");
        if (folder.exists() && folder.isDirectory()) {
            synchronized (templates) {
                File[] files = folder.listFiles();
                templates.clear();
                for (File f : files) {
                    if (f.getName().endsWith(".ftl")) {
                        try {
                            String fn = f.getName();
                            Template template = cfg.getTemplate(f.getName(), "utf-8");
                            String id = fn.replace(".ftl", "");
                            templates.put(id, template);
                        } catch (IOException e) {
                            System.out.println(e.getMessage());
                        }
                    }
                }
            }

        }
    }

    /**
     * 从字符串加载模板
     *
     * @param content Map,key为ID，value为模板内容
     */
    private static synchronized void loadTemplatesFromString(Map<String, String> content) {
        synchronized (templates) {
            Configuration cfg = new Configuration(new Version(VERSION));
            cfg.setDefaultEncoding("utf-8");
            if (!content.isEmpty()) {
                templates.clear();
            }
            StringTemplateLoader loader = new StringTemplateLoader();
            for (String id : content.keySet()) {
                loader.putTemplate(id + ".ftl", content.get(id));
            }
            cfg.setTemplateLoader(loader);
            for (String id : content.keySet()) {
                try {
                    Template template = cfg.getTemplate(id + ".ftl", "utf-8");
                    templates.put(id, template);
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
            }
        }
    }

    /**
     * 生成DSL文本
     *
     * @param params     参数（Map<String,Object>）
     * @param templateId 模板ID （模板ID==文件名，去掉.ftl）
     * @return DSL语句
     * @throws TemplateException
     * @throws IOException
     */
    public static String genDSL(Map<String, Object> params, String templateId) throws TemplateException, IOException {
        StringWriter sw = new StringWriter();
        templates.get(templateId).process(params, sw);
        String rslt = sw.toString();
        sw.close();
        return rslt;
    }

    /**
     * \
     * 模板转换工具
     *
     * @param templateContent 模板内容
     * @param params          参数
     * @return true:成功； false：失败
     */
    public static String convert(String templateContent, Map<String, Object> params) {
        try {
            Configuration cfg = new Configuration(new Version(VERSION));
            StringTemplateLoader loader = new StringTemplateLoader();
            StringWriter sw = new StringWriter();
            loader.putTemplate("test_template.ftl", templateContent);
            cfg.setTemplateLoader(loader);
            Template t = cfg.getTemplate("test_template.ftl");
            t.process(params, sw);
            String rslt = sw.toString();
            sw.close();
            return rslt;
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return null;
        }
    }
}

enum TemplateLoadingWay {
    STRING, FILE
}