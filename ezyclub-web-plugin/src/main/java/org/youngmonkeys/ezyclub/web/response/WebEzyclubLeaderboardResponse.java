package org.youngmonkeys.ezyclub.web.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class WebEzyclubLeaderboardResponse {
    private List<WebEzyclubUserResponse> topMembers;
    private WebEzyclubUserResponse me;
    private long myRank;
    private long pointsToNextLevel;
    private int maxLevel;
    private List<Long> levelPoints;
}
