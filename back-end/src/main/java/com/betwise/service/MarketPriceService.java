package com.betwise.service;

import java.math.BigDecimal;

public interface MarketPriceService {

    BigDecimal getCurrentPrice(String ticker);
}