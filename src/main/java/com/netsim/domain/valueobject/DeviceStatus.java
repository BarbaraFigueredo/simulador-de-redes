package com.netsim.domain.valueobject;

/**
 * Status operacional de um dispositivo de rede.
 * ACTIVE  = dispositivo ligado e respondendo
 * INACTIVE = dispositivo desligado
 * ERROR   = dispositivo com falha (simula crash ou timeout)
 */
public enum DeviceStatus {
    ACTIVE,
    INACTIVE,
    ERROR
}