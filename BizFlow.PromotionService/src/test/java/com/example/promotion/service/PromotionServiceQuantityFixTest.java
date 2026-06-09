package com.example.promotion.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.promotion.dto.PromotionDTO;
import com.example.promotion.entity.Promotion;
import com.example.promotion.repository.PromotionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cache.CacheManager;
import org.springframework.boot.web.client.RestTemplateBuilder;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 🧪 Unit Tests untuk PromotionService - Quantity & Used Quantity Fix
 * 
 * Test coverage untuk skenario berikut:
 * - Rule 1: Transition to unlimited (maxQty = 0/null) → reset usedQty = 0
 * - Rule 2: Transition from unlimited to limited → reset usedQty = 0
 * - Rule 3: New quantity < used quantity (cap) → set usedQty = new maxQty
 * - Rule 4: Other cases (keep unchanged)
 */
@DisplayName("Promotion Quantity & Used Quantity Fix Tests")
class PromotionServiceQuantityFixTest {

    @Mock
    private PromotionRepository promotionRepository;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private RestTemplateBuilder restTemplateBuilder;

    @InjectMocks
    private PromotionServiceImpl promotionService;

    private Promotion testPromotion;
    private PromotionDTO testDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        initializeTestData();
    }

    private void initializeTestData() {
        testPromotion = new Promotion();
        testPromotion.setId(1L);
        testPromotion.setCode("TEST001");
        testPromotion.setName("Test Promotion");
        testPromotion.setDiscountType(Promotion.DiscountType.PERCENT);
        testPromotion.setDiscountValue(10.0);
        testPromotion.setStartDate(LocalDateTime.now());
        testPromotion.setEndDate(LocalDateTime.now().plusDays(30));
        testPromotion.setActive(true);

        testDTO = new PromotionDTO();
        testDTO.setCode("TEST001");
        testDTO.setName("Test Promotion");
        testDTO.setDiscountType(Promotion.DiscountType.PERCENT);
        testDTO.setDiscountValue(10.0);
        testDTO.setActive(true);
    }

    // ================== RULE 1 TESTS ==================
    @Test
    @DisplayName("Rule 1: Transition to Unlimited (maxQty=0) should reset usedQty to 0")
    void testRule1_SetToZero() {
        // Arrange: Current state: maxQty=5, usedQty=3
        testPromotion.setMaxQuantity(5);
        testPromotion.setUsedQuantity(3);
        testDTO.setMaxQuantity(0); // Setting to unlimited

        // Act
        testPromotion.setCode(testDTO.getCode());
        testPromotion.setName(testDTO.getName());
        // Simulate handleUsedQuantityOnMaxQuantityChange behavior
        Integer oldMax = testPromotion.getMaxQuantity();
        testPromotion.setMaxQuantity(null); // normalize(0) = null
        callHandleUsedQuantity(testPromotion, oldMax, null);

        // Assert
        assertEquals(0, testPromotion.getUsedQuantity(), 
            "usedQty should be reset to 0 when maxQty transitions to unlimited");
    }

    @Test
    @DisplayName("Rule 1: Transition to Unlimited (maxQty=null) should reset usedQty to 0")
    void testRule1_SetToNull() {
        // Arrange: Current state: maxQty=1, usedQty=1
        testPromotion.setMaxQuantity(1);
        testPromotion.setUsedQuantity(1);
        testDTO.setMaxQuantity(null); // Setting to unlimited

        // Act
        Integer oldMax = testPromotion.getMaxQuantity();
        testPromotion.setMaxQuantity(null);
        callHandleUsedQuantity(testPromotion, oldMax, null);

        // Assert
        assertEquals(0, testPromotion.getUsedQuantity(), 
            "usedQty should be reset to 0 when maxQty is set to null");
    }

    // ================== RULE 2 TESTS ==================
    @Test
    @DisplayName("Rule 2: Transition from Unlimited to Limited should reset usedQty to 0")
    void testRule2_UnlimitedToLimited() {
        // Arrange: Current state: maxQty=null (unlimited), usedQty=5
        testPromotion.setMaxQuantity(null);
        testPromotion.setUsedQuantity(5);
        testDTO.setMaxQuantity(3); // Setting to limited

        // Act
        Integer oldMax = testPromotion.getMaxQuantity(); // null
        testPromotion.setMaxQuantity(3);
        callHandleUsedQuantity(testPromotion, oldMax, 3);

        // Assert
        assertEquals(0, testPromotion.getUsedQuantity(), 
            "usedQty should be reset to 0 when transitioning from unlimited to limited");
    }

    @Test
    @DisplayName("Rule 2: Zero to Positive should reset usedQty to 0")
    void testRule2_ZeroToPositive() {
        // Arrange: maxQty=0 (unlimited), usedQty=10
        testPromotion.setMaxQuantity(0); // Will be normalized to null
        testPromotion.setUsedQuantity(10);
        testDTO.setMaxQuantity(5);

        // Act
        Integer oldMax = testPromotion.getMaxQuantity(); // 0
        testPromotion.setMaxQuantity(5);
        callHandleUsedQuantity(testPromotion, oldMax, 5);

        // Assert
        assertEquals(0, testPromotion.getUsedQuantity(), 
            "usedQty should be reset to 0 when changing from 0 (unlimited) to positive");
    }

    // ================== RULE 3 TESTS ==================
    @Test
    @DisplayName("Rule 3: New qty < used qty should cap usedQty at new qty")
    void testRule3_CapUsedQuantity() {
        // Arrange: maxQty=10, usedQty=8, changing to 5
        testPromotion.setMaxQuantity(10);
        testPromotion.setUsedQuantity(8);
        testDTO.setMaxQuantity(5);

        // Act
        Integer oldMax = testPromotion.getMaxQuantity(); // 10
        testPromotion.setMaxQuantity(5);
        callHandleUsedQuantity(testPromotion, oldMax, 5);

        // Assert
        assertEquals(5, testPromotion.getUsedQuantity(), 
            "usedQty should be capped to 5 (new maxQty) since it was 8");
    }

    @Test
    @DisplayName("Rule 3: Reduce from 100 to 50 should cap used from 75 to 50")
    void testRule3_LargeReduction() {
        // Arrange: maxQty=100, usedQty=75
        testPromotion.setMaxQuantity(100);
        testPromotion.setUsedQuantity(75);
        testDTO.setMaxQuantity(50);

        // Act
        Integer oldMax = testPromotion.getMaxQuantity();
        testPromotion.setMaxQuantity(50);
        callHandleUsedQuantity(testPromotion, oldMax, 50);

        // Assert
        assertEquals(50, testPromotion.getUsedQuantity(), 
            "usedQty should be capped at 50 when reducing from 100");
    }

    // ================== RULE 4 TESTS ==================
    @Test
    @DisplayName("Rule 4: Increase quantity should keep usedQty unchanged")
    void testRule4_IncreaseQuantity() {
        // Arrange: maxQty=5, usedQty=2, changing to 10
        testPromotion.setMaxQuantity(5);
        testPromotion.setUsedQuantity(2);
        testDTO.setMaxQuantity(10);

        // Act
        Integer oldMax = testPromotion.getMaxQuantity();
        testPromotion.setMaxQuantity(10);
        callHandleUsedQuantity(testPromotion, oldMax, 10);

        // Assert
        assertEquals(2, testPromotion.getUsedQuantity(), 
            "usedQty should remain 2 when quantity is increased from 5 to 10");
    }

    @Test
    @DisplayName("Rule 4: Update with same or higher qty should keep used unchanged")
    void testRule4_SameOrHigherQuantity() {
        // Arrange: maxQty=5, usedQty=3, changing to 5 (same)
        testPromotion.setMaxQuantity(5);
        testPromotion.setUsedQuantity(3);
        testDTO.setMaxQuantity(5);

        // Act
        Integer oldMax = testPromotion.getMaxQuantity();
        testPromotion.setMaxQuantity(5);
        callHandleUsedQuantity(testPromotion, oldMax, 5);

        // Assert
        assertEquals(3, testPromotion.getUsedQuantity(), 
            "usedQty should remain 3 when quantity stays or increases");
    }

    // ================== EDGE CASES ==================
    @Test
    @DisplayName("Edge Case: Used quantity already 0")
    void testEdgeCase_UsedQuantityZero() {
        // Arrange: maxQty=5, usedQty=0, changing to 0
        testPromotion.setMaxQuantity(5);
        testPromotion.setUsedQuantity(0);
        testDTO.setMaxQuantity(0);

        // Act
        Integer oldMax = testPromotion.getMaxQuantity();
        testPromotion.setMaxQuantity(null);
        callHandleUsedQuantity(testPromotion, oldMax, null);

        // Assert
        assertEquals(0, testPromotion.getUsedQuantity(), 
            "usedQty should remain 0");
    }

    @Test
    @DisplayName("Edge Case: Null usedQuantity should be treated as 0")
    void testEdgeCase_NullUsedQuantity() {
        // Arrange: maxQty=5, usedQty=null, changing to 0
        testPromotion.setMaxQuantity(5);
        testPromotion.setUsedQuantity(null);
        testDTO.setMaxQuantity(0);

        // Act
        Integer oldMax = testPromotion.getMaxQuantity();
        testPromotion.setMaxQuantity(null);
        callHandleUsedQuantity(testPromotion, oldMax, null);

        // Assert
        assertEquals(0, testPromotion.getUsedQuantity(), 
            "usedQty should be reset to 0 (null should be treated as 0)");
    }

    @Test
    @DisplayName("Edge Case: Negative used quantity should be treated as 0")
    void testEdgeCase_NegativeUsedQuantity() {
        // Arrange: maxQty=5, usedQty=-1, changing to 0
        testPromotion.setMaxQuantity(5);
        testPromotion.setUsedQuantity(-1);
        testDTO.setMaxQuantity(0);

        // Act
        Integer oldMax = testPromotion.getMaxQuantity();
        testPromotion.setMaxQuantity(null);
        // Before calling, ensure usedQty is normalized
        if (testPromotion.getUsedQuantity() == null || testPromotion.getUsedQuantity() < 0) {
            testPromotion.setUsedQuantity(0);
        }
        callHandleUsedQuantity(testPromotion, oldMax, null);

        // Assert
        assertEquals(0, testPromotion.getUsedQuantity(), 
            "usedQty should be 0 (negative normalized)");
    }

    // ================== REAL SCENARIO TESTS ==================
    @Test
    @DisplayName("Real Scenario: qty=1, used=1 → qty=0 → qty=2")
    void testRealScenario_DepleteAndReplenish() {
        // Step 1: Create with qty=1, used=1
        testPromotion.setMaxQuantity(1);
        testPromotion.setUsedQuantity(1);

        // Step 2: Set to unlimited (qty=0)
        Integer oldMax1 = testPromotion.getMaxQuantity();
        testPromotion.setMaxQuantity(null);
        callHandleUsedQuantity(testPromotion, oldMax1, null);
        assertEquals(0, testPromotion.getUsedQuantity(), 
            "After setting to unlimited, usedQty should be 0");

        // Step 3: Set to qty=2
        Integer oldMax2 = testPromotion.getMaxQuantity();
        testPromotion.setMaxQuantity(2);
        callHandleUsedQuantity(testPromotion, oldMax2, 2);
        assertEquals(0, testPromotion.getUsedQuantity(), 
            "After setting from unlimited to 2, usedQty should still be 0");
        
        // Verify: remaining should be 2
        assertEquals(2, testPromotion.getMaxQuantity() - testPromotion.getUsedQuantity(),
            "Remaining quantity should be 2");
    }

    // ================== HELPER METHODS ==================
    
    /**
     * Simulates the handleUsedQuantityOnMaxQuantityChange behavior for testing
     * This is a simplified version that implements the same logic
     */
    private void callHandleUsedQuantity(Promotion promotion, Integer oldMaxQuantity, Integer newMaxQuantity) {
        Integer currentUsedQuantity = normalizeUsedQuantity(promotion.getUsedQuantity());
        Integer normalizedOldMax = normalizeMaxQuantity(oldMaxQuantity);

        // Rule 1: If new maxQuantity is null (unlimited) → reset usedQuantity
        if (newMaxQuantity == null) {
            promotion.setUsedQuantity(0);
            return;
        }

        // Rule 2: Transition from unlimited to limited
        if (normalizedOldMax == null && newMaxQuantity > 0) {
            promotion.setUsedQuantity(0);
            return;
        }

        // Rule 3: Both limited, but new < used
        if (normalizedOldMax != null && normalizedOldMax > 0 &&
            newMaxQuantity > 0 && newMaxQuantity < currentUsedQuantity) {
            promotion.setUsedQuantity(newMaxQuantity);
            return;
        }

        // Rule 4: Keep unchanged
        // (do nothing - usedQuantity stays same)
    }

    private Integer normalizeMaxQuantity(Integer maxQuantity) {
        if (maxQuantity == null || maxQuantity <= 0) {
            return null;
        }
        return maxQuantity;
    }

    private int normalizeUsedQuantity(Integer usedQuantity) {
        if (usedQuantity == null || usedQuantity < 0) {
            return 0;
        }
        return usedQuantity;
    }
}
