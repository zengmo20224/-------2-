package com.petcare.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcare.product.entity.CartItem;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface CartItemMapper extends BaseMapper<CartItem> {

    /**
     * Upsert: insert a new cart item, or increase quantity if the user-product pair already exists.
     * Relies on uk_user_product unique constraint to prevent duplicate rows.
     *
     * @return number of rows affected
     */
    @Insert("INSERT INTO cart_item (id, user_id, product_id, quantity, checked, create_time, update_time) " +
            "VALUES (#{id}, #{userId}, #{productId}, #{quantity}, 1, NOW(), NOW()) " +
            "ON DUPLICATE KEY UPDATE quantity = quantity + #{quantity}, update_time = NOW()")
    int upsert(@Param("id") Long id,
               @Param("userId") Long userId,
               @Param("productId") Long productId,
               @Param("quantity") int quantity);

    /**
     * Updates cart item quantity by cart item ID (for explicit quantity changes).
     *
     * @return number of rows affected
     */
    @Update("UPDATE cart_item SET quantity = #{quantity}, update_time = NOW() " +
            "WHERE id = #{id} AND user_id = #{userId}")
    int updateQuantityByIdAndUser(@Param("id") Long id,
                                  @Param("userId") Long userId,
                                  @Param("quantity") int quantity);

    /**
     * Deletes a cart item by ID and user ID (ownership check).
     *
     * @return number of rows affected
     */
    @Update("DELETE FROM cart_item WHERE id = #{id} AND user_id = #{userId}")
    int deleteByIdAndUser(@Param("id") Long id, @Param("userId") Long userId);
}
