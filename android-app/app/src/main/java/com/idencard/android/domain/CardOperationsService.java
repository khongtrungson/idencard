package com.idencard.android.domain;

import com.idencard.android.card.CardTransceiver;
import com.idencard.protocol.ApduCommand;
import com.idencard.protocol.ApduConstants;
import com.idencard.protocol.ApduResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class CardOperationsService {

    private final CardTransceiver transceiver;

    public CardOperationsService(CardTransceiver transceiver) {
        this.transceiver = transceiver;
    }

    public ApduResponse selectApplet() throws IOException {
        ApduCommand cmd = ApduCommand.selectApplet(ApduConstants.IDENCARD_AID);
        byte[] resp = transceiver.transmit(cmd.toBytes());
        return new ApduResponse(resp);
    }

    public CardInfo readCardInfo() throws IOException {
        ApduCommand cmd = ApduCommand.getCardInfo();
        byte[] resp = transceiver.transmit(cmd.toBytes());
        ApduResponse response = new ApduResponse(resp);

        if (!response.isSuccess()) {
            throw new IOException("Failed to read card info, SW: " + String.format("0x%04X", response.getStatusWord()));
        }

        byte[] data = response.getData();
        if (data.length < 2) {
            throw new IOException("Card info response is too short");
        }

        int triesRemaining = data[0] & 0xFF;
        boolean pinValidated = data[1] == 1;
        String serialNumber = "";
        if (data.length > 2) {
            serialNumber = new String(data, 2, data.length - 2, StandardCharsets.UTF_8);
        }

        return new CardInfo(serialNumber, triesRemaining, pinValidated);
    }

    public boolean verifyPin(String pin) throws IOException {
        byte[] pinBytes = pin.getBytes(StandardCharsets.US_ASCII);
        ApduCommand cmd = ApduCommand.verifyPin(pinBytes);
        byte[] resp = transceiver.transmit(cmd.toBytes());
        ApduResponse response = new ApduResponse(resp);
        return response.isSuccess();
    }

    public String getIdentity() throws IOException {
        ApduCommand cmd = ApduCommand.getIdentity();
        byte[] resp = transceiver.transmit(cmd.toBytes());
        ApduResponse response = new ApduResponse(resp);

        if (!response.isSuccess()) {
            throw new IOException("Failed to get identity. Status: " + String.format("0x%04X", response.getStatusWord()));
        }

        return response.getDataAsString();
    }
}
