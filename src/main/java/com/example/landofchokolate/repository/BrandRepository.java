package com.example.landofchokolate.repository;

import com.example.landofchokolate.dto.brend.BrandFilterDto;
import com.example.landofchokolate.dto.brend.BrandProjection;
import com.example.landofchokolate.model.Brand;
import org.hibernate.type.descriptor.converter.spi.JpaAttributeConverter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {

    List<BrandProjection> findAllByOrderByNameAsc();
}
