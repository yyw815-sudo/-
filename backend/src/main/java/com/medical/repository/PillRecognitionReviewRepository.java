package com.medical.repository;

import com.medical.entity.PillRecognitionReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PillRecognitionReviewRepository extends JpaRepository<PillRecognitionReview, Long> {

    @Query(value = "SELECT * FROM pill_recognition_review WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "pill_name LIKE CONCAT('%', :keyword, '%')) AND " +
            "(:status IS NULL OR status = :status)",
            countQuery = "SELECT COUNT(*) FROM pill_recognition_review WHERE " +
                    "(:keyword IS NULL OR :keyword = '' OR " +
                    "pill_name LIKE CONCAT('%', :keyword, '%')) AND " +
                    "(:status IS NULL OR status = :status)",
            nativeQuery = true)
    Page<PillRecognitionReview> findByKeywordAndStatus(@Param("keyword") String keyword, @Param("status") Integer status, Pageable pageable);

    @Query(value = "SELECT * FROM pill_recognition_review WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "pill_name LIKE CONCAT('%', :keyword, '%'))",
            countQuery = "SELECT COUNT(*) FROM pill_recognition_review WHERE " +
                    "(:keyword IS NULL OR :keyword = '' OR " +
                    "pill_name LIKE CONCAT('%', :keyword, '%'))",
            nativeQuery = true)
    Page<PillRecognitionReview> findByKeywordOnly(@Param("keyword") String keyword, Pageable pageable);

    List<PillRecognitionReview> findByUserId(Long userId);

    List<PillRecognitionReview> findByStatusOrderByCreateTimeDesc(Integer status);
}