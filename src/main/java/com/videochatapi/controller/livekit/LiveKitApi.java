package com.videochatapi.controller.livekit;

import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;

public interface LiveKitApi {

  @POST("twirp/livekit.RoomService/CreateRoom")
  Call<Room> createRoom(@Body CreateRoomRequest request);

  @POST("twirp/livekit.RoomService/ListRooms")
  Call<ListRoomsResponse> listRooms(@Body ListRoomsRequest request);

  @POST("twirp/livekit.RoomService/DeleteRoom")
  Call<Void> deleteRoom(@Body DeleteRoomRequest request);


  class CreateRoomRequest {
    private String name;
    private Integer emptyTimeout;
    private Integer maxParticipants;
    private String node;
    private Boolean metadata;
    private Boolean egress;

    public CreateRoomRequest(String name, Integer emptyTimeout, Integer maxParticipants) {
      this.name = name;
      this.emptyTimeout = emptyTimeout;
      this.maxParticipants = maxParticipants;
    }

    public String getName() { return name; }
    public Integer getEmptyTimeout() { return emptyTimeout; }
    public Integer getMaxParticipants() { return maxParticipants; }
    public String getNode() { return node; }
    public Boolean getMetadata() { return metadata; }
    public Boolean getEgress() { return egress; }
  }

  class Room {
    private String name;
    private String sid;
    private Integer numParticipants;
    private Integer numPublishers;
    private Long creationTime;
    private Long turnedOn;
    private String metadata;
    private Boolean activeRecording;
    private String timeout;

    public String getName() { return name; }
    public String getSid() { return sid; }
    public Integer getNumParticipants() { return numParticipants; }
    public Integer getNumPublishers() { return numPublishers; }
    public Long getCreationTime() { return creationTime; }
    public Long getTurnedOn() { return turnedOn; }
    public String getMetadata() { return metadata; }
    public Boolean getActiveRecording() { return activeRecording; }
    public String getTimeout() { return timeout; }
  }

  class ListRoomsRequest {
    private List<String> names;

    public ListRoomsRequest(List<String> names) {
      this.names = names;
    }

    public List<String> getNames() { return names; }
  }

  class ListRoomsResponse {
    private List<Room> rooms;

    public List<Room> getRooms() { return rooms; }
  }

  class DeleteRoomRequest {
    private String room;

    public DeleteRoomRequest(String roomName) {
      this.room = roomName;
    }

    public String getRoom() { return room; }
  }

}