package com.example.OrderService.service;

import com.example.OrderService.dto.ItemResponseDTO;
import com.example.OrderService.dto.ProductDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ItemService {
    ItemResponseDTO createItem(ProductDto dto);
    ItemResponseDTO updateItem(Long id, ProductDto dto );
    void deleteItem(Long id);
    Page<ItemResponseDTO> getAllItems(Pageable pageable);
}
