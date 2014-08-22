package com.noths.ratel;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

class Mem {

    private final BigDecimal total;
    private final BigDecimal free;
    @JsonProperty("free_total")
    private final BigDecimal freeTotal;

    Mem() {
        final long max = Runtime.getRuntime().maxMemory();
        if (max == Long.MAX_VALUE) {
            free = toMB(Runtime.getRuntime().freeMemory());
        } else {
            free = toMB(max - Runtime.getRuntime().totalMemory() + Runtime.getRuntime().freeMemory());
        }
        freeTotal = null;
        total = toMB(Runtime.getRuntime().maxMemory());
    }

    public BigDecimal getTotal() {
        return total;
    }

    public BigDecimal getFree() {
        return free;
    }

    public BigDecimal getFreeTotal() {
        return freeTotal;
    }

    private BigDecimal toMB(final long bytes) {
        return new BigDecimal(bytes).divide(new BigDecimal(1024 * 1024));
    }
}
