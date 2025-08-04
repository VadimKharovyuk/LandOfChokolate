package com.example.landofchokolate.service.serviceImpl;

import com.example.landofchokolate.dto.product.PagedResponse;
import com.example.landofchokolate.dto.storeReview.CreateStoreReviewDTO;
import com.example.landofchokolate.dto.storeReview.StoreReviewResponseDTO;
import com.example.landofchokolate.mapper.StoreReviewMapper;
import com.example.landofchokolate.model.StoreReview;
import com.example.landofchokolate.repository.StoreReviewRepository;
import com.example.landofchokolate.service.StoreReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
@CacheConfig(
        cacheManager = "storeReviewCacheManager",
        cacheNames = {"storeReviewsList", "storeReviewsMainPage"}
)
public class StoreReviewServiceImpl implements StoreReviewService {

    private final StoreReviewMapper storeReviewMapper;
    private final StoreReviewRepository storeReviewRepository;

    // Очищаем оба кеша при создании отзыва
    @CacheEvict(value = {"storeReviewsList", "storeReviewsMainPage"}, allEntries = true)
    @Override
    public StoreReviewResponseDTO createReview(CreateStoreReviewDTO dto) {
        log.info("Создание нового отзыва, очищение кеша");
        StoreReview review = storeReviewMapper.toEntity(dto);
        StoreReview savedReview = storeReviewRepository.save(review);
        return storeReviewMapper.toResponseDTO(savedReview);
    }

    // Очищаем оба кеша при удалении отзыва
    @CacheEvict(value = {"storeReviewsList", "storeReviewsMainPage"}, allEntries = true)
    @Override
    public void deleteReview(Long id) {
        log.info("Удаление отзыва с ID: {}, очищение кеша", id);
        if (!storeReviewRepository.existsById(id)) {
            throw new RuntimeException("Отзыв не найден с id: " + id);
        }
        storeReviewRepository.deleteById(id);
    }

    // Кешируем пагинированные списки
    @Cacheable(value = "storeReviewsList",
            key = "#pageable.pageNumber + '_' + #pageable.pageSize + '_' + #pageable.sort.toString()")
    @Override
    public PagedResponse<StoreReviewResponseDTO> getAllReviews(Pageable pageable) {
        log.info("Загрузка отзывов из БД: страница {}, размер {}", pageable.getPageNumber(), pageable.getPageSize());

        // Получаем пагинированные данные из БД
        Page<StoreReview> reviewPage = storeReviewRepository.findAllByOrderByCreatedDesc(pageable);

        // Конвертируем в DTO
        List<StoreReviewResponseDTO> reviewDTOs = reviewPage.getContent()
                .stream()
                .map(storeReviewMapper::toResponseDTO)
                .collect(Collectors.toList());

        return new PagedResponse<>(reviewDTOs, reviewPage);
    }

    // Кешируем последние отзывы для главной страницы
    @Cacheable(value = "storeReviewsMainPage",
            key = "'latest_' + #limit")
    @Override
    public List<StoreReviewResponseDTO> getLatestReviews(int limit) {
        log.info("Загрузка последних {} отзывов из БД", limit);
        Pageable pageable = PageRequest.of(0, limit, Sort.by("created").descending());
        Page<StoreReview> reviews = storeReviewRepository.findAll(pageable);

        return reviews.getContent().stream()
                .map(storeReviewMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
}