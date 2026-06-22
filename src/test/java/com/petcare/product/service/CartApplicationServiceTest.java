package com.petcare.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.petcare.common.exception.BusinessException;
import com.petcare.common.exception.ErrorCode;
import com.petcare.product.dto.CartItemCheckRequest;
import com.petcare.product.dto.CartItemCreateRequest;
import com.petcare.product.dto.CartItemResponse;
import com.petcare.product.dto.CartItemUpdateRequest;
import com.petcare.product.entity.CartItem;
import com.petcare.product.entity.Product;
import com.petcare.product.mapper.CartItemMapper;
import com.petcare.product.mapper.ProductMapper;
import com.petcare.product.service.impl.CartApplicationServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link CartApplicationServiceImpl}.
 * Uses Mockito mocks — no Spring context.
 *
 * Uses doReturn().when() stubbing to avoid ambiguous method resolution
 * caused by MyBatis-Plus BaseMapper overloaded methods.
 */
@ExtendWith(MockitoExtension.class)
class CartApplicationServiceTest {

    /**
     * Initializes MyBatis-Plus table info for CartItem so that
     * LambdaUpdateWrapper can resolve lambda references like CartItem::getUserId.
     */
    @BeforeAll
    static void initMybatisPlusTableInfo() {
        if (TableInfoHelper.getTableInfo(CartItem.class) == null) {
            MybatisConfiguration configuration = new MybatisConfiguration();
            MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "");
            assistant.setCurrentNamespace(CartItemMapper.class.getName());
            TableInfoHelper.initTableInfo(assistant, CartItem.class);
        }
    }

    @Mock
    private CartItemMapper cartItemMapper;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private CartApplicationServiceImpl cartService;

    private static final Long USER_ID = 1001L;
    private static final Long PRODUCT_ID = 2001L;
    private static final Long CART_ITEM_ID = 3001L;

    private Product defaultProduct;
    private CartItem defaultCartItem;

    @BeforeEach
    void setUp() {
        defaultProduct = new Product();
        defaultProduct.setId(PRODUCT_ID);
        defaultProduct.setName("猫粮");
        defaultProduct.setCoverUrl("https://example.com/cat-food.jpg");
        defaultProduct.setPrice(new BigDecimal("99.00"));
        defaultProduct.setStock(50);
        defaultProduct.setSalesCount(10);
        defaultProduct.setStatus("ON_SALE");
        defaultProduct.setPickupOnly(1);
        defaultProduct.setDeleted(0);

        defaultCartItem = new CartItem();
        defaultCartItem.setId(CART_ITEM_ID);
        defaultCartItem.setUserId(USER_ID);
        defaultCartItem.setProductId(PRODUCT_ID);
        defaultCartItem.setQuantity(2);
        defaultCartItem.setChecked(1);
        defaultCartItem.setCreateTime(LocalDateTime.of(2026, 6, 1, 10, 0));
        defaultCartItem.setUpdateTime(LocalDateTime.of(2026, 6, 1, 10, 0));
    }

    // ==================== addCartItem ====================

    @Nested
    @DisplayName("addCartItem")
    class AddCartItemTests {

        @Test
        @DisplayName("Adding a product that exists and is ON_SALE should call upsert and return response")
        void addCartItem_success() {
            // Arrange
            CartItemCreateRequest request = new CartItemCreateRequest(PRODUCT_ID, 2);
            doReturn(defaultProduct).when(productMapper).selectById(PRODUCT_ID);
            doReturn(1).when(cartItemMapper).upsert(anyLong(), eq(USER_ID), eq(PRODUCT_ID), eq(2));
            doReturn(defaultCartItem).when(cartItemMapper).selectOne(any());

            // Act
            CartItemResponse result = cartService.addCartItem(USER_ID, request);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(CART_ITEM_ID);
            assertThat(result.productId()).isEqualTo(PRODUCT_ID);
            assertThat(result.productName()).isEqualTo("猫粮");
            assertThat(result.productPrice()).isEqualByComparingTo(new BigDecimal("99.00"));
            assertThat(result.quantity()).isEqualTo(2);
            assertThat(result.checked()).isTrue();
            assertThat(result.subtotal()).isEqualByComparingTo(new BigDecimal("198.00"));

            verify(cartItemMapper).upsert(anyLong(), eq(USER_ID), eq(PRODUCT_ID), eq(2));
        }

        @Test
        @DisplayName("Adding a product that doesn't exist should throw PRODUCT_NOT_ON_SALE")
        void addCartItem_productNotFound() {
            // Arrange
            CartItemCreateRequest request = new CartItemCreateRequest(9999L, 1);
            doReturn(null).when(productMapper).selectById(9999L);

            // Act & Assert
            assertThatThrownBy(() -> cartService.addCartItem(USER_ID, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ErrorCode.PRODUCT_NOT_ON_SALE);

            verify(cartItemMapper, never()).upsert(any(), anyLong(), anyLong(), anyInt());
        }

        @Test
        @DisplayName("Adding a product that is OFF_SALE should throw PRODUCT_NOT_ON_SALE")
        void addCartItem_productOffSale() {
            // Arrange
            CartItemCreateRequest request = new CartItemCreateRequest(PRODUCT_ID, 1);
            defaultProduct.setStatus("OFF_SALE");
            doReturn(defaultProduct).when(productMapper).selectById(PRODUCT_ID);

            // Act & Assert
            assertThatThrownBy(() -> cartService.addCartItem(USER_ID, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ErrorCode.PRODUCT_NOT_ON_SALE);

            verify(cartItemMapper, never()).upsert(any(), anyLong(), anyLong(), anyInt());
        }
    }

    // ==================== updateCartItem ====================

    @Nested
    @DisplayName("updateCartItem")
    class UpdateCartItemTests {

        @Test
        @DisplayName("Updating cart item with correct ownership should succeed")
        void updateCartItem_success() {
            // Arrange
            CartItemUpdateRequest request = new CartItemUpdateRequest(5);

            // The service calls selectById twice: once for ownership check, once for re-read
            // Return defaultCartItem first, then updatedItem on subsequent calls
            CartItem updatedItem = new CartItem();
            updatedItem.setId(CART_ITEM_ID);
            updatedItem.setUserId(USER_ID);
            updatedItem.setProductId(PRODUCT_ID);
            updatedItem.setQuantity(5);
            updatedItem.setChecked(1);

            doReturn(defaultCartItem, updatedItem).when(cartItemMapper).selectById(CART_ITEM_ID);
            doReturn(1).when(cartItemMapper).updateQuantityByIdAndUser(CART_ITEM_ID, USER_ID, 5);
            doReturn(defaultProduct).when(productMapper).selectById(PRODUCT_ID);

            // Act
            CartItemResponse result = cartService.updateCartItem(USER_ID, CART_ITEM_ID, request);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.quantity()).isEqualTo(5);
            assertThat(result.subtotal()).isEqualByComparingTo(new BigDecimal("495.00"));

            verify(cartItemMapper).updateQuantityByIdAndUser(CART_ITEM_ID, USER_ID, 5);
        }

        @Test
        @DisplayName("Updating cart item with wrong ownership should throw CART_ITEM_FORBIDDEN")
        void updateCartItem_wrongOwnership() {
            // Arrange
            Long otherUserId = 9999L;
            CartItemUpdateRequest request = new CartItemUpdateRequest(5);
            doReturn(defaultCartItem).when(cartItemMapper).selectById(CART_ITEM_ID);

            // Act & Assert
            assertThatThrownBy(() -> cartService.updateCartItem(otherUserId, CART_ITEM_ID, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ErrorCode.CART_ITEM_FORBIDDEN);

            verify(cartItemMapper, never()).updateQuantityByIdAndUser(anyLong(), anyLong(), anyInt());
        }

        @Test
        @DisplayName("Updating non-existent cart item should throw CART_ITEM_FORBIDDEN")
        void updateCartItem_notFound() {
            // Arrange
            CartItemUpdateRequest request = new CartItemUpdateRequest(5);
            doReturn(null).when(cartItemMapper).selectById(CART_ITEM_ID);

            // Act & Assert
            assertThatThrownBy(() -> cartService.updateCartItem(USER_ID, CART_ITEM_ID, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ErrorCode.CART_ITEM_FORBIDDEN);

            verify(cartItemMapper, never()).updateQuantityByIdAndUser(anyLong(), anyLong(), anyInt());
        }
    }

    // ==================== deleteCartItem ====================

    @Nested
    @DisplayName("deleteCartItem")
    class DeleteCartItemTests {

        @Test
        @DisplayName("Deleting a cart item that exists should succeed")
        void deleteCartItem_success() {
            // Arrange
            doReturn(1).when(cartItemMapper).deleteByIdAndUser(CART_ITEM_ID, USER_ID);

            // Act
            cartService.deleteCartItem(USER_ID, CART_ITEM_ID);

            // Assert
            verify(cartItemMapper).deleteByIdAndUser(CART_ITEM_ID, USER_ID);
        }

        @Test
        @DisplayName("Deleting a cart item that doesn't exist should throw CART_ITEM_NOT_FOUND")
        void deleteCartItem_notFound() {
            // Arrange
            doReturn(0).when(cartItemMapper).deleteByIdAndUser(CART_ITEM_ID, USER_ID);

            // Act & Assert
            assertThatThrownBy(() -> cartService.deleteCartItem(USER_ID, CART_ITEM_ID))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ErrorCode.CART_ITEM_NOT_FOUND);
        }
    }

    // ==================== listCartItems ====================

    @Nested
    @DisplayName("listCartItems")
    class ListCartItemsTests {

        @Test
        @DisplayName("Listing cart items should return all items with product info")
        void listCartItems_returnsAllItemsWithProductInfo() {
            // Arrange
            CartItem item1 = new CartItem();
            item1.setId(CART_ITEM_ID);
            item1.setUserId(USER_ID);
            item1.setProductId(PRODUCT_ID);
            item1.setQuantity(2);
            item1.setChecked(1);
            item1.setUpdateTime(LocalDateTime.of(2026, 6, 2, 10, 0));

            Long productId2 = 2002L;
            Product product2 = new Product();
            product2.setId(productId2);
            product2.setName("狗粮");
            product2.setCoverUrl("https://example.com/dog-food.jpg");
            product2.setPrice(new BigDecimal("79.00"));
            product2.setStock(30);
            product2.setStatus("ON_SALE");
            product2.setDeleted(0);

            CartItem item2 = new CartItem();
            item2.setId(3002L);
            item2.setUserId(USER_ID);
            item2.setProductId(productId2);
            item2.setQuantity(3);
            item2.setChecked(0);
            item2.setUpdateTime(LocalDateTime.of(2026, 6, 1, 10, 0));

            doReturn(List.of(item1, item2)).when(cartItemMapper).selectList(any());
            doReturn(defaultProduct).when(productMapper).selectById(PRODUCT_ID);
            doReturn(product2).when(productMapper).selectById(productId2);

            // Act
            List<CartItemResponse> result = cartService.listCartItems(USER_ID);

            // Assert
            assertThat(result).hasSize(2);

            CartItemResponse first = result.get(0);
            assertThat(first.id()).isEqualTo(CART_ITEM_ID);
            assertThat(first.productName()).isEqualTo("猫粮");
            assertThat(first.productPrice()).isEqualByComparingTo(new BigDecimal("99.00"));
            assertThat(first.quantity()).isEqualTo(2);
            assertThat(first.checked()).isTrue();
            assertThat(first.subtotal()).isEqualByComparingTo(new BigDecimal("198.00"));

            CartItemResponse second = result.get(1);
            assertThat(second.id()).isEqualTo(3002L);
            assertThat(second.productName()).isEqualTo("狗粮");
            assertThat(second.productPrice()).isEqualByComparingTo(new BigDecimal("79.00"));
            assertThat(second.quantity()).isEqualTo(3);
            assertThat(second.checked()).isFalse();
            assertThat(second.subtotal()).isEqualByComparingTo(new BigDecimal("237.00"));

            verify(cartItemMapper).selectList(any());
        }

        @Test
        @DisplayName("Listing cart items when cart is empty should return empty list")
        void listCartItems_emptyCart() {
            // Arrange
            doReturn(List.of()).when(cartItemMapper).selectList(any());

            // Act
            List<CartItemResponse> result = cartService.listCartItems(USER_ID);

            // Assert
            assertThat(result).isEmpty();
        }
    }

    // ==================== checkCartItems ====================

    @Nested
    @DisplayName("checkCartItems")
    class CheckCartItemsTests {

        @Test
        @DisplayName("Checking cart items should update checked status and return updated list")
        void checkCartItems_success() {
            // Arrange
            CartItemCheckRequest request = new CartItemCheckRequest(List.of(CART_ITEM_ID), true);
            doReturn(1).when(cartItemMapper).update((CartItem) any(), any());

            CartItem checkedItem = new CartItem();
            checkedItem.setId(CART_ITEM_ID);
            checkedItem.setUserId(USER_ID);
            checkedItem.setProductId(PRODUCT_ID);
            checkedItem.setQuantity(2);
            checkedItem.setChecked(1);
            checkedItem.setUpdateTime(LocalDateTime.of(2026, 6, 1, 10, 0));

            doReturn(List.of(checkedItem)).when(cartItemMapper).selectList(any());
            doReturn(defaultProduct).when(productMapper).selectById(PRODUCT_ID);

            // Act
            List<CartItemResponse> result = cartService.checkCartItems(USER_ID, request);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).checked()).isTrue();

            verify(cartItemMapper).update((CartItem) any(), any());
        }

        @Test
        @DisplayName("Unchecking cart items should set checked to false")
        void checkCartItems_uncheck() {
            // Arrange
            CartItemCheckRequest request = new CartItemCheckRequest(List.of(CART_ITEM_ID), false);
            doReturn(1).when(cartItemMapper).update((CartItem) any(), any());

            CartItem uncheckedItem = new CartItem();
            uncheckedItem.setId(CART_ITEM_ID);
            uncheckedItem.setUserId(USER_ID);
            uncheckedItem.setProductId(PRODUCT_ID);
            uncheckedItem.setQuantity(2);
            uncheckedItem.setChecked(0);
            uncheckedItem.setUpdateTime(LocalDateTime.of(2026, 6, 1, 10, 0));

            doReturn(List.of(uncheckedItem)).when(cartItemMapper).selectList(any());
            doReturn(defaultProduct).when(productMapper).selectById(PRODUCT_ID);

            // Act
            List<CartItemResponse> result = cartService.checkCartItems(USER_ID, request);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).checked()).isFalse();
        }
    }
}
