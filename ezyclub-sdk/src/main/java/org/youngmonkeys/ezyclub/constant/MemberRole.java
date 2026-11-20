package org.youngmonkeys.ezyclub.constant;

public enum MemberRole {
    OWNER,
    ADMIN,
    MEMBER;

    public boolean isManager() {
        return this == OWNER || this == ADMIN;
    }

    public static MemberRole of(String value) {
        for (MemberRole role : values()) {
            if (role.toString().equals(value)) {
                return role;
            }
        }
        return MEMBER;
    }
}
