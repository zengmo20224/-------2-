package com.petcare.ai.domain;

import java.util.List;

/**
 * Grounded context for AI customer service.
 * Only contains approved data sources: store, store_config, service items, products, FAQ.
 * This is passed to the Provider — never raw database rows.
 */
public record CustomerServiceContext(
        String storeName,
        String storeAddress,
        String businessHours,
        String phone,
        String homeServiceRadius,
        String cancellationPolicy,
        List<ServiceItemFact> services,
        List<ProductFact> products,
        List<FaqFact> faqs,
        boolean hasData
) {

    /**
     * A single service item fact for AI context.
     */
    public record ServiceItemFact(
            Long id,
            String name,
            String categoryName,
            String description,
            String price,
            String duration,
            String mode
    ) {}

    /**
     * A single product fact for AI context.
     */
    public record ProductFact(
            Long id,
            String name,
            String categoryName,
            String price,
            String stock
    ) {}

    /**
     * A single FAQ fact for AI context.
     */
    public record FaqFact(
            Long id,
            String question,
            String answer,
            String category
    ) {}

    /**
     * Creates an empty context with hasData = false.
     */
    public static CustomerServiceContext empty() {
        return new CustomerServiceContext(
                null, null, null, null, null, null,
                List.of(), List.of(), List.of(), false
        );
    }
}
