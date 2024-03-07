package cn.duxue.template;

import freemarker.template.TemplateException;
import junit.framework.TestCase;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DSLGeneratorTest extends TestCase {

    public void setUp() throws Exception {
        super.setUp();
        // 从文件系统加载
//        DSLGenerator.init(TemplateLoadingWay.FILE,"dsl-templates",null);
        // 从文本数据加载
        Map<String, String> data = new HashMap<>();
        data.put("1", "{\n" +
                "\"query\": {\n" +
                "${abc}\n" +
                "}\n" +
                "}");
        data.put("2", "{\n" +
                "\"query\": {\n" +
                "${name}\n" +
                "}\n" +
                "}");
        ScriptGenerator.init(TemplateLoadingWay.STRING, null, data);
    }

    public void testGenDSL() {
        Map<String, Object> params = new HashMap<>();
        params.put("abc", "Tom");
        params.put("name", "Jack");
        try {
//            System.out.println(DSLGenerator.genDSL(params,"sea-tams-query"));
            System.out.println(ScriptGenerator.genDSL(params, "2"));
            System.out.println(ScriptGenerator.genDSL(params, "1"));
        } catch (TemplateException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void testConvert() {
        Map<String, Object> params = new HashMap<>();
        params.put("abc", "Tom");
        String templateStr = "{\n" +
                "\"query\": {\n" +
                "${abc}\n" +
                "}\n" +
                "}";
        String rslt = ScriptGenerator.convert(templateStr, params);
        System.out.println(rslt);
    }
}