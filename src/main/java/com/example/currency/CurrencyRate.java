package com.example.currency;

import java.time.LocalDate;

public class CurrencyRate {
    private String code;
    private double value;
    private LocalDate date;

    public CurrencyRate(String code, double value, LocalDate date) {
        this.code = code;
        this.value = value;
        this.date = date;
    }

    public String getCode() {
        return code;
    }

    public double getValue() {
        return value;
    }

    public LocalDate getDate() {
        return date;
    }

    @Override
    public String toString() {
        return String.format("%s: %.4f (%s)", code, value, date);
    }
}