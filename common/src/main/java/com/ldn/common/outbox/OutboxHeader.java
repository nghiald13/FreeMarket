package com.ldn.common.outbox;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class OutboxHeader {
    public static final String OUTBOX_ID = "x-outbox-id";
    public static final String AGGREGATE_TYPE = "x-aggregate-type";
    public static final String AGGREGATE_ID = "x-aggregate-id";
}
