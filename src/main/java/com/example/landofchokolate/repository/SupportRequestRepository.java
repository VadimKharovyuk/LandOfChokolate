package com.example.landofchokolate.repository;

import com.example.landofchokolate.model.SupportRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupportRequestRepository extends JpaRepository<SupportRequest, Long> {
}
