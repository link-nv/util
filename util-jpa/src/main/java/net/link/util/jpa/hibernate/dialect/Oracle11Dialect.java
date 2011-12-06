package net.link.util.jpa.hibernate.dialect;

import java.sql.Types;
import org.hibernate.dialect.OracleDialect;


/**
 * Custom Oracle Dialect to resolve the issue with Double mapping to Oracle's Double precision
 *
 * @author wvdhaute
 */
public class Oracle11Dialect extends OracleDialect {

    public Oracle11Dialect() {

        registerColumnType( Types.DOUBLE, "binary_double" );
    }
}
