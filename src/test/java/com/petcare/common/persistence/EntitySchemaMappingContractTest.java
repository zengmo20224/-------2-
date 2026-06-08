package com.petcare.common.persistence;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EntitySchemaMappingContractTest {

    @Test
    void entityTableNamesCoverSchemaTablesExactly() throws IOException {
        Set<String> schemaTables = PersistenceContractSupport.schemaColumnsByTable().keySet();
        Set<String> entityTables = PersistenceContractSupport.entityTypes().stream()
                .map(PersistenceContractSupport::tableName)
                .collect(Collectors.toSet());

        assertEquals(schemaTables, entityTables);
    }

    @Test
    void entityFieldsMatchSchemaColumnsExactly() throws IOException {
        Map<String, Set<String>> schemaColumnsByTable = PersistenceContractSupport.schemaColumnsByTable();

        for (Class<?> entityType : PersistenceContractSupport.entityTypes()) {
            String tableName = PersistenceContractSupport.tableName(entityType);
            Set<String> schemaColumns = schemaColumnsByTable.get(tableName);
            Set<String> mappedColumns = PersistenceContractSupport.mappedColumns(entityType);

            assertEquals(schemaColumns, mappedColumns, entityType.getName() + " must match " + tableName);
        }
    }

    @Test
    void allEntitiesUseAssignIdTableId() {
        for (Class<?> entityType : PersistenceContractSupport.entityTypes()) {
            Field idField = PersistenceContractSupport.tableIdField(entityType);
            TableId tableId = idField.getAnnotation(TableId.class);

            assertEquals("id", tableId.value(), entityType.getName());
            assertEquals(IdType.ASSIGN_ID, tableId.type(), entityType.getName());
        }
    }

    @Test
    void deletedColumnsUseTableLogic() throws IOException {
        Map<String, Set<String>> schemaColumnsByTable = PersistenceContractSupport.schemaColumnsByTable();

        for (Class<?> entityType : PersistenceContractSupport.entityTypes()) {
            String tableName = PersistenceContractSupport.tableName(entityType);
            if (!schemaColumnsByTable.get(tableName).contains("deleted")) {
                continue;
            }

            Field deletedField = PersistenceContractSupport.fieldByColumn(entityType, "deleted");
            assertTrue(deletedField.isAnnotationPresent(TableLogic.class), entityType.getName());
        }
    }
}
