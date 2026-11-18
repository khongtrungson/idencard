package com.idencard.protocol;

import java.io.ByteArrayOutputStream;

/**
 * Helper to construct APDU command byte arrays.
 */
public class ApduCommand {
    private final byte cla;
    private final byte ins;
    private final byte p1;
    private final byte p2;
    private final byte[] data;
    private final Integer le;

    public ApduCommand(byte cla, byte ins, byte p1, byte p2, byte[] data, Integer le) {
        this.cla = cla;
        this.ins = ins;
        this.p1 = p1;
        this.p2 = p2;
        this.data = data != null ? data.clone() : new byte[0];
        this.le = le;
    }

    public static ApduCommand selectApplet(byte[] aid) {
        return new ApduCommand(ApduConstants.CLA_ISO7816, ApduConstants.INS_SELECT, (byte) 0x04, (byte) 0x00, aid, null);
    }

    public static ApduCommand getCardInfo() {
        return new ApduCommand(ApduConstants.CLA_IDENCARD, ApduConstants.INS_GET_CARD_INFO, (byte) 0x00, (byte) 0x00, null, 256);
    }

    public static ApduCommand verifyPin(byte[] pinBytes) {
        return new ApduCommand(ApduConstants.CLA_IDENCARD, ApduConstants.INS_VERIFY_PIN, (byte) 0x00, (byte) 0x00, pinBytes, null);
    }

    public static ApduCommand getIdentity() {
        return new ApduCommand(ApduConstants.CLA_IDENCARD, ApduConstants.INS_GET_IDENTITY, (byte) 0x00, (byte) 0x00, null, 256);
    }

    public byte[] toBytes() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(cla);
        out.write(ins);
        out.write(p1);
        out.write(p2);

        if (data.length > 0) {
            out.write((byte) (data.length & 0xFF));
            out.write(data, 0, data.length);
        }

        if (le != null) {
            out.write((byte) (le & 0xFF));
        }

        return out.toByteArray();
    }

    public byte getCla() { return cla; }
    public byte getIns() { return ins; }
    public byte getP1() { return p1; }
    public byte getP2() { return p2; }
    public byte[] getData() { return data.clone(); }
    public Integer getLe() { return le; }

    @Override
    public String toString() {
        return "ApduCommand{" +
                "cla=" + String.format("0x%02X", cla) +
                ", ins=" + String.format("0x%02X", ins) +
                ", p1=" + String.format("0x%02X", p1) +
                ", p2=" + String.format("0x%02X", p2) +
                ", dataLen=" + data.length +
                '}';
    }
}
