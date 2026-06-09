package com.petcare.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcare.product.entity.ProductOrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ProductOrderItemMapper extends BaseMapper<ProductOrderItem> {

    /**
     * Finds all order items for a given order ID.
     */
    @Select("SELECT * FROM product_order_item WHERE order_id = #{orderId}")
    List<ProductOrderItem> selectByOrderId(@Param("orderId") Long orderId);

    /**
     * Finds all order items for multiple order IDs (batch query).
     */
    @Select("<script>" +
            "SELECT * FROM product_order_item WHERE order_id IN " +
            "<foreach item='id' collection='orderIds' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    List<ProductOrderItem> selectByOrderIds(@Param("orderIds") List<Long> orderIds);
}
