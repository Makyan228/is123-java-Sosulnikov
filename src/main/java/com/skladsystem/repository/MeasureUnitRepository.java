package com.skladsystem.repository;

import com.skladsystem.model.MeasureUnit;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MeasureUnitRepository {

    private final JdbcTemplate jdbcTemplate;

    public MeasureUnitRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<MeasureUnit> findAll() {
        String sql = """
                select id, name, short_name
                from measure_unit
                order by name
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            MeasureUnit unit = new MeasureUnit();
            unit.setId(rs.getLong("id"));
            unit.setName(rs.getString("name"));
            unit.setShortName(rs.getString("short_name"));
            return unit;
        });
    }
}