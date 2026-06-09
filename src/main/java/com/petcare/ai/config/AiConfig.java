package com.petcare.ai.config;

import com.petcare.ai.analytics.*;
import com.petcare.ai.domain.CustomerServiceContextBuilder;
import com.petcare.ai.provider.AiProviderClient;
import com.petcare.ai.provider.DisabledAiProviderClient;
import com.petcare.booking.mapper.ServiceBookingMapper;
import com.petcare.community.mapper.PostCommentMapper;
import com.petcare.community.mapper.PostMapper;
import com.petcare.community.mapper.PostReportMapper;
import com.petcare.marketing.mapper.MarketingActivityMapper;
import com.petcare.product.mapper.ProductMapper;
import com.petcare.product.mapper.ProductOrderMapper;
import com.petcare.service.mapper.ServiceItemMapper;
import com.petcare.ai.mapper.FaqKnowledgeMapper;
import com.petcare.store.mapper.StoreConfigMapper;
import com.petcare.store.mapper.StoreMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration for AI module beans.
 * Wires up the Provider client, context builder, and analytics aggregators.
 */
@Configuration
@EnableConfigurationProperties(com.petcare.common.config.DeepSeekProperties.class)
public class AiConfig {

    /**
     * Provider client bean. Default is disabled — no fake AI responses.
     * When provider-enabled is true and all required config is present,
     * a real adapter bean will replace this in phase 8B.
     */
    @Bean
    public AiProviderClient aiProviderClient() {
        return new DisabledAiProviderClient();
    }

    /**
     * Context builder for AI customer service.
     * Only reads approved data sources: store, store_config, services, products, FAQ.
     */
    @Bean
    public CustomerServiceContextBuilder customerServiceContextBuilder(
            StoreMapper storeMapper,
            StoreConfigMapper storeConfigMapper,
            ServiceItemMapper serviceItemMapper,
            ProductMapper productMapper,
            FaqKnowledgeMapper faqKnowledgeMapper) {
        return new CustomerServiceContextBuilder(
                storeMapper, storeConfigMapper, serviceItemMapper,
                productMapper, faqKnowledgeMapper);
    }

    @Bean
    public BusinessAnalyticsAggregator businessAnalyticsAggregator(ServiceBookingMapper serviceBookingMapper) {
        return new BusinessAnalyticsAggregator(serviceBookingMapper);
    }

    @Bean
    public CommunityAnalyticsAggregator communityAnalyticsAggregator(
            PostMapper postMapper,
            PostCommentMapper postCommentMapper,
            PostReportMapper postReportMapper) {
        return new CommunityAnalyticsAggregator(postMapper, postCommentMapper, postReportMapper);
    }

    @Bean
    public SalesAnalyticsAggregator salesAnalyticsAggregator(ProductOrderMapper productOrderMapper) {
        return new SalesAnalyticsAggregator(productOrderMapper);
    }

    @Bean
    public ActivityAnalyticsAggregator activityAnalyticsAggregator(
            MarketingActivityMapper marketingActivityMapper) {
        return new ActivityAnalyticsAggregator(marketingActivityMapper);
    }
}
