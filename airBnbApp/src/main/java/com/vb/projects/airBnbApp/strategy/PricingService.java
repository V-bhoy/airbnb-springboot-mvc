package com.vb.projects.airBnbApp.strategy;

import com.vb.projects.airBnbApp.entity.Inventory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PricingService {
    // Reasons
    // separation of concern
    // open closed principle, to modify later

    public BigDecimal calculateDynamicPricing(Inventory inventory) {
        PricingStrategy basePricing = new BasePricingStrategy();
        // apply additional strategies
        basePricing = new SurgePricingStrategy(basePricing);
        basePricing = new OccupancyPricingStrategy(basePricing);
        basePricing = new UrgencyPricingStrategy(basePricing);
        basePricing = new HolidayPricingStrategy(basePricing);

        return basePricing.calculatePrice(inventory);
    }
}
