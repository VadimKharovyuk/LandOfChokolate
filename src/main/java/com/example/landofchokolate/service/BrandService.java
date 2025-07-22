package com.example.landofchokolate.service;

import com.example.landofchokolate.dto.brend.*;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BrandService {

    BrandResponseDto createBrand(CreateBrandDto createBrandDto);

    List<BrandResponseDto> getAllBrands();

    BrandResponseDto getBrandById(Long id);

    BrandResponseDto updateBrand(Long id, CreateBrandDto updateBrandDto);

    List<BrandProjection> getBrandsForFilters();
    void deleteBrand(Long id);

    BrandPageResponseDto getBrandsForClient(Pageable pageable);
    BrandClientDto getBrandBySlug(String slug);

    List<BrandClientDto> getBrandByLimit(int limit);
}
