package org.youngmonkeys.ezyclub.constant;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum CommunityCategory {
    BUSINESS,
    TECHNOLOGY,
    HEALTH,
    SPORTS,
    MUSIC,
    ARTS,
    LANGUAGES,
    FINANCE,
    SELF_IMPROVEMENT,
    HOBBIES,
    OTHER;

    public static CommunityCategory of(String value) {
        for (CommunityCategory category : values()) {
            if (category.toString().equalsIgnoreCase(value)) {
                return category;
            }
        }
        return null;
    }

    public static List<String> names() {
        return Arrays.stream(values())
            .map(Enum::toString)
            .collect(Collectors.toList());
    }
}
