package com.example.landofchokolate.service.serviceImpl;

import com.example.landofchokolate.dto.brend.BrandResponseDto;
import com.example.landofchokolate.dto.brend.CreateBrandDto;
import com.example.landofchokolate.mapper.BrandMapper;
import com.example.landofchokolate.model.Brand;
import com.example.landofchokolate.repository.BrandRepository;
import com.example.landofchokolate.service.BrandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BrandServiceImpl implements BrandService {
    private final BrandRepository brandRepository;
    private final BrandMapper brandMapper ;

    @Override
    public BrandResponseDto createBrand(CreateBrandDto createBrandDto) {
        log.info("Creating brand with name: {}", createBrandDto.getName());

        Brand brand = brandMapper.toEntity(createBrandDto);
        Brand savedBrand = brandRepository.save(brand);

        log.info("Brand created successfully with id: {}", savedBrand.getId());
        return brandMapper.toResponseDto(savedBrand);
    }

    @Override
    public List<BrandResponseDto> getAllBrands() {
        log.info("Fetching all brands");
        List<Brand> brands = brandRepository.findAll();
        return brandMapper.toResponseDtoList(brands);
    }

    @Override
    public BrandResponseDto getBrandById(Long id) {
        log.info("Fetching brand with id: {}", id);
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Brand not found with id: " + id));
        return brandMapper.toResponseDto(brand);
    }

    @Override
    public BrandResponseDto updateBrand(Long id, CreateBrandDto updateBrandDto) {
        log.info("Updating brand with id: {} and name: {}", id, updateBrandDto.getName());

        Brand existingBrand = brandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Brand not found with id: " + id));

        brandMapper.updateEntityFromDto(updateBrandDto, existingBrand);
        Brand updatedBrand = brandRepository.save(existingBrand);

        log.info("Brand updated successfully with id: {}", updatedBrand.getId());
        return brandMapper.toResponseDto(updatedBrand);
    }

    @Override
    public void deleteBrand(Long id) {
        log.info("Deleting brand with id: {}", id);

        if (!brandRepository.existsById(id)) {
            throw new RuntimeException("Brand not found with id: " + id);
        }

        brandRepository.deleteById(id);
        log.info("Brand deleted successfully with id: {}", id);
    }
}
