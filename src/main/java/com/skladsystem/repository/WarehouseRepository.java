package com.skladsystem.repository;

import com.skladsystem.model.Warehouse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class WarehouseRepository {

    private final JdbcTemplate jdbcTemplate;

    public WarehouseRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Warehouse> findAllActive() {
        String sql = """
                select id, name, address, is_active
                from warehouse
                where coalesce(is_active, 0) = 1
                order by name
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Warehouse warehouse = new Warehouse();
            warehouse.setId(rs.getLong("id"));
            warehouse.setName(rs.getString("name"));
            warehouse.setAddress(rs.getString("address"));

            int activeValue = rs.getInt("is_active");
            warehouse.setActive(!rs.wasNull() && activeValue == 1);

            return warehouse;
        });
    }
}