package com.ec0lint.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.intellij.execution.process.ProcessOutput;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Ec0lint result
 * Created by idok on 8/25/14.
 */
public class Result {
    public List<Warn> warns = new ArrayList<Warn>();
    public String errorOutput;

    private static final Pattern PATTERN = Pattern.compile("(\\d+):(\\d+)\\s+(\\w+)\\s+([\\w\\W]+)\\s+([\\w-]+)");
//    private static final Logger LOG = Logger.getInstance(Ec0lintBundle.LOG_ID);

    public static Result processResults(ProcessOutput output) {
        Result result = new Result();
        List<String> lines = output.getStdoutLines();
        String file = null;
        for (String line : lines) {
            if (file == null) {
                file = line;
            }
            Warn warn = parseLine(line);
            if (warn != null) {
                result.warns.add(warn);
            }
        }
        result.errorOutput = output.getStderr();
        return result;
    }

    private static Warn parseLine(String line) {
        Warn warn = null;
        Matcher matcher = PATTERN.matcher(line);
        if (matcher.find() && matcher.groupCount() == 5) {
            warn = new Warn();
            warn.line = Integer.parseInt(matcher.group(1));
            warn.column = Integer.parseInt(matcher.group(2));
            warn.level = matcher.group(3);
            warn.message = matcher.group(4);
            warn.rule = matcher.group(5);
        }
//        while (matcher.find()) {
//            LOG.debug("I found the text \"%s\" starting at index %d and ending at index %d.%n",
//                    matcher.group(),
//                    matcher.start(),
//                    matcher.end());
//        }
        return warn;
    }

    public static class Warn {
        public int line;
        public int column;
        public String message;
        public String level;
        public String rule;
    }

    private static List<FileResult> parseInternal(String json) {
        GsonBuilder builder = new GsonBuilder();
//        builder.registerTypeAdapterFactory(adapter);
        Gson g = builder.setPrettyPrinting().create();
        Type listType = new TypeToken<ArrayList<FileResult>>() {}.getType();
        return g.fromJson(json, listType);
    }

    public static Result createError(String error) {
        Result result = new Result();
        result.errorOutput = error;
        return result;
    }
}