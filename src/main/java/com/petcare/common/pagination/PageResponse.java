package com.petcare.common.pagination;

import java.util.List;

/**
 * Generic pagination response wrapper.
 * Contains the data list and pagination metadata.
 *
 * @param <T> type of items in the page
 */
public class PageResponse<T> {

    private final List<T> items;
    private final long total;
    private final int page;
    private final int size;
    private final int totalPages;

    public PageResponse(List<T> items, long total, int page, int size) {
        this.items = items;
        this.total = total;
        this.page = page;
        this.size = size;
        this.totalPages = (int) Math.ceil((double) total / size);
    }

    public static <T> PageResponse<T> of(List<T> items, long total, int page, int size) {
        return new PageResponse<>(items, total, page, size);
    }

    public static <T> PageResponse<T> empty(int page, int size) {
        return new PageResponse<>(List.of(), 0, page, size);
    }

    public List<T> getItems() {
        return items;
    }

    public long getTotal() {
        return total;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public int getTotalPages() {
        return totalPages;
    }
}
