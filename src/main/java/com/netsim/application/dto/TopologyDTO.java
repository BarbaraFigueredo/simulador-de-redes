package com.netsim.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TopologyDTO {
    public String            version  = "1.0";
    public List<DeviceDTO>   devices  = new ArrayList<>();
    public List<ConnectionDTO> connections = new ArrayList<>();

    public TopologyDTO() {}
}