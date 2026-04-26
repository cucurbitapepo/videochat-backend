package com.videochatapi.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

  private final LiveKit livekit = new LiveKit();

  @Data
  public static class LiveKit {
    private String internalUrl;
    private String externalUrl;
    private String apiKey;
    private String apiSecret;
  }
}