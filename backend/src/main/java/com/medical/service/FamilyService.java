package com.medical.service;

import com.medical.dto.FamilyAuthDTO;
import com.medical.dto.FamilyMemberDTO;
import com.medical.entity.FamilyAuth;
import com.medical.entity.FamilyMember;
import com.medical.entity.ReminderLog;
import com.medical.repository.FamilyAuthRepository;
import com.medical.repository.FamilyMemberRepository;
import com.medical.repository.ReminderLogRepository;
import com.medical.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class FamilyService {

    @Autowired
    private FamilyMemberRepository familyMemberRepository;

    @Autowired
    private FamilyAuthRepository familyAuthRepository;

    @Autowired
    private ReminderLogRepository reminderLogRepository;

    @Autowired
    private UserRepository userRepository;

    public FamilyMember addFamilyMember(Long userId, FamilyMemberDTO dto) {
        if (dto.getFamilyUserId() != null) {
            if (!userRepository.existsById(dto.getFamilyUserId())) {
                throw new RuntimeException("家属用户不存在");
            }
            if (familyMemberRepository.findByUserIdAndFamilyUserId(userId, dto.getFamilyUserId()).isPresent()) {
                throw new RuntimeException("该用户已添加为家属");
            }
        }

        FamilyMember member = new FamilyMember();
        member.setUserId(userId);
        member.setFamilyUserId(dto.getFamilyUserId());
        member.setRealname(dto.getRealname());
        member.setPhone(dto.getPhone());
        member.setRelation(dto.getRelation());
        member.setPermissionLevel(dto.getPermissionLevel());
        member.setStatus(1);

        FamilyMember saved = familyMemberRepository.save(member);

        FamilyAuth auth = buildDefaultAuth(dto.getPermissionLevel());
        auth.setUserId(userId);
        auth.setMemberId(saved.getMemberId());
        familyAuthRepository.save(auth);

        return saved;
    }

    private FamilyAuth buildDefaultAuth(String permissionLevel) {
        FamilyAuth auth = new FamilyAuth();
        if ("advanced".equals(permissionLevel)) {
            auth.setViewMedicalRecord(1);
            auth.setViewMedication(1);
            auth.setViewStatistics(1);
            auth.setReceiveMissAlert(1);
            auth.setReceiveEmergency(1);
            auth.setDisconnAlert(1);
        } else {
            auth.setViewMedicalRecord(0);
            auth.setViewMedication(1);
            auth.setViewStatistics(0);
            auth.setReceiveMissAlert(1);
            auth.setReceiveEmergency(0);
            auth.setDisconnAlert(0);
        }
        return auth;
    }

    public List<FamilyMember> getFamilyMembers(Long userId) {
        return familyMemberRepository.findByUserId(userId);
    }

    public Optional<FamilyMember> getFamilyMember(Long userId, Long memberId) {
        return familyMemberRepository.findById(memberId)
                .filter(member -> member.getUserId().equals(userId));
    }

    public FamilyMember updateFamilyMember(Long userId, Long memberId, FamilyMemberDTO dto) {
        return familyMemberRepository.findById(memberId)
                .filter(member -> member.getUserId().equals(userId))
                .map(member -> {
                    if (dto.getRealname() != null) member.setRealname(dto.getRealname());
                    if (dto.getPhone() != null) member.setPhone(dto.getPhone());
                    if (dto.getRelation() != null) member.setRelation(dto.getRelation());
                    if (dto.getPermissionLevel() != null && !dto.getPermissionLevel().equals(member.getPermissionLevel())) {
                        member.setPermissionLevel(dto.getPermissionLevel());
                        updateAuthByPermissionLevel(userId, memberId, dto.getPermissionLevel());
                    }
                    return familyMemberRepository.save(member);
                })
                .orElseThrow(() -> new RuntimeException("家属成员不存在"));
    }

    private void updateAuthByPermissionLevel(Long userId, Long memberId, String permissionLevel) {
        familyAuthRepository.findByMemberId(memberId)
                .filter(auth -> auth.getUserId().equals(userId))
                .ifPresent(auth -> {
                    FamilyAuth defaultAuth = buildDefaultAuth(permissionLevel);
                    auth.setViewMedicalRecord(defaultAuth.getViewMedicalRecord());
                    auth.setViewMedication(defaultAuth.getViewMedication());
                    auth.setViewStatistics(defaultAuth.getViewStatistics());
                    auth.setReceiveMissAlert(defaultAuth.getReceiveMissAlert());
                    auth.setReceiveEmergency(defaultAuth.getReceiveEmergency());
                    auth.setDisconnAlert(defaultAuth.getDisconnAlert());
                    familyAuthRepository.save(auth);
                });
    }

    public void deleteFamilyMember(Long userId, Long memberId) {
        if (!familyMemberRepository.findById(memberId)
                .filter(member -> member.getUserId().equals(userId))
                .isPresent()) {
            throw new RuntimeException("家属成员不存在");
        }
        familyAuthRepository.deleteByMemberId(memberId);
        familyMemberRepository.deleteById(memberId);
    }

    public FamilyAuth getFamilyAuth(Long userId, Long memberId) {
        return familyAuthRepository.findByMemberId(memberId)
                .filter(auth -> auth.getUserId().equals(userId))
                .orElseThrow(() -> new RuntimeException("权限记录不存在"));
    }

    public FamilyAuth updateFamilyAuth(Long userId, FamilyAuthDTO dto) {
        return familyAuthRepository.findByMemberId(dto.getMemberId())
                .filter(auth -> auth.getUserId().equals(userId))
                .map(auth -> {
                    if (dto.getViewMedicalRecord() != null) auth.setViewMedicalRecord(dto.getViewMedicalRecord());
                    if (dto.getViewMedication() != null) auth.setViewMedication(dto.getViewMedication());
                    if (dto.getViewStatistics() != null) auth.setViewStatistics(dto.getViewStatistics());
                    if (dto.getReceiveMissAlert() != null) auth.setReceiveMissAlert(dto.getReceiveMissAlert());
                    if (dto.getReceiveEmergency() != null) auth.setReceiveEmergency(dto.getReceiveEmergency());
                    if (dto.getDisconnAlert() != null) auth.setDisconnAlert(dto.getDisconnAlert());
                    return familyAuthRepository.save(auth);
                })
                .orElseThrow(() -> new RuntimeException("权限记录不存在"));
    }

    public List<FamilyAuth> getAllFamilyAuth(Long userId) {
        return familyAuthRepository.findByUserId(userId);
    }

    public List<ReminderLog> getFamilyReminderLogs(Long userId, Long memberId) {
        FamilyMember member = familyMemberRepository.findById(memberId)
                .filter(m -> m.getUserId().equals(userId))
                .orElseThrow(() -> new RuntimeException("家属成员不存在"));

        if (member.getFamilyUserId() == null) {
            return java.util.Collections.emptyList();
        }

        return reminderLogRepository.findByUserIdOrderByCreateTimeDesc(member.getFamilyUserId());
    }

    public List<ReminderLog> getReminderLogsByFamilyUserId(Long familyUserId) {
        return reminderLogRepository.findByUserIdOrderByCreateTimeDesc(familyUserId);
    }
}