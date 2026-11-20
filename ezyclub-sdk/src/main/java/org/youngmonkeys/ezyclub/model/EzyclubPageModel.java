package org.youngmonkeys.ezyclub.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public class EzyclubPageModel<T> {
    private final List<T> items;
    private final int page;
    private final int limit;
    private final boolean hasMore;

    public static <T> EzyclubPageModel<T> of(
        List<T> fetchedItems,
        int page,
        int limit
    ) {
        boolean hasMore = fetchedItems.size() > limit;
        List<T> items = hasMore
            ? fetchedItems.subList(0, limit)
            : fetchedItems;
        return new EzyclubPageModel<>(items, page, limit, hasMore);
    }

    public <R> EzyclubPageModel<R> map(Function<T, R> mapper) {
        return new EzyclubPageModel<>(
            items.stream().map(mapper).collect(Collectors.toList()),
            page,
            limit,
            hasMore
        );
    }
}
