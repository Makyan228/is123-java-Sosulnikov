package com.skladsystem.repository;

import com.skladsystem.model.AppUser;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

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
                    r.name as role_name,
                    u.is_active
                from app_user u
                join app_role r on r.id = u.role_id
                where coalesce(u.is_active, 0) = 1
                order by u.id
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            AppUser user = new AppUser();
            user.setId(rs.getLong("id"));
            user.setUsername(rs.getString("username"));
            user.setFullName(rs.getString("full_name"));
            user.setRoleName(rs.getString("role_name"));
            user.setActive(rs.getInt("is_active") == 1);
            return user;
        });
    }

    public AppUser findById(Long id) {
        String sql = """
                select
                    u.id,
                    u.username,
                    u.full_name,
                    r.name as role_name,
                    u.is_active
                from app_user u
                join app_role r on r.id = u.role_id
                where u.id = ?
                """;

        List<AppUser> users = jdbcTemplate.query(sql, (rs, rowNum) -> {
            AppUser user = new AppUser();
            user.setId(rs.getLong("id"));
            user.setUsername(rs.getString("username"));
            user.setFullName(rs.getString("full_name"));
            user.setRoleName(rs.getString("role_name"));
            user.setActive(rs.getInt("is_active") == 1);
            return user;
        }, id);

        return users.isEmpty() ? null : users.get(0);
    }

    public AppUser findFirstActive() {
        List<AppUser> users = findAllActive();
        return users.isEmpty() ? null : users.get(0);
    }
}