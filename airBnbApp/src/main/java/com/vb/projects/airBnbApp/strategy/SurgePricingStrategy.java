package com.vb.projects.airBnbApp.strategy;

import com.vb.projects.airBnbApp.entity.Inventory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class SurgePricingStrategy implements PricingStrategy {

    private final PricingStrategy pricingStrategy;

    @Override
    public BigDecimal calculatePrice(Inventory inventory) {
        return pricingStrategy.calculatePrice(inventory).multiply(inventory.getSurgeFactor());
    }
}
