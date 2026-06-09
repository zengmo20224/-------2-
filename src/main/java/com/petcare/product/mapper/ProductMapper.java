package com.petcare.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcare.product.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    /**
     * Atomically deducts stock for a product.
     * Only succeeds if stock >= quantity and product is on sale and not deleted.
     *
     * @return number of rows affected (1 = success, 0 = insufficient stock or product unavailable)
     */
    @Update("UPDATE product SET stock = stock - #{quantity}, update_time = NOW() " +
            "WHERE id = #{productId} AND stock >= #{quantity} " +
            "AND status = 'ON_SALE' AND deleted = 0")
    int deductStock(@Param("productId") Long productId, @Param("quantity") int quantity);

    /**
     * Atomically restores stock for a product (used on order cancellation).
     *
     * @return number of rows affected
     */
    @Update("UPDATE product SET stock = stock + #{quantity}, update_time = NOW() " +
            "WHERE id = #{productId} AND deleted = 0")
    int restoreStock(@Param("productId") Long productId, @Param("quantity") int quantity);

    /**
     * Atomically increases sales count for a product (used on order completion).
     *
     * @return number of rows affected
     */
    @Update("UPDATE product SET sales_count = sales_count + #{quantity}, update_time = NOW() " +
            "WHERE id = #{productId} AND deleted = 0")
    int increaseSalesCount(@Param("productId") Long productId, @Param("quantity") int quantity);

    /**
     * Gets current stock for a product, for validation purposes.
     */
    @Select("SELECT stock FROM product WHERE id = #{productId} AND deleted = 0")
    Integer selectStock(@Param("productId") Long productId);
}
