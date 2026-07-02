package com.medical.repository;

import com.medical.entity.OcrReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OcrReviewRepository extends JpaRepository<OcrReview, Long> {

    @Query(value = "SELECT * FROM ocr_review WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "medication_name LIKE CONCAT('%', :keyword, '%')) AND " +
            "(:status IS NULL OR status = :status)",
            countQuery = "SELECT COUNT(*) FROM ocr_review WHERE " +
                    "(:keyword IS NULL OR :keyword = '' OR " +
                    "medication_name LIKE CONCAT('%', :keyword, '%')) AND " +
                    "(:status IS NULL OR status = :status)",
            nativeQuery = true)
    Page<OcrReview> findByKeywordAndStatus(@Param("keyword") String keyword, @Param("status") Integer status, Pageable pageable);

    @Query(value = "SELECT * FROM ocr_review WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "medication_name LIKE CONCAT('%', :keyword, '%'))",
            countQuery = "SELECT COUNT(*) FROM ocr_review WHERE " +
                    "(:keyword IS NULL OR :keyword = '' OR " +
                    "medication_name LIKE CONCAT('%', :keyword, '%'))",
            nativeQuery = true)
    Page<OcrReview> findByKeywordOnly(@Param("keyword") String keyword, Pageable pageable);

    List<OcrReview> findByUserId(Long userId);

    List<OcrReview> findByStatusOrderByCreateTimeDesc(Integer status);
}