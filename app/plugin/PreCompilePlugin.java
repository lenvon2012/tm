package plugin;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.Play;
import play.PlayPlugin;
import play.classloading.ApplicationClasses;
import play.classloading.ApplicationCompiler;
import play.templates.BaseTemplate;
import play.templates.GroovyTemplate;
import play.templates.Template;
import play.vfs.VirtualFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lyl
 * @date 2018/02/05
 */
public class PreCompilePlugin extends PlayPlugin {
    private static final String PRE_COMPILE_DATE = "PreCompileDate";
    private static final Logger logger = LoggerFactory.getLogger(PreCompilePlugin.class);

    private static Map<String, Long> templateCompileHashMap = null;

    @Override
    public boolean compileSources() {
        //只有在执行预编译命令时运行
        if (System.getProperty("precompile") == null) {
            return false;
        }
        logger.info("开始预编译");
        try {
            HashMap<String, ApplicationClasses.ApplicationClass> compiledClassHashMap = new HashMap<String, ApplicationClasses.ApplicationClass>();
            if (Play.getFile("precompiled/java").exists()) {
                scanPrecompiled(compiledClassHashMap, "", Play.getVirtualFile("precompiled/java"));
            }
            logger.info("取之前预编译时间表");

            //取之前预编译时间表
            HashMap<String, Long> compileDateHashMap = new HashMap<String, Long>();
            File preFile = Play.getFile("precompiled/java/PreCompileDate");
            if (preFile.exists()) {
                logger.info("获取map");
                getPreCompileDateMap(compileDateHashMap, preFile);
            }else {
                logger.info("java编译时间表不存在");
            }
            logger.info("取现在所有的java类");

            //取现在所有的java类
            List<ApplicationClasses.ApplicationClass> all = new ArrayList<ApplicationClasses.ApplicationClass>();
            for (VirtualFile virtualFile : Play.javaPath) {
                all.addAll(getAllClasses(virtualFile));
            }
            logger.info("开始处理");
            ApplicationCompiler applicationCompiler = new ApplicationCompiler(Play.classes);
            for (ApplicationClasses.ApplicationClass clazz : all) {
                VirtualFile file = clazz.javaFile;
                long modifiedDate = file.lastModified();
                Long compileDate = compileDateHashMap.get(clazz.name);
                //logger.info("{}   {}", modifiedDate, compileDate);
                //判断预编译表中是否有记录时间
                if (compileDate != null && compileDate.equals(modifiedDate)) {
                    //如果编译时间一样，尝试取之前的编译结果
                    logger.info("存在编译时间一样的结果");
                    ApplicationClasses.ApplicationClass compiledClass = compiledClassHashMap.get(clazz.name);
                    if (compiledClass != null) {
                        compiledClass.compiled(compiledClass.javaSource.getBytes());
                        //这里随便加个class，预编译反正不处理
                        compiledClass.javaClass = PreCompilePlugin.class;
                        Play.classes.add(compiledClass);
                    }
                    continue;
                }
                applicationCompiler.compile(new String[]{clazz.name});
                compileDateHashMap.put(clazz.name, modifiedDate);
            }
            logger.info("编译结束");
            ArrayList<String> compileDateArrayList = new ArrayList<String>(compileDateHashMap.size());
            for (String name : compileDateHashMap.keySet()) {
                compileDateArrayList.add(name + ":" + compileDateHashMap.get(name));
            }

            File f = Play.getFile("precompiled/java/PreCompileDate");
            f.getParentFile().mkdirs();
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(StringUtils.join(compileDateArrayList, "\r\n").getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("预编译结束");
            return false;
        }
        logger.info("预编译结束");
        return true;
    }

    @Override
    public Template loadTemplate(VirtualFile file) {
        //只有在执行预编译命令时运行
        logger.info("开始预编译模板");
        if (System.getProperty("precompile") == null) {
            return null;
        }
        //template编译是用循环一个个编译的，所以这边只能加判断来处理
        if ("routes".equals(file.getName())) {
            //避免多次重复保存，设置保存一次后就清空
            if (templateCompileHashMap == null) {
                return null;
            }
            logger.info("开始预编译模板时间保存");
            ArrayList<String> templateCompileArrayList = new ArrayList<String>(templateCompileHashMap.size());
            for (String name : templateCompileHashMap.keySet()) {
                templateCompileArrayList.add(name + ":::" + templateCompileHashMap.get(name));
            }

            File f = Play.getFile("precompiled/templates/PreCompileDate");
            f.getParentFile().mkdirs();
            FileOutputStream fos;
            try {
                fos = new FileOutputStream(f);
                fos.write(StringUtils.join(templateCompileArrayList, "\r\n").getBytes());
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            templateCompileHashMap = null;
            return null;
        }
        if (templateCompileHashMap == null) {
            logger.info("获取模板map");
            File preFile = Play.getFile("precompiled/templates/PreCompileDate");
            templateCompileHashMap = new HashMap<String, Long>();
            if (preFile.exists()) {
                getTemplateCompileDateMap(preFile);
            }else {
                logger.info("模板编译时间表不存在");
            }
        }
        long modifiedDate = file.lastModified();
        Long compileDate = templateCompileHashMap.get(file.relativePath());
        if (compileDate != null && compileDate.equals(modifiedDate)) {
            logger.info("已存在模板文件");
            BaseTemplate template = new GroovyTemplate(file.relativePath().replaceAll("\\{(.*)\\}", "from_$1").replace(":", "_").replace("..", "parent"), file.contentAsString());
            try {
                template.loadPrecompiled();
                return template;
            } catch (Exception e) {
                play.Logger.warn("Precompiled template %s not found, trying to load it dynamically...", file.relativePath());
            }
        }
        //这里不需要处理，直接返回null就行
        templateCompileHashMap.put(file.relativePath(), modifiedDate);
        return null;
    }

    private void getPreCompileDateMap(HashMap<String, Long> compileDateHashMap, File preFile) {
        String line;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(preFile));
            while ((line = reader.readLine()) != null) {
                if (line.trim().length() == 0) {
                    continue;
                }
                String[] lineParts = line.split(":");
                compileDateHashMap.put(lineParts[0], Long.valueOf(lineParts[1]));
            }
        } catch (Exception e) {
            logger.error("读取java预编译时间文件出错");
            throw new RuntimeException("java预编译出错");
        }
    }

    private void getTemplateCompileDateMap(File preFile) {
        String line;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(preFile));
            while ((line = reader.readLine()) != null) {
                if (line.trim().length() == 0) {
                    continue;
                }
                String[] lineParts = line.split(":::");
                templateCompileHashMap.put(lineParts[0], Long.valueOf(lineParts[1]));
            }
        } catch (Exception e) {
            logger.error("读取模板预编译时间文件出错");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private List<ApplicationClasses.ApplicationClass> getAllClasses(VirtualFile path) {
        return getAllClasses(path, "");
    }

    private List<ApplicationClasses.ApplicationClass> getAllClasses(VirtualFile path, String basePackage) {
        if (basePackage.length() > 0 && !basePackage.endsWith(".")) {
            basePackage += ".";
        }
        List<ApplicationClasses.ApplicationClass> res = new ArrayList<ApplicationClasses.ApplicationClass>();
        for (VirtualFile virtualFile : path.list()) {
            scan(res, basePackage, virtualFile);
        }
        return res;
    }

    void scan(List<ApplicationClasses.ApplicationClass> classes, String packageName, VirtualFile current) {
        if (!current.isDirectory()) {
            if (current.getName().endsWith(".java") && !current.getName().startsWith(".")) {
                String classname = packageName + current.getName().substring(0, current.getName().length() - 5);
                classes.add(Play.classes.getApplicationClass(classname));
            }
        } else {
            for (VirtualFile virtualFile : current.list()) {
                scan(classes, packageName + current.getName() + ".", virtualFile);
            }
        }
    }

    private void scanPrecompiled(HashMap<String, ApplicationClasses.ApplicationClass> classes, String packageName, VirtualFile current) {
        if (!current.isDirectory()) {
            if (current.getName().endsWith(".class") && !current.getName().startsWith(".")) {
                String classname = packageName.substring(5) + current.getName().substring(0, current.getName().length() - 6);
                classes.put(classname, new ApplicationClasses.ApplicationClass(classname));
            }
        } else {
            for (VirtualFile virtualFile : current.list()) {
                scanPrecompiled(classes, packageName + current.getName() + ".", virtualFile);
            }
        }
    }
}
