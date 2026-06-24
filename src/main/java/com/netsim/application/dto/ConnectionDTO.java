package com.netsim.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConnectionDTO {
    public String id;
    public String deviceAId;
    public String deviceBId;
    public int    latencyMs;
    public int    bandwidthMbps;

    public ConnectionDTO() {}

    public ConnectionDTO(String id, String deviceAId, String deviceBId,
                         int latencyMs, int bandwidthMbps) {
        this.id            = id;
        this.deviceAId     = deviceAId;
        this.deviceBId     = deviceBId;
        this.latencyMs     = latencyMs;
        this.bandwidthMbps = bandwidthMbps;
    }
}