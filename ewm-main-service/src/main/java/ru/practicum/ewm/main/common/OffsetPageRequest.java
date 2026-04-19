package ru.practicum.ewm.main.common;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public record OffsetPageRequest(int offset, int limit, Sort sort) implements Pageable {
    public OffsetPageRequest {
        if (offset < 0) {
            throw new IllegalArgumentException("Offset must not be less than zero");
        }
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be greater than zero");
        }
        sort = sort == null ? Sort.unsorted() : sort;
    }

    public OffsetPageRequest(int offset, int limit) {
        this(offset, limit, Sort.unsorted());
    }

    @Override
    public int getPageNumber() {
        return offset / limit;
    }

    @Override
    public int getPageSize() {
        return limit;
    }

    @Override
    public long getOffset() {
        return offset;
    }

    @Override
    public Sort getSort() {
        return sort;
    }

    @Override
    public Pageable next() {
        return new OffsetPageRequest(offset + limit, limit, sort);
    }

    @Override
    public Pageable previousOrFirst() {
        int previousOffset = Math.max(offset - limit, 0);
        return new OffsetPageRequest(previousOffset, limit, sort);
    }

    @Override
    public Pageable first() {
        return new OffsetPageRequest(0, limit, sort);
    }

    @Override
    public Pageable withPage(int pageNumber) {
        return new OffsetPageRequest(pageNumber * limit, limit, sort);
    }

    @Override
    public boolean hasPrevious() {
        return offset > 0;
    }
}
