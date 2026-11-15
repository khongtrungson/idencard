package com.idencard.protocol;

import java.util.Arrays;

/**
 * Parses and wraps raw APDU response bytes (Data + SW1 + SW2).
 */
public class ApduResponse {
    private final byte[] data;
    private final byte sw1;
    private final byte sw2;

    public ApduResponse(byte[] rawResponse) {
        if (rawResponse == null || rawResponse.length < 2) {
            throw new IllegalArgumentException("APDU response must be at least 2 bytes (SW1, SW2)");
        }
        int dataLength = rawResponse.length - 2;
        this.data = new byte[dataLength];
        System.arraycopy(rawResponse, 0, this.data, 0, dataLength);
        this.sw1 = rawResponse[rawResponse.length - 2];
        this.sw2 = rawResponse[rawResponse.length - 1];
    }

    public ApduResponse(byte[] data, byte sw1, byte sw2) {
        this.data = data != null ? data.clone() : new byte[0];
        this.sw1 = sw1;
        this.sw2 = sw2;
    }

    public boolean isSuccess() {
        return getStatusWord() == ApduConstants.SW_NO_ERROR;
    }

    public short getStatusWord() {
        return (short) (((sw1 & 0xFF) << 8) | (sw2 & 0xFF));
    }

    public byte[] getData() {
        return data.clone();
    }

    public byte getSw1() { return sw1; }
    public byte getSw2() { return sw2; }

    public String getDataAsString() {
        return new String(data, java.nio.charset.StandardCharsets.UTF_8);
    }

    @Override
    public String toString() {
        return "ApduResponse{" +
                "sw=" + String.format("0x%04X", getStatusWord()) +
                ", dataLen=" + data.length +
                ", isSuccess=" + isSuccess() +
                '}';
    }
}
