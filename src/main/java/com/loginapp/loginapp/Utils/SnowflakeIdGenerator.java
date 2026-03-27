package com.loginapp.loginapp.Utils;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import java.io.Serializable;

public class SnowflakeIdGenerator implements IdentifierGenerator {

    private static final long EPOCH = 1609459200000L; // Jan 1, 2021
    private static final long MACHINE_ID = 1L; // for multiple servers, change this ID
    private static long sequence = 0L;
    private static long lastTimestamp = -1L;

    @Override
    public synchronized Serializable generate(SharedSessionContractImplementor session, Object object) {
        long timestamp = System.currentTimeMillis();

        if (timestamp < lastTimestamp) {
            throw new RuntimeException("Clock moved backwards. Refusing to generate ID.");
        }

        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & 0xFFF; // 12-bit sequence
            if (sequence == 0) {
                while (timestamp <= lastTimestamp) {
                    timestamp = System.currentTimeMillis();
                }
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        return ((timestamp - EPOCH) << 22) | (MACHINE_ID << 12) | sequence;
    }
}