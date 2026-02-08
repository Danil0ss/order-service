package com.example.OrderService.service;

import com.example.OrderService.dto.ItemResponseDTO;
import com.example.OrderService.dto.ProductDto;
import com.example.OrderService.entity.Item;
import com.example.OrderService.mapper.ItemMapper;
import com.example.OrderService.repository.ItemRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;
    @Mock
    private ItemMapper itemMapper;

    @InjectMocks
    private ItemServiceImpl itemService;

    @Test
    void createItem_ShouldReturnCreatedItem() {
        ProductDto request = new ProductDto("Laptop", BigDecimal.valueOf(1000));
        Item item = new Item();
        item.setName("Laptop");
        Item savedItem = new Item();
        savedItem.setId(1L);
        savedItem.setName("Laptop");

        when(itemMapper.toEntity(request)).thenReturn(item);
        when(itemRepository.save(item)).thenReturn(savedItem);
        when(itemMapper.toDto(savedItem)).thenReturn(new ItemResponseDTO(1L, "Laptop", BigDecimal.valueOf(1000)));

        ItemResponseDTO result = itemService.createItem(request);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        verify(itemRepository).save(item);
    }

    @Test
    void updateItem_ShouldUpdateAndReturnItem_WhenExists() {
        Long id = 1L;
        ProductDto request = new ProductDto("Updated Laptop", BigDecimal.valueOf(1200));
        Item existingItem = new Item();
        existingItem.setId(id);

        when(itemRepository.findById(id)).thenReturn(Optional.of(existingItem));
        when(itemRepository.save(existingItem)).thenReturn(existingItem);
        when(itemMapper.toDto(existingItem)).thenReturn(new ItemResponseDTO(id, "Updated Laptop", BigDecimal.valueOf(1200)));

        ItemResponseDTO result = itemService.updateItem(id, request);

        assertThat(result).isNotNull();
        verify(itemMapper).updateEntityFromDto(request, existingItem);
        verify(itemRepository).save(existingItem);
    }

    @Test
    void updateItem_ShouldThrowException_WhenNotFound() {
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> itemService.updateItem(1L, new ProductDto("Name", BigDecimal.TEN)));
    }

    @Test
    void deleteItem_ShouldCallRepositoryDelete() {
        Long id = 1L;
        Item item = new Item();
        when(itemRepository.findById(id)).thenReturn(Optional.of(item));

        itemService.deleteItem(id);

        verify(itemRepository).delete(item);
    }
}