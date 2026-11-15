package com.idencard.android.card;

import java.io.IOException;

/**
 * Seam for communicating with JavaCard smartcard:
 * can be implemented over TCP Socket (Simulator) or NFC IsoDep (Physical Card).
 */
public interface CardTransceiver {

    void connect() throws IOException;

    byte[] transmit(byte[] commandApdu) throws IOException;

    void disconnect();

    boolean isConnected();

    String getTransceiverName();
}
