package com.example.landofchokolate.dto.product;

import lombok.Data;


import java.math.BigDecimal;
///недавно просмотренные
@Data
public class RecentlyViewedProduct {
    private Long id;
    private String slug;
    private String name;
    private String imageUrl;
    private BigDecimal price;
    private boolean inStock;
}
