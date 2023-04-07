package com.ec0lint.config.schema;

public class BaseType {
    public String title;
    public Ec0LintSchema.PropertyType type;
    public String description;

    public BaseType() {
    }

    public static final String ANY_NAME = "*";

    public BaseType(String title, Ec0LintSchema.PropertyType type, String description) {
        this.title = title;
        this.type = type;
        this.description = description;
    }

    public boolean isValidValue(String value) {
        return true;
    }

    public static boolean isBoolean(String valueStr) {
        return Boolean.TRUE.toString().equals(valueStr) || Boolean.FALSE.toString().equals(valueStr);
    }

    public static class SchemaBoolean extends BaseType {
        public SchemaBoolean() {
            type = Ec0LintSchema.PropertyType.BOOLEAN;
        }

        @Override
        public boolean isValidValue(String value) {
            return isBoolean(value);
        }
    }

    public static class SchemaAny extends BaseType {
        public SchemaAny(String title, String description) {
            super(title, Ec0LintSchema.PropertyType.ANY, description);
        }

        public SchemaAny() {
            type = Ec0LintSchema.PropertyType.ANY;
        }
    }

    public static class SchemaString extends BaseType {
        public SchemaString() {
            type = Ec0LintSchema.PropertyType.STRING;
        }
    }
}