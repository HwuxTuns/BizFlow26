package com.example.promotion.service;

import com.example.promotion.dto.CartItemPriceRequest;
import com.example.promotion.dto.CartItemPriceResponse;
import com.example.promotion.dto.PromotionDTO;

import java.util.List;

public interface PromotionService {

    List<PromotionDTO> getAllPromotions();

    List<PromotionDTO> getActivePromotions();

    List<PromotionDTO> searchPromotions(String query, String discountType, String targetType, Long targetId, Boolean active);

    String generatePromotionCode(String name);

    PromotionDTO getPromotionByCode(String code);

    PromotionDTO getActivePromotionByCode(String code);

    PromotionDTO createPromotion(PromotionDTO dto);

    PromotionDTO updatePromotion(Long id, PromotionDTO dto);

    void deletePromotion(Long id);

    void deactivatePromotion(Long id);

    void activatePromotion(Long id);
    
    List<CartItemPriceResponse> calculateCartItemPrices(CartItemPriceRequest request);

    List<CartItemPriceResponse> calculateAndConsumeCartItemPrices(CartItemPriceRequest request);
}
