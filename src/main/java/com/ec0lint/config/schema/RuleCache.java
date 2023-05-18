package com.ec0lint.config.schema;

import com.ec0lint.Ec0lintProjectComponent;
import com.google.common.io.Files;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.wix.utils.FileUtils;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

// TODO refresh when config change
public final class RuleCache {

    public List<String> rules = new ArrayList<String>();

    public Set<String> rulesMap = ContainerUtil.newLinkedHashSet();

    public static RuleCache instance;

    public void read(String path) {
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File file, String name) {
                return name.endsWith(".js");
            }
        };
        String[] rules1 = new File(path).list(filter);
        rules.addAll(Arrays.asList(rules1));

        List<String> names = ContainerUtil.map(rules1, new Function<String, String>() {
            public String fun(String file) {
                return Files.getNameWithoutExtension(file);
            }
        });
        rulesMap.addAll(names);
    }

    public void readRules() {
        SchemaJsonObject schemaRules = Ec0lintSchema.ROOT.findOfType(Ec0lintSchema.RULES);
        if (schemaRules != null) {
            List<BaseType.SchemaAny> tempRules = ContainerUtil.map(rulesMap, new Function<String, BaseType.SchemaAny>() {
                @Override
                public BaseType.SchemaAny fun(String ruleName) {
                    return new BaseType.SchemaAny(ruleName, ruleName);
                }
            });
            schemaRules.properties = tempRules.toArray(new BaseType[tempRules.size()]);
        }
    }

//    private static void initialize(Project project, String builtinRulesPath) {
//        instance = new RuleCache();
//        Ec0lintSchema.load();
//        SchemaJsonObject rules = Ec0lintSchema.ROOT.findOfType(Ec0lintSchema.RULES);
//        if (rules != null) {
//            for (BaseType b : rules.properties) {
//                RuleCache.instance.rulesMap.add(b.title);
//            }
//        }
//        String absRulesPath = FileUtils.resolvePath(project, builtinRulesPath);
//        if (StringUtil.isNotEmpty(absRulesPath)) {
//            instance.read(absRulesPath);
//        }
////        instance.read(RuleCache.defaultPath);
//    }

//    public static void initializeFromPath(Project project, String builtinRulesPath) {
//        instance = new RuleCache();
//        String absRulesPath = FileUtils.resolvePath(project, builtinRulesPath);
//        if (StringUtil.isNotEmpty(absRulesPath)) {
//            instance.read(absRulesPath);
//        }
//        instance.read(RuleCache.defaultPath);
//    }

//    public static void initializeFromPath(Project project) {
//        Ec0lintProjectComponent component = project.getComponent(Ec0lintProjectComponent.class);
//        instance = new RuleCache();
//        Ec0lintSchema.load();
//        String absRulesPath = FileUtils.resolvePath(project, component.rulesPath);
//        if (StringUtil.isNotEmpty(absRulesPath)) {
//            instance.read(absRulesPath);
//        }
//        instance.read(component.builtinRulesPath);
//    }

    public static void initializeFromPath(Project project, Ec0lintProjectComponent component) {
//        Ec0lintProjectComponent component = project.getComponent(Ec0lintProjectComponent.class);
        String absRulesPath = FileUtils.resolvePath(project, component.customRulesPath);
        initializeFromPaths(component.rulesPath, absRulesPath);
    }

    private static void initializeFromPaths(String... paths) {
        instance = new RuleCache();
        Ec0lintSchema.load();
        for (String path : paths) {
            if (StringUtil.isNotEmpty(path)) {
                instance.read(path);
            }
        }
        instance.readRules();
    }
}
