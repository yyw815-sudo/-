package com.medical.repository;

import com.medical.entity.Admin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * 管理员数据访问层
 */
@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
    
    @Query(value = "SELECT * FROM admin WHERE BINARY admin_name = BINARY :adminName", nativeQuery = true)
    Optional<Admin> findByAdminName(@Param("adminName") String adminName);
    
    @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN 1 ELSE 0 END FROM admin WHERE BINARY admin_name = BINARY :adminName", nativeQuery = true)
    Integer existsByAdminName(@Param("adminName") String adminName);

    @Query(value = "SELECT * FROM admin WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "admin_id = CAST(:keyword AS signed) OR " +
           "BINARY admin_name LIKE BINARY CONCAT('%', :keyword, '%') OR BINARY realname LIKE BINARY CONCAT('%', :keyword, '%') OR BINARY phone LIKE BINARY CONCAT('%', :keyword, '%'))",
           countQuery = "SELECT COUNT(*) FROM admin WHERE " +
                        "(:keyword IS NULL OR :keyword = '' OR " +
                        "admin_id = CAST(:keyword AS signed) OR " +
                        "BINARY admin_name LIKE BINARY CONCAT('%', :keyword, '%') OR BINARY realname LIKE BINARY CONCAT('%', :keyword, '%') OR BINARY phone LIKE BINARY CONCAT('%', :keyword, '%'))",
           nativeQuery = true)
    Page<Admin> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
}