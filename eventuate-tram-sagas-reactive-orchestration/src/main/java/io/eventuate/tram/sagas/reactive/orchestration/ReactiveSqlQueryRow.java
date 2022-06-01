package io.eventuate.tram.sagas.reactive.orchestration;

import io.eventuate.tram.sagas.orchestration.SqlQueryRow;
import io.r2dbc.spi.Row;

public class ReactiveSqlQueryRow implements SqlQueryRow  {
    private Row row;

    public ReactiveSqlQueryRow(Row row) {
        this.row = row;
    }

    @Override
    public String getString(String name) {
        return row.get(name, String.class);
    }

    @Override
    public boolean getBoolean(String name) {
        Integer o = row.get(name, Integer.class);
        return o != null && o > 0;
    }


}
