package com.medical.repository;

import com.medical.entity.FamilyAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FamilyAuthRepository extends JpaRepository<FamilyAuth, Long> {

    Optional<FamilyAuth> findByMemberId(Long memberId);

    List<FamilyAuth> findByUserId(Long userId);

    void deleteByMemberId(Long memberId);

    void deleteByUserId(Long userId);
}