package com.skladsystem.repository;

import com.skladsystem.model.StorageLocation;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class StorageLocationRepository {

    private final JdbcTemplate jdbcTemplate;

    public StorageLocationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<StorageLocation> findActiveByWarehouseId(Long warehouseId) {
        String sql = """
                select id, warehouse_id, code, name, capacity, is_active
                from storage_location
                where coalesce(is_active, 0) = 1
                  and warehouse_id = ?
                order by code, name
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            StorageLocation location = new StorageLocation();
            location.setId(rs.getLong("id"));
            location.setWarehouseId(rs.getLong("warehouse_id"));
            location.setCode(rs.getString("code"));
            location.setName(rs.getString("name"));
            location.setCapacity(rs.getBigDecimal("capacity"));

            int activeValue = rs.getInt("is_active");
            location.setActive(!rs.wasNull() && activeValue == 1);

            return location;
        }, warehouseId);
    }

    public StorageLocation findFirstActiveByWarehouseId(Long warehouseId) {
        List<StorageLocation> locations = findActiveByWarehouseId(warehouseId);
        return locations.isEmpty() ? null : locations.get(0);
    }
}