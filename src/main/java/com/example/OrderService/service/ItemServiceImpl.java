package com.example.OrderService.service;

import com.example.OrderService.dto.ItemResponseDTO;
import com.example.OrderService.dto.ProductDto;
import com.example.OrderService.entity.Item;
import com.example.OrderService.mapper.ItemMapper;
import com.example.OrderService.repository.ItemRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final ItemMapper itemMapper;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public ItemResponseDTO createItem(ProductDto dto) {
        Item item=itemMapper.toEntity(dto);
        Item savedItem =itemRepository.save(item);
        return itemMapper.toDto(savedItem);
    }

    @Override
    @Transactional
    public ItemResponseDTO updateItem(Long id, ProductDto dto) {
        Item item=itemRepository.findById(id)
                .orElseThrow(()->new EntityNotFoundException("Item not found"));
        itemMapper.updateEntityFromDto(dto,item);
        Item saved=itemRepository.save(item);
        return itemMapper.toDto(saved);
    }


    @Override
    @Transactional
    public void deleteItem(Long id) {
        Item item=itemRepository.findById(id)
                .orElseThrow(()->new EntityNotFoundException("Item not found"));
        itemRepository.delete(item);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ItemResponseDTO> getAllItems(Pageable pageable) {
        return itemRepository.findAll(pageable)
                .map(itemMapper::toDto);
    }

}
