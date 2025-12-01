    package com.example.OrderService.mapper;

    import com.example.OrderService.dto.ItemResponseDTO;
    import com.example.OrderService.dto.ProductDto;
    import com.example.OrderService.entity.Item;
    import org.mapstruct.Mapper;
    import org.mapstruct.Mapping;
    import org.mapstruct.MappingTarget;

    @Mapper(componentModel = "spring")
    public interface ItemMapper {
        @Mapping(target = "id", ignore = true)
        @Mapping(target = "createdAt",ignore = true)
        @Mapping(target = "updatedAt",ignore = true)
        Item toEntity(ProductDto dto);

        ItemResponseDTO toDto(Item item);

        @Mapping(target = "id", ignore = true)
        @Mapping(target = "createdAt", ignore = true)
        @Mapping(target = "updatedAt", ignore = true)
        void updateEntityFromDto(ProductDto dto, @MappingTarget Item item);

    }
