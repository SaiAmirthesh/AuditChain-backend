package com.SaiAmirthesh.AuditChain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminAnalyticsDTO {
    private long totalCount;
    private double totalVolume;
    private double avgTxValue;
    private List<StatusCount> statusDistribution;
    private List<ChannelCount> channelUsage;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StatusCount {
        private String status;
        private long count;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChannelCount {
        private String channel;
        private long count;
    }
}
