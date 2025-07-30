package com.vb.projects.airBnbApp.strategy;

import com.vb.projects.airBnbApp.entity.Inventory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public interface PricingStrategy {
    public BigDecimal calculatePrice(Inventory inventory);
}
