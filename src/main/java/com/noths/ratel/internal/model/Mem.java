package com.noths.ratel.internal.model;

/*
 * #%L
 * Ratel Library
 * %%
 * Copyright (C) 2014 notonthehighstreet.com
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * Honeybadger API class. Contains information about system memory.
 */
public class Mem {

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
