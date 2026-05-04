package com.skladsystem.repository;

import com.skladsystem.model.AppUser;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class AppUserRepository {

    private final JdbcTemplate jdbcTemplate;

    public AppUserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<AppUser> findAllActive() {
        String sql = """
                select
                    u.id,
                    u.username,
                    u.full_name,
                    u.password_hash as password,
                    r.name as role_name,
                    u.is_active
                from app_user u
                join app_role r on r.id = u.role_id
                where coalesce(u.is_active, 0) = 1
                order by u.id
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> mapUser(rs));
    }

    public AppUser findById(Long id) {
        String sql = """
                select
                    u.id,
                    u.username,
                    u.full_name,
                    u.password_hash as password,
                    r.name as role_name,
                    u.is_active
                from app_user u
                join app_role r on r.id = u.role_id
                where u.id = ?
                """;

        List<AppUser> users = jdbcTemplate.query(sql, (rs, rowNum) -> mapUser(rs), id);
        return users.isEmpty() ? null : users.get(0);
    }

    public AppUser findByRoleUsernameAndPassword(String roleName, String username, String password) {
        String sql = """
                select
                    u.id,
                    u.username,
                    u.full_name,
                    u.password_hash as password,
                    r.name as role_name,
                    u.is_active
                from app_user u
                join app_role r on r.id = u.role_id
                where upper(r.name) = upper(?)
                  and lower(u.username) = lower(?)
                  and u.password_hash = ?
                  and coalesce(u.is_active, 0) = 1
                """;

        List<AppUser> users = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapUser(rs),
                roleName,
                username,
                password
        );

        return users.isEmpty() ? null : users.get(0);
    }

    private AppUser mapUser(ResultSet rs) throws SQLException {
        AppUser user = new AppUser();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setFullName(rs.getString("full_name"));
        user.setPassword(rs.getString("password"));
        user.setRoleName(rs.getString("role_name"));
        user.setActive(rs.getInt("is_active") == 1);
        return user;
    }
}