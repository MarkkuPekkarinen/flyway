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
package org.flywaydb.core.internal.database.oracle;

import org.flywaydb.core.internal.database.Delimiter;
import org.flywaydb.core.internal.util.StringUtils;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test for OracleSqlStatementBuilder.
 */
public class OracleSqlStatementBuilderSmallTest {
    private OracleSqlStatementBuilder builder = new OracleSqlStatementBuilder(new Delimiter(";", false));

    @Test
    public void setDefineOff() {
        builder.addLine("set define off;");
        assertTrue(builder.canDiscard());
    }

    @Test(timeout = 2000)
    public void isUnsupportedSqlPlusStatementPerformance() {
        builder.addLine("SHOW ERRORS;");
        for (int i = 0; i < 1000000; i++) {
            assertFalse(builder.isUnsupportedSqlPlusStatement());
        }
    }

    @Test
    public void isUnsupportedSqlPlusStatementShowHistory() {
        builder.addLine("SHOW HISTORY;");
        assertTrue(builder.isUnsupportedSqlPlusStatement());
    }

    @Test
    public void loneSlash() {
        builder.addLine("/");
        assertTrue(builder.canDiscard());
    }

    @Test
    public void changeDelimiterRegEx() {
        assertNull(builder.changeDelimiterIfNecessary("BEGIN_DATE", null));
        assertEquals("/", builder.changeDelimiterIfNecessary("BEGIN DATE", null).getDelimiter());
        assertEquals("/", builder.changeDelimiterIfNecessary("BEGIN", null).getDelimiter());
    }

    @Test
    public void javaSource() {
        builder.addLine("CREATE OR REPLACE AND COMPILE JAVA SOURCE NAMED \"JavaTest\" AS");
        assertFalse(builder.isTerminated());
        builder.addLine("public class JavaTest {");
        assertFalse(builder.isTerminated());
        builder.addLine("};");
        assertFalse(builder.isTerminated());
        builder.addLine("/");
        assertTrue(builder.isTerminated());
    }

    @Test
    public void nvarchar() {
        builder.addLine("INSERT INTO nvarchar2_test VALUES ( N'qwerty' );");
        assertTrue(builder.isTerminated());
    }

    @Test
    public void notNvarchar() {
        builder.addLine("INSERT INTO nvarchar2_test VALUES ( ' N' );");
        assertTrue(builder.isTerminated());
    }

    @Test
    public void qQuote() {
        builder.addLine("select q'[Hello 'no quotes]' from dual;");
        assertTrue(builder.isTerminated());
    }

    @Test
    public void asNoSpace() {
        builder.addLine("select '1'as \"QUANTITY\" from dual;");
        assertTrue(builder.isTerminated());
    }

    @Test
    public void multilineCommentQuote() {
        String sqlScriptSource = "create or replace procedure Test_proc\n" +
                "is\n" +
                "begin\n" +
                "    EXECUTE IMMEDIATE 'SELECT 123 num, 321 num2 '||'/*comment with,comma'||'*/ from dual order by num, num2';\n" +
                "end Test_proc;\n" +
                "/\n";

        String[] lines = StringUtils.tokenizeToStringArray(sqlScriptSource, "\n");
        for (String line : lines) {
            builder.addLine(line);
        }

        assertTrue(builder.isTerminated());
    }

    @Test
    public void dateNoSpace() {
        String sqlScriptSource = "CREATE OR REPLACE PACKAGE BODY DEMO_P_FLYWAYBUG\n" +
                "IS\n" +
                "  FUNCTION F_MAGIC_DATE\n" +
                "    RETURN DATE\n" +
                "  IS BEGIN\n" +
                "    RETURN DATE'11/29/2017';\n" +
                "  END F_MAGIC_DATE;\n" +
                "\n" +
                "END DEMO_P_FLYWAYBUG;\n" +
                "/\n";

        String[] lines = StringUtils.tokenizeToStringArray(sqlScriptSource, "\n");
        for (String line : lines) {
            builder.addLine(line);
        }

        assertTrue(builder.isTerminated());
    }

    @Test
    public void quotedStringEndingWithN() {
        builder.addLine("insert into table (COLUMN) values 'VALUE_WITH_N';");
        assertTrue(builder.isTerminated());
    }

    @Test
    public void quotedWithFrom() {
        builder.addLine("insert into table (COLUMN) values 'FROM';");
        assertTrue(builder.isTerminated());
    }

    @Test
    public void quotedWithFromComplex() {
        builder.addLine("DELETE FROM TEST.TABLE1 where CFG_AREA_ID_1 like '%NAME%' AND SOME_ID='NITS'AND CFG_AREA_CD IN ('COND_TXT','FORM');");
        assertTrue(builder.isTerminated());
    }
}