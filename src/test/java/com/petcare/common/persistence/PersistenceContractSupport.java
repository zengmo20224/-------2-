package com.petcare.common.persistence;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class PersistenceContractSupport {

    private static final Pattern CREATE_TABLE_PATTERN = Pattern.compile(
            "CREATE TABLE `([^`]+)` \\((.*?)\\) ENGINE=",
            Pattern.DOTALL);

    private static final Pattern COLUMN_PATTERN = Pattern.compile("^\\s*`([^`]+)`\\s+");

    private PersistenceContractSupport() {
    }

    static Map<String, Set<String>> schemaColumnsByTable() throws IOException {
        String schema = Files.readString(Path.of("schema.sql"));
        Matcher tableMatcher = CREATE_TABLE_PATTERN.matcher(schema);
        Map<String, Set<String>> tables = new LinkedHashMap<>();

        while (tableMatcher.find()) {
            String tableName = tableMatcher.group(1);
            String tableBody = tableMatcher.group(2);
            Set<String> columns = new LinkedHashSet<>();

            for (String line : tableBody.split("\\R")) {
                Matcher columnMatcher = COLUMN_PATTERN.matcher(line);
                if (columnMatcher.find()) {
                    columns.add(columnMatcher.group(1));
                }
            }

            tables.put(tableName, columns);
        }

        return tables;
    }

    static Set<Class<?>> entityTypes() {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(TableName.class));

        Set<Class<?>> entityTypes = new LinkedHashSet<>();
        scanner.findCandidateComponents("com.petcare").forEach(component -> {
            try {
                entityTypes.add(Class.forName(component.getBeanClassName()));
            } catch (ClassNotFoundException ex) {
                throw new IllegalStateException("Entity class not found: " + component.getBeanClassName(), ex);
            }
        });
        return entityTypes;
    }

    static String tableName(Class<?> entityType) {
        TableName tableName = entityType.getAnnotation(TableName.class);
        if (tableName == null) {
            throw new IllegalArgumentException(entityType.getName() + " has no @TableName");
        }
        return tableName.value().replace("`", "");
    }

    static Set<String> mappedColumns(Class<?> entityType) {
        Set<String> columns = new LinkedHashSet<>();
        for (Field field : allFields(entityType)) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            TableField tableField = field.getAnnotation(TableField.class);
            if (tableField != null && !tableField.exist()) {
                continue;
            }

            TableId tableId = field.getAnnotation(TableId.class);
            if (tableId != null && !tableId.value().isBlank()) {
                columns.add(tableId.value());
            } else if (tableField != null && !tableField.value().isBlank()) {
                columns.add(tableField.value());
            } else {
                columns.add(camelToSnake(field.getName()));
            }
        }
        return columns;
    }

    static Field tableIdField(Class<?> entityType) {
        return allFields(entityType).stream()
                .filter(field -> field.isAnnotationPresent(TableId.class))
                .findFirst()
                .orElseThrow(() -> new AssertionError(entityType.getName() + " has no @TableId field"));
    }

    static Field fieldByColumn(Class<?> entityType, String columnName) {
        return allFields(entityType).stream()
                .filter(field -> columnName.equals(columnName(field)))
                .findFirst()
                .orElseThrow(() -> new AssertionError(entityType.getName() + " has no mapped field for " + columnName));
    }

    private static List<Field> allFields(Class<?> entityType) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = entityType;
        while (current != null && current != Object.class) {
            fields.addAll(List.of(current.getDeclaredFields()));
            current = current.getSuperclass();
        }
        return fields;
    }

    private static String columnName(Field field) {
        TableId tableId = field.getAnnotation(TableId.class);
        if (tableId != null && !tableId.value().isBlank()) {
            return tableId.value();
        }

        TableField tableField = field.getAnnotation(TableField.class);
        if (tableField != null && !tableField.value().isBlank()) {
            return tableField.value();
        }

        return camelToSnake(field.getName());
    }

    private static String camelToSnake(String value) {
        return value.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase();
    }
}
