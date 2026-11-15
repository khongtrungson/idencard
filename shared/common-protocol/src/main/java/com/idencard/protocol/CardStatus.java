package com.idencard.protocol;

/**
 * High-level status of the smart card connection and session.
 */
public enum CardStatus {
    DISCONNECTED,
    CONNECTED,
    SELECTED,
    AUTHENTICATED,
    LOCKED
}
