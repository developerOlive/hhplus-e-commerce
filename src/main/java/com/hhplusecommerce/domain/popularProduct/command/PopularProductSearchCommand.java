package com.hhplusecommerce.domain.popularProduct.command;

public class PopularProductSearchCommand {

    public static final int DEFAULT_LIMIT = 5;
    public static final int DEFAULT_DAYS = 7;

    private final String category;
    private final Integer limit;
    private final Integer days;

    public PopularProductSearchCommand(String category, Integer limit, Integer days) {
        this.category = (category == null || category.isBlank()) ? "all" : category;
        this.limit = (limit != null && limit > 0) ? limit : DEFAULT_LIMIT;
        this.days = (days != null && days > 0) ? days : DEFAULT_DAYS;
    }

    public String category() {
        return category;
    }

    public int limit() {
        return limit;
    }

    public int days() {
        return days;
    }
}
