package com.petcare.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcare.product.entity.ProductOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ProductOrderMapper extends BaseMapper<ProductOrder> {

    /**
     * Locks an order row for update (used in state transitions to prevent concurrent modifications).
     *
     * @return the locked order, or null if not found
     */
    @Select("SELECT * FROM product_order WHERE id = #{id} AND deleted = 0 FOR UPDATE")
    ProductOrder selectForUpdate(@Param("id") Long id);

    /**
     * Updates order status and related fields atomically with a status guard.
     *
     * @return number of rows affected (1 = success, 0 = status conflict)
     */
    @Update("UPDATE product_order SET status = #{newStatus}, update_time = NOW() " +
            "WHERE id = #{id} AND status = #{expectedStatus} AND deleted = 0")
    int updateStatusWithGuard(@Param("id") Long id,
                              @Param("expectedStatus") String expectedStatus,
                              @Param("newStatus") String newStatus);
}
