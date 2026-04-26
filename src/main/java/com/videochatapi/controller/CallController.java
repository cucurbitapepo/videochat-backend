package com.videochatapi.controller;

import com.videochatapi.dto.call.CallDto;
import com.videochatapi.dto.livekit.RoomTokenResponse;
import com.videochatapi.service.CallService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/calls")
@RequiredArgsConstructor
public class CallController {

  private final CallService callService;

  @PostMapping
  public ResponseEntity<CallDto> createCall(@RequestBody CallDto callDto) {
    return ResponseEntity.ok(callService.createCall(callDto));
  }

  @GetMapping("/{callId}")
  public ResponseEntity<CallDto> getCall(@PathVariable String callId) {
    return ResponseEntity.ok(callService.getCallById(callId));
  }

  @PostMapping("/{callId}/accept")
  public ResponseEntity<CallDto> acceptCall(@PathVariable String callId) {
    return ResponseEntity.ok(callService.acceptCall(callId));
  }

  @PostMapping("/{callId}/reject")
  public ResponseEntity<CallDto> rejectCall(@PathVariable String callId) {
    return ResponseEntity.ok(callService.rejectCall(callId));
  }

  @PostMapping("/{callId}/end")
  public ResponseEntity<CallDto> endCall(@PathVariable String callId) {
    return ResponseEntity.ok(callService.endCall(callId));
  }

  @GetMapping("/active")
  public ResponseEntity<List<CallDto>> getActiveCalls() {
    return ResponseEntity.ok(callService.getActiveCalls());
  }

  @GetMapping("/{callId}/token")
  public ResponseEntity<RoomTokenResponse> getCallToken(
          @PathVariable String callId,
          @RequestParam(required = false, defaultValue = "true") boolean isPublisher) {

    return ResponseEntity.ok(callService.getCallToken(callId, isPublisher));
  }
}
