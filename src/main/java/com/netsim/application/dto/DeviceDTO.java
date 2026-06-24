package com.netsim.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Data Transfer Object para serialização/desserialização de dispositivos.
 * Usado para exportar/importar topologia em JSON.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceDTO {
    public String id;
    public String name;
    public String ipAddress;
    public String type;
    public String status;
    public String operatingSystem;
    public double positionX;
    public double positionY;

    public DeviceDTO() {}

    public DeviceDTO(String id, String name, String ipAddress, String type,
                     String status, String os, double x, double y) {
        this.id              = id;
        this.name            = name;
        this.ipAddress       = ipAddress;
        this.type            = type;
        this.status          = status;
        this.operatingSystem = os;
        this.positionX       = x;
        this.positionY       = y;
    }
}