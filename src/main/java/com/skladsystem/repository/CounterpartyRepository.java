package com.skladsystem.repository;

import com.skladsystem.model.Counterparty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CounterpartyRepository {

    private static final String SUPPLIER_TYPE = "SUPPLIER";

    private final JdbcTemplate jdbcTemplate;

    public CounterpartyRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Counterparty> counterpartyRowMapper = (rs, rowNum) -> {
        Counterparty counterparty = new Counterparty();

        counterparty.setId(rs.getLong("id"));
        counterparty.setName(rs.getString("name"));
        counterparty.setPartyType(rs.getString("party_type"));
        counterparty.setPhone(rs.getString("phone"));
        counterparty.setEmail(rs.getString("email"));
        counterparty.setAddress(rs.getString("address"));

        int activeValue = rs.getInt("is_active");
        counterparty.setActive(!rs.wasNull() && activeValue == 1);

        return counterparty;
    };

    public List<Counterparty> findAllSuppliers() {
        String sql = """
                select id, name, party_type, phone, email, address, is_active
                from counterparty
                where coalesce(is_active, 0) = 1
                  and party_type = ?
                order by name
                """;

        return jdbcTemplate.query(sql, counterpartyRowMapper, SUPPLIER_TYPE);
    }

    public List<Counterparty> searchSuppliers(String search) {
        if (search == null || search.isBlank()) {
            return findAllSuppliers();
        }

        String sql = """
                select id, name, party_type, phone, email, address, is_active
                from counterparty
                where coalesce(is_active, 0) = 1
                  and party_type = ?
                  and (
                      name containing ?
                      or coalesce(phone, '') containing ?
                      or coalesce(email, '') containing ?
                  )
                order by name
                """;

        String term = search.trim();
        return jdbcTemplate.query(sql, counterpartyRowMapper, SUPPLIER_TYPE, term, term, term);
    }

    public Counterparty findSupplierById(Long id) {
        String sql = """
                select id, name, party_type, phone, email, address, is_active
                from counterparty
                where coalesce(is_active, 0) = 1
                  and party_type = ?
                  and id = ?
                """;

        List<Counterparty> suppliers = jdbcTemplate.query(sql, counterpartyRowMapper, SUPPLIER_TYPE, id);
        return suppliers.isEmpty() ? null : suppliers.get(0);
    }

    public void saveSupplier(Counterparty counterparty) {
        String sql = """
                insert into counterparty (
                    name,
                    party_type,
                    phone,
                    email,
                    address,
                    is_active
                )
                values (?, ?, ?, ?, ?, ?)
                """;

        boolean active = counterparty.getActive() == null || counterparty.getActive();

        jdbcTemplate.update(
                sql,
                nullIfBlank(counterparty.getName()),
                SUPPLIER_TYPE,
                nullIfBlank(counterparty.getPhone()),
                nullIfBlank(counterparty.getEmail()),
                nullIfBlank(counterparty.getAddress()),
                active ? 1 : 0
        );
    }

    public void updateSupplier(Counterparty counterparty) {
        String sql = """
                update counterparty
                set
                    name = ?,
                    phone = ?,
                    email = ?,
                    address = ?,
                    is_active = ?
                where id = ?
                  and party_type = ?
                """;

        boolean active = counterparty.getActive() == null || counterparty.getActive();

        jdbcTemplate.update(
                sql,
                nullIfBlank(counterparty.getName()),
                nullIfBlank(counterparty.getPhone()),
                nullIfBlank(counterparty.getEmail()),
                nullIfBlank(counterparty.getAddress()),
                active ? 1 : 0,
                counterparty.getId(),
                SUPPLIER_TYPE
        );
    }

    public void softDeleteSupplier(Long id) {
        String sql = """
                update counterparty
                set is_active = 0
                where id = ?
                  and party_type = ?
                """;

        jdbcTemplate.update(sql, id, SUPPLIER_TYPE);
    }

    private String nullIfBlank(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}