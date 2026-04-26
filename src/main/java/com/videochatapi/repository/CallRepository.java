package com.videochatapi.repository;

import com.videochatapi.model.Call;
import com.videochatapi.model.CallStatus;
import com.videochatapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CallRepository extends JpaRepository<Call, Long> {
  Optional<Call> findByCallId(String callId);
  Optional<Call> findByParticipantsContainingAndStatusNot(User participant, CallStatus status);
}
