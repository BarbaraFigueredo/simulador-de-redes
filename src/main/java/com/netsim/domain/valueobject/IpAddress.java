package com.netsim.domain.valueobject;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value Object que representa um endereço IPv4.
 *
 * Regras de redes: endereços IP são identificadores lógicos de camada 3
 * compostos por 4 octetos (32 bits). Ex: 192.168.1.10
 *
 * Value Objects são imutáveis e sua identidade é definida pelo valor,
 * não por referência — princípio do DDD (Domain-Driven Design).
 */
public final class IpAddress {

    private static final Pattern IPV4_PATTERN =
            Pattern.compile("^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$");

    private final String value;

    private IpAddress(String value) {
        this.value = value;
    }

    public static IpAddress of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Endereço IP não pode ser nulo ou vazio.");
        }
        if (!IPV4_PATTERN.matcher(value.trim()).matches()) {
            throw new IllegalArgumentException("Endereço IP inválido: " + value);
        }
        return new IpAddress(value.trim());
    }

    public String getValue() {
        return value;
    }

    /** Retorna os primeiros 3 octetos como rede /24. Ex: "192.168.1" */
    public String getNetworkAddress() {
        String[] parts = value.split("\\.");
        return parts[0] + "." + parts[1] + "." + parts[2] + ".0/24";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IpAddress)) return false;
        return value.equals(((IpAddress) o).value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}