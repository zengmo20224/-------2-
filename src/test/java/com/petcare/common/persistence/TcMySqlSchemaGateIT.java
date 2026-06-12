package com.petcare.common.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcare.user.entity.User;
import com.petcare.user.mapper.UserMapper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testcontainers MySQL 8 schema gate — verifies the DDL and mapper layer
 * against a real MySQL 8 container spun up by Testcontainers.
 * <p>
 * Run with: {@code mvn clean test -Ptc-mysql}
 */
@Tag("tc-mysql")
@Transactional
class TcMySqlSchemaGateIT extends AbstractTcMySqlIT {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private UserMapper userMapper;

    @Test
    void schemaLoadsIntoMysql8() {
        String version = jdbcTemplate.queryForObject("SELECT VERSION()", String.class);
        assertNotNull(version);
        assertTrue(version.startsWith("8."),
                "Expected MySQL 8.x but got " + version);
    }

    @Test
    void everyMapperCanCountAgainstRealMysql() {
        Arrays.stream(applicationContext.getBeanNamesForType(BaseMapper.class))
                .map(applicationContext::getBean)
                .map(bean -> (BaseMapper<?>) bean)
                .forEach(mapper -> assertDoesNotThrow(() -> mapper.selectCount(null)));
    }

    @Test
    void userMapperInsertAndSelectOnMysql() {
        User user = new User();
        user.setNickname("tc-gate-user");
        user.setStatus("ACTIVE");
        userMapper.insert(user);

        User loaded = userMapper.selectById(user.getId());
        assertNotNull(loaded);
        assertEquals("tc-gate-user", loaded.getNickname());
        assertEquals("ACTIVE", loaded.getStatus());
    }
}
