package com.bank.bank.Converter;

import lombok.Data;
import java.util.Map;

@Data
public class ExchangeRateResponse {
    private String result;
    private String base_code;
    private Map<String, Float> rates;
}