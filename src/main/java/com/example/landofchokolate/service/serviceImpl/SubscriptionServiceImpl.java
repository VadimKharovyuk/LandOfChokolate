package com.example.landofchokolate.service.serviceImpl;

import com.example.landofchokolate.dto.subscription.CreateSubscriptionRequest;
import com.example.landofchokolate.dto.subscription.SubscriptionResponse;
import com.example.landofchokolate.exception.SubscriptionAlreadyExistsException;
import com.example.landofchokolate.exception.SubscriptionNotFoundException;
import com.example.landofchokolate.mapper.SubscriptionMapper;
import com.example.landofchokolate.model.Subscription;
import com.example.landofchokolate.repository.SubscriptionRepository;
import com.example.landofchokolate.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionMapper subscriptionMapper;

    @Transactional
    public SubscriptionResponse createSubscription(CreateSubscriptionRequest request) {
        String normalizedEmail = request.getEmail().toLowerCase().trim();
        log.info("Попытка создания подписки для email: {}", normalizedEmail);

        if (subscriptionRepository.existsByEmail(normalizedEmail)) {
            throw new SubscriptionAlreadyExistsException("Подписка с таким email уже существует");
        }

        Subscription subscription = Subscription.builder()
                .email(normalizedEmail)
                .active(true)
                .build();

        Subscription savedSubscription = subscriptionRepository.save(subscription);

        log.info("Успешно создана подписка с id: {} для email: {}",
                savedSubscription.getId(), savedSubscription.getEmail());

        return subscriptionMapper.toResponse(savedSubscription);
    }

    @Override
    public List<SubscriptionResponse> getAllActiveSubscriptions() {
        List<Subscription> activeSubscriptions = subscriptionRepository.findActiveSubscriptions();
        return activeSubscriptions.stream()
                .map(subscriptionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteSubscription(Long id) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new SubscriptionNotFoundException("Подписка с id " + id + " не найдена"));

        subscription.setActive(false);
        subscriptionRepository.save(subscription);

        log.info("Подписка с id {} деактивирована", id);
    }
}
