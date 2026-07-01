package com.medical.repository;

import com.medical.entity.SystemAnnouncement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SystemAnnouncementRepository extends JpaRepository<SystemAnnouncement, Long> {

    @Query(value = "SELECT * FROM system_announcement WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "title LIKE CONCAT('%', :keyword, '%') OR type LIKE CONCAT('%', :keyword, '%')) AND " +
            "(:status IS NULL OR status = :status)",
            countQuery = "SELECT COUNT(*) FROM system_announcement WHERE " +
                    "(:keyword IS NULL OR :keyword = '' OR " +
                    "title LIKE CONCAT('%', :keyword, '%') OR type LIKE CONCAT('%', :keyword, '%')) AND " +
                    "(:status IS NULL OR status = :status)",
            nativeQuery = true)
    Page<SystemAnnouncement> findByKeywordAndStatus(@Param("keyword") String keyword, @Param("status") Integer status, Pageable pageable);

    @Query(value = "SELECT * FROM system_announcement WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "title LIKE CONCAT('%', :keyword, '%') OR type LIKE CONCAT('%', :keyword, '%'))",
            countQuery = "SELECT COUNT(*) FROM system_announcement WHERE " +
                    "(:keyword IS NULL OR :keyword = '' OR " +
                    "title LIKE CONCAT('%', :keyword, '%') OR type LIKE CONCAT('%', :keyword, '%'))",
            nativeQuery = true)
    Page<SystemAnnouncement> findByKeywordOnly(@Param("keyword") String keyword, Pageable pageable);

    List<SystemAnnouncement> findByStatusOrderByPublishTimeDesc(Integer status);
}