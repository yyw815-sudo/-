package com.medical.repository;

import com.medical.entity.FamilyMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FamilyMemberRepository extends JpaRepository<FamilyMember, Long> {

    List<FamilyMember> findByUserId(Long userId);

    Optional<FamilyMember> findByUserIdAndFamilyUserId(Long userId, Long familyUserId);

    void deleteByUserId(Long userId);
}