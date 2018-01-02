/*
 * Copyright 2010-2018 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 *
 * Must
 * be
 * exactly
 * 13 lines
 * to match
 * community
 * edition
 * license
 * length.
 */
package org.flywaydb.core.internal.database.db2;

import org.flywaydb.core.DbCategory;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.flywaydb.core.migration.MigrationTestCase;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

/**
 * Test to demonstrate the migration functionality using DB2.
 */
@Category(DbCategory.DB2.class)
@RunWith(Parameterized.class)
public class DB2MigrationMediumTest extends MigrationTestCase {
    static final String JDBC_URL_DB2_111 = "jdbc:db2://localhost:62011/testdb";
    static final String JDBC_URL_DB2_105 = "jdbc:db2://localhost:62010/flyway";
    static final String JDBC_USER = "db2inst1";
    static final String JDBC_PASSWORD = "flywaypwd";

    private final String jdbcUrl;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {JDBC_URL_DB2_111},
                {JDBC_URL_DB2_105}
        });
    }

    public DB2MigrationMediumTest(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    @Override
    protected DataSource createDataSource() {
        return new DriverDataSource(Thread.currentThread().getContextClassLoader(), null,
                jdbcUrl, JDBC_USER, JDBC_PASSWORD, null);
    }

    @Override
    protected String getQuoteLocation() {
        return "migration/quote";
    }

    @Test
    @Ignore("Excluding by default as for some reason this test is flaky in Maven even though it is stable in IntelliJ")
    public void schemaWithDash() throws FlywayException {
        flyway.setSchemas("my-schema");
        flyway.setLocations(getBasedir());
        flyway.clean();
        flyway.migrate();
    }

    @Test
    public void sequence() throws Exception {
        flyway.setLocations("migration/database/db2/sql/sequence");
        flyway.migrate();

        assertEquals("1", flyway.info().current().getVersion().toString());
        assertEquals("Sequence", flyway.info().current().getDescription());

        assertEquals(666, jdbcTemplate.queryForInt("VALUES NEXTVAL FOR BEAST_SEQ"));

        flyway.clean();
        flyway.migrate();
    }

    @Test
    public void bitdata() {
        flyway.setLocations("migration/database/db2/sql/bitdata");
        flyway.migrate();

        assertEquals("1", flyway.info().current().getVersion().toString());
    }

    @Test
    public void truncate() {
        flyway.setLocations("migration/database/db2/sql/truncate");
        flyway.migrate();

        assertEquals("2", flyway.info().current().getVersion().toString());
    }

    @Test
    public void delimiter() {
        flyway.setLocations("migration/database/db2/sql/delimiter");
        flyway.migrate();

        assertEquals("1", flyway.info().current().getVersion().toString());
    }

    @Test
    public void mqt() throws Exception {
        flyway.setLocations("migration/database/db2/sql/mqt");
        flyway.migrate();

        assertEquals("1", flyway.info().current().getVersion().toString());
        assertEquals("Mqt", flyway.info().current().getDescription());

        assertEquals(2, jdbcTemplate.queryForInt("SELECT COUNT(*) FROM empl_mqt"));

        flyway.clean();
        flyway.migrate();
    }

    @Test
    public void alias() throws Exception {
        flyway.setLocations("migration/database/db2/sql/alias");
        flyway.migrate();

        assertEquals("1", flyway.info().current().getVersion().toString());
        assertEquals("Alias", flyway.info().current().getDescription());

        assertEquals(2, jdbcTemplate.queryForInt("SELECT COUNT(*) FROM POOR_SLAVE"));

        flyway.clean();
        flyway.migrate();
    }

    @Test
    public void trigger() {
        flyway.setLocations("migration/database/db2/sql/trigger");
        flyway.migrate();

        flyway.clean();
        flyway.migrate();
    }

    @Test
    public void procedure() {
        flyway.setLocations("migration/database/db2/sql/procedure");
        flyway.migrate();

        flyway.clean();
        flyway.migrate();
    }

    @Test
    public void type() {
        flyway.setLocations("migration/database/db2/sql/type");
        flyway.migrate();

        flyway.clean();
        flyway.migrate();
    }

    @Test
    public void function() {
        flyway.setLocations("migration/database/db2/sql/function");
        flyway.migrate();

        flyway.clean();
        flyway.migrate();
    }

    @Test
    public void expressionBasedIndex() {
        flyway.setLocations("migration/database/db2/sql/index");
        flyway.migrate();

        flyway.clean();
        flyway.migrate();
    }

    @Test
    public void versioned() {
        flyway.setLocations("migration/database/db2/sql/versioned");
        flyway.migrate();

        flyway.clean();
        flyway.migrate();
    }

    // Issue #802: Clean on DB2 does not clean triggers.
    @Test
    public void noTriggersShouldBeLeftAfterClean() throws Exception {
        flyway.setLocations("migration/database/db2/sql/trigger");
        flyway.migrate();
        flyway.clean();

        // default schema is username in upper case, so we need to use that.
        assertEquals(0, jdbcTemplate.queryForInt("SELECT COUNT(*) FROM SYSCAT.TRIGGERS WHERE TRIGSCHEMA = ?", JDBC_USER.toUpperCase()));
    }

    // Issue #1722: (DB2) clean fails due to sqlcode=476 following a DROP PROCEDURE statement
    @Test
    public void dropProceduresWithSameName() throws Exception {
        flyway.setLocations("migration/database/db2/sql/procedure");
        flyway.migrate();
        flyway.clean();

        //THE ONLY PROCEDURES DEFINED USES THE SAME NAME "SP_EQIP_HOURS_AGGRGT_DAY_VIS", SO IT SHOULD NOT EXIST ANYMORE ON SYSTEM CATALOG
        assertEquals(0, jdbcTemplate.queryForInt("SELECT COUNT(*) FROM SYSCAT.PROCEDURES WHERE PROCNAME = ?", "SP_EQIP_HOURS_AGGRGT_DAY_VIS"));
    }
}