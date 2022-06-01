package io.eventuate.tram.sagas.orchestration;

import java.sql.ResultSet;
import java.sql.SQLException;

class JdbcSqlQueryRow implements SqlQueryRow {
    private final ResultSet rs;

    public JdbcSqlQueryRow(ResultSet rs) {
        this.rs = rs;
    }

    @Override
    public String getString(String name) {
        try {
            return rs.getString(name);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean getBoolean(String name) {
        try {
            return rs.getBoolean(name);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
