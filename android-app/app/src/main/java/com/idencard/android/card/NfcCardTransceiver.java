package com.idencard.android.card;

import android.nfc.tech.IsoDep;

import java.io.IOException;

/**
 * Communicates with a physical JavaCard contactless smart card using NFC (IsoDep).
 */
public class NfcCardTransceiver implements CardTransceiver {

    private final IsoDep isoDep;

    public NfcCardTransceiver(IsoDep isoDep) {
        this.isoDep = isoDep;
    }

    @Override
    public void connect() throws IOException {
        if (isoDep != null && !isoDep.isConnected()) {
            isoDep.connect();
            isoDep.setTimeout(5000);
        }
    }

    @Override
    public byte[] transmit(byte[] commandApdu) throws IOException {
        if (isoDep == null || !isoDep.isConnected()) {
            throw new IOException("NFC IsoDep tag is not connected");
        }
        return isoDep.transceive(commandApdu);
    }

    @Override
    public void disconnect() {
        if (isoDep != null && isoDep.isConnected()) {
            try {
                isoDep.close();
            } catch (IOException ignored) {}
        }
    }

    @Override
    public boolean isConnected() {
        return isoDep != null && isoDep.isConnected();
    }

    @Override
    public String getTransceiverName() {
        return "NFC Contactless (IsoDep)";
    }
}
