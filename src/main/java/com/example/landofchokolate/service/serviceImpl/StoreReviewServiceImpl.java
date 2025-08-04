package com.example.landofchokolate.service.serviceImpl;

import com.example.landofchokolate.dto.product.PagedResponse;
import com.example.landofchokolate.dto.storeReview.CreateStoreReviewDTO;
import com.example.landofchokolate.dto.storeReview.StoreReviewResponseDTO;
import com.example.landofchokolate.mapper.StoreReviewMapper;
import com.example.landofchokolate.model.StoreReview;
import com.example.landofchokolate.repository.StoreReviewRepository;
import com.example.landofchokolate.service.StoreReviewService;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StoreReviewServiceImpl implements StoreReviewService {
    private final StoreReviewMapper storeReviewMapper;
    private final StoreReviewRepository storeReviewRepository;


    @Override
    public StoreReviewResponseDTO createReview(CreateStoreReviewDTO dto) {
        StoreReview review = storeReviewMapper.toEntity(dto);
        StoreReview savedReview = storeReviewRepository.save(review);
        return storeReviewMapper.toResponseDTO(savedReview);
    }

    @Override
    public void deleteReview(Long id) {
        if (!storeReviewRepository.existsById(id)) {
            throw new RuntimeException("Отзыв не найден с id: " + id);
        }
        storeReviewRepository.deleteById(id);
    }

    @Override
    public PagedResponse<StoreReviewResponseDTO> getAllReviews(Pageable pageable) {
        log.info("Получение отзывов: страница {}, размер {}", pageable.getPageNumber(), pageable.getPageSize());

        // Получаем пагинированные данные из БД
        Page<StoreReview> reviewPage = storeReviewRepository.findAllByOrderByCreatedDesc(pageable);

        // Конвертируем в DTO
        List<StoreReviewResponseDTO> reviewDTOs = reviewPage.getContent()
                .stream()
                .map(storeReviewMapper::toResponseDTO)
                .collect(Collectors.toList());

        log.info("Найдено {} отзывов из {} общих", reviewDTOs.size(), reviewPage.getTotalElements());

        return new PagedResponse<>(reviewDTOs, reviewPage);
    }

    @Override
    public List<StoreReviewResponseDTO> getLatestReviews(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by("created").descending());
        Page<StoreReview> reviews = storeReviewRepository.findAll(pageable);

        return reviews.getContent().stream()
                .map(storeReviewMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
}
