package com.example.landofchokolate.repository;

import com.example.landofchokolate.model.StoreReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreReviewRepository extends JpaRepository<StoreReview, Long> {
    Page<StoreReview> findAllByOrderByCreatedDesc(Pageable pageable);

}
