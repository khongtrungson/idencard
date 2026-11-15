package com.idencard.android.domain;

public class CardInfo {
    private final String serialNumber;
    private final int pinTriesRemaining;
    private final boolean pinValidated;

    public CardInfo(String serialNumber, int pinTriesRemaining, boolean pinValidated) {
        this.serialNumber = serialNumber;
        this.pinTriesRemaining = pinTriesRemaining;
        this.pinValidated = pinValidated;
    }

    public String getSerialNumber() { return serialNumber; }
    public int getPinTriesRemaining() { return pinTriesRemaining; }
    public boolean isPinValidated() { return pinValidated; }

    @Override
    public String toString() {
        return "CardInfo{" +
                "serialNumber='" + serialNumber + '\'' +
                ", pinTriesRemaining=" + pinTriesRemaining +
                ", pinValidated=" + pinValidated +
                '}';
    }
}
