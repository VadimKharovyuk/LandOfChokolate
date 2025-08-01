package com.example.landofchokolate.service;

import com.example.landofchokolate.dto.novaposhta.*;
import com.example.landofchokolate.dto.order.OrderDTO;

import java.util.List;

public interface PoshtaService {

    List<City> getCities();
    List<Department> getDepartments(String cityRef);
    String createDelivery(OrderDTO order);
    TrackingInfo trackDelivery(String trackingNumber);
}
