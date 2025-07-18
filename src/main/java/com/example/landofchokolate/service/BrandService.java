package com.example.landofchokolate.service;

import com.example.landofchokolate.dto.brend.BrandFilterDto;
import com.example.landofchokolate.dto.brend.BrandProjection;
import com.example.landofchokolate.dto.brend.BrandResponseDto;
import com.example.landofchokolate.dto.brend.CreateBrandDto;

import java.util.List;

public interface BrandService {

    BrandResponseDto createBrand(CreateBrandDto createBrandDto);

    List<BrandResponseDto> getAllBrands();

    BrandResponseDto getBrandById(Long id);

    BrandResponseDto updateBrand(Long id, CreateBrandDto updateBrandDto);

    List<BrandProjection> getBrandsForFilters();
    void deleteBrand(Long id);
}
