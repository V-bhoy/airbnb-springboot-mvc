package com.vb.projects.airBnbApp.controller;

import com.vb.projects.airBnbApp.dto.InventoryDto;
import com.vb.projects.airBnbApp.dto.UpdateInventoryRequestDto;
import com.vb.projects.airBnbApp.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<List<InventoryDto>> getAllInventories(@PathVariable Long roomId) {
        return ResponseEntity.ok(inventoryService.getAllInventories(roomId));
    }

    @PatchMapping("/rooms/{roomId}")
    public ResponseEntity<Void> updateInventories(@PathVariable Long roomId, @RequestBody UpdateInventoryRequestDto updateInventoryRequestDto) {
        inventoryService.updateInventories(roomId, updateInventoryRequestDto);
        return ResponseEntity.noContent().build();
    }


}
