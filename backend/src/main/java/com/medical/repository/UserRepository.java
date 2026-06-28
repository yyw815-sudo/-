package com.medical.repository;

import com.medical.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query(value = "SELECT * FROM user WHERE BINARY user_name = BINARY :userName", nativeQuery = true)
    Optional<User> findByUserName(@Param("userName") String userName);

    @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN 1 ELSE 0 END FROM user WHERE BINARY user_name = BINARY :userName", nativeQuery = true)
    Integer existsByUserName(@Param("userName") String userName);

    Optional<User> findByPhone(String phone);

    @Query(value = "SELECT * FROM user WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "user_id = CAST(:keyword AS signed) OR " +
           "BINARY user_name LIKE BINARY CONCAT('%', :keyword, '%') OR BINARY realname LIKE BINARY CONCAT('%', :keyword, '%') OR BINARY phone LIKE BINARY CONCAT('%', :keyword, '%'))",
           countQuery = "SELECT COUNT(*) FROM user WHERE " +
                        "(:keyword IS NULL OR :keyword = '' OR " +
                        "user_id = CAST(:keyword AS signed) OR " +
                        "BINARY user_name LIKE BINARY CONCAT('%', :keyword, '%') OR BINARY realname LIKE BINARY CONCAT('%', :keyword, '%') OR BINARY phone LIKE BINARY CONCAT('%', :keyword, '%'))",
           nativeQuery = true)
    Page<User> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
