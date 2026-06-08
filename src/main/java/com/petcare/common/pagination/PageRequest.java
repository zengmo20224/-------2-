package com.petcare.common.pagination;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * Generic pagination request parameters.
 * Controllers should accept this as a query parameter object.
 */
public class PageRequest {

    @Min(value = 1, message = "页码不能小于 1")
    private int page = 1;

    @Min(value = 1, message = "每页数量不能小于 1")
    @Max(value = 100, message = "每页数量不能超过 100")
    private int size = 20;

    public PageRequest() {
    }

    public PageRequest(int page, int size) {
        this.page = page;
        this.size = size;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    /**
     * Converts to MyBatis-Plus page offset (zero-based).
     */
    public long getOffset() {
        return (long) (page - 1) * size;
    }
}
