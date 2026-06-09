package com.petcare.ai.domain;

import com.petcare.ai.entity.FaqKnowledge;
import com.petcare.ai.mapper.FaqKnowledgeMapper;
import com.petcare.product.mapper.ProductMapper;
import com.petcare.product.entity.Product;
import com.petcare.service.entity.ServiceItem;
import com.petcare.service.mapper.ServiceItemMapper;
import com.petcare.store.entity.Store;
import com.petcare.store.entity.StoreConfig;
import com.petcare.store.mapper.StoreConfigMapper;
import com.petcare.store.mapper.StoreMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.List;

/**
 * Builds the grounded context for AI customer service.
 * Only reads approved data sources: store, store_config, service items, products, FAQ.
 * Never exposes raw database rows, internal IDs for user display, or sensitive data.
 */
public class CustomerServiceContextBuilder {

    private final StoreMapper storeMapper;
    private final StoreConfigMapper storeConfigMapper;
    private final ServiceItemMapper serviceItemMapper;
    private final ProductMapper productMapper;
    private final FaqKnowledgeMapper faqKnowledgeMapper;

    public CustomerServiceContextBuilder(
            StoreMapper storeMapper,
            StoreConfigMapper storeConfigMapper,
            ServiceItemMapper serviceItemMapper,
            ProductMapper productMapper,
            FaqKnowledgeMapper faqKnowledgeMapper) {
        this.storeMapper = storeMapper;
        this.storeConfigMapper = storeConfigMapper;
        this.serviceItemMapper = serviceItemMapper;
        this.productMapper = productMapper;
        this.faqKnowledgeMapper = faqKnowledgeMapper;
    }

    /**
     * Builds the full customer service context from approved data sources.
     *
     * @return a grounded context, or empty context if no data is available
     */
    public CustomerServiceContext build() {
        Store store = findFirstActiveStore();
        if (store == null) {
            return CustomerServiceContext.empty();
        }

        StoreConfig config = storeConfigMapper.selectOne(
                new QueryWrapper<StoreConfig>().eq("store_id", store.getId()));

        List<ServiceItem> activeServices = serviceItemMapper.selectList(
                new QueryWrapper<ServiceItem>()
                        .eq("status", "ON_SALE")
                        .eq("deleted", 0));

        List<Product> activeProducts = productMapper.selectList(
                new QueryWrapper<Product>()
                        .eq("status", "ON_SALE")
                        .eq("deleted", 0));

        List<FaqKnowledge> activeFaqs = faqKnowledgeMapper.selectList(
                new QueryWrapper<FaqKnowledge>()
                        .eq("status", "ACTIVE")
                        .eq("deleted", 0));

        String storeName = store.getStoreName();
        String storeAddress = store.getAddress();
        String phone = store.getPhone();
        String businessHours = store.getBusinessHours();

        String homeServiceRadius = null;
        String cancellationPolicy = null;

        if (config != null) {
            if (config.getHomeServiceRadiusKm() != null) {
                homeServiceRadius = config.getHomeServiceRadiusKm() + "km";
            }
            if (config.getBookingCancelHours() != null) {
                cancellationPolicy = "需提前" + config.getBookingCancelHours() + "小时取消";
            }
        }

        List<CustomerServiceContext.ServiceItemFact> serviceFacts = activeServices.stream()
                .map(s -> new CustomerServiceContext.ServiceItemFact(
                        s.getId(),
                        s.getName(),
                        s.getServiceMode() != null ? s.getServiceMode() : "到店",
                        s.getDescription(),
                        s.getPrice() != null ? s.getPrice().toPlainString() : "详询",
                        s.getDurationMinutes() != null ? s.getDurationMinutes().toString() : "详询",
                        s.getServiceMode() != null ? s.getServiceMode() : "到店"
                ))
                .toList();

        List<CustomerServiceContext.ProductFact> productFacts = activeProducts.stream()
                .map(p -> new CustomerServiceContext.ProductFact(
                        p.getId(),
                        p.getName(),
                        "到店自提",
                        p.getPrice() != null ? p.getPrice().toPlainString() : "详询",
                        p.getStock() != null ? p.getStock().toString() : "详询"
                ))
                .toList();

        List<CustomerServiceContext.FaqFact> faqFacts = activeFaqs.stream()
                .map(f -> new CustomerServiceContext.FaqFact(
                        f.getId(),
                        f.getQuestion(),
                        f.getAnswer(),
                        f.getCategory()
                ))
                .toList();

        boolean hasData = !serviceFacts.isEmpty() || !productFacts.isEmpty() || !faqFacts.isEmpty()
                || storeName != null;

        return new CustomerServiceContext(
                storeName, storeAddress, businessHours, phone,
                homeServiceRadius, cancellationPolicy,
                serviceFacts, productFacts, faqFacts, hasData
        );
    }

    private Store findFirstActiveStore() {
        return storeMapper.selectOne(
                new QueryWrapper<Store>()
                        .eq("status", "OPEN")
                        .eq("deleted", 0)
                        .last("LIMIT 1"));
    }
}
