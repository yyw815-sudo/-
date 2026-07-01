package com.medical.repository;

import com.medical.entity.ApiConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApiConfigRepository extends JpaRepository<ApiConfig, Long> {

    @Query(value = "SELECT * FROM api_config WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "api_name LIKE CONCAT('%', :keyword, '%') OR api_type LIKE CONCAT('%', :keyword, '%'))",
            countQuery = "SELECT COUNT(*) FROM api_config WHERE " +
                    "(:keyword IS NULL OR :keyword = '' OR " +
                    "api_name LIKE CONCAT('%', :keyword, '%') OR api_type LIKE CONCAT('%', :keyword, '%'))",
            nativeQuery = true)
    Page<ApiConfig> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    List<ApiConfig> findByApiType(String apiType);

    List<ApiConfig> findByStatus(Integer status);
}