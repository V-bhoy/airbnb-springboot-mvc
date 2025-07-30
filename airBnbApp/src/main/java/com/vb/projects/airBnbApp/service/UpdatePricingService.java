package com.vb.projects.airBnbApp.service;

import com.vb.projects.airBnbApp.entity.Hotel;
import com.vb.projects.airBnbApp.entity.HotelMinPrice;
import com.vb.projects.airBnbApp.entity.Inventory;
import com.vb.projects.airBnbApp.repository.HotelMinPriceRepository;
import com.vb.projects.airBnbApp.repository.HotelRepository;
import com.vb.projects.airBnbApp.repository.InventoryRepository;
import com.vb.projects.airBnbApp.strategy.PricingService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdatePricingService {

    // scheduler to update the inventory and hotel min price table every hour

    private final HotelRepository hotelRepository;
    private final InventoryRepository inventoryRepository;
    private final HotelMinPriceRepository hotelMinPriceRepository;
    private final PricingService pricingService;

    @Scheduled(cron = "0 0 * * * *") // cron expression for every hour
    public void updatePrices(){
        int page = 0;
        int batchSize = 100;

        while(true){
            Page<Hotel> hotel = hotelRepository.findAll(PageRequest.of(page, batchSize));
            if(hotel.isEmpty()){
                hotel.getContent().forEach(this::updateHotelPrices);
                break;
            }
            page++;
        }
    }

    private void updateHotelPrices(Hotel hotel){
      LocalDate startDate = LocalDate.now();
      LocalDate endDate = LocalDate.now().plusYears(1);
      List<Inventory> inventories = inventoryRepository.findByHotelAndDateBetween(hotel, startDate, endDate);
      updateInventoryPrices(inventories);
      updateHotelMinPrice(hotel, inventories, startDate, endDate);
    }

    private void updateHotelMinPrice(Hotel hotel, List<Inventory> inventories, LocalDate startDate, LocalDate endDate) {
        // compute minimum price per day for the hotel
        Map<LocalDate, BigDecimal> dailyMinPrices = inventories
                .stream()
                .collect(Collectors.groupingBy(
                        Inventory::getDate,
                        Collectors.mapping(Inventory::getPrice, Collectors.minBy(Comparator.naturalOrder()))
                        )
                ).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().orElse(BigDecimal.ZERO)));

        // prepare hotel price entities in bulk
        List<HotelMinPrice> hotelMinPrices = new ArrayList<>();
        dailyMinPrices.forEach((date, price) -> {
            HotelMinPrice hotelMinPrice = hotelMinPriceRepository
                    .findByHotelAndDate(hotel, date).orElse(new HotelMinPrice(hotel, date));
            hotelMinPrice.setPrice(price);
            hotelMinPrices.add(hotelMinPrice);
        });

        hotelMinPriceRepository.saveAll(hotelMinPrices);
    }

    private void updateInventoryPrices(List<Inventory> inventories){
      inventories.forEach(inventory -> {
          BigDecimal dynamicPrice = pricingService.calculateDynamicPricing(inventory);
          inventory.setPrice(dynamicPrice);
      });
      inventoryRepository.saveAll(inventories);
    }

}
