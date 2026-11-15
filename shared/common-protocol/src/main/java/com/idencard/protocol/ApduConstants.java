package com.idencard.protocol;

/**
 * Shared APDU Command and Status Word constants for IdenCard Applet & Clients.
 */
public final class ApduConstants {

    private ApduConstants() {}

    // Application Identifier (AID): 5 to 16 bytes
    // RID: F000000001, PIX: 01020304
    public static final byte[] IDENCARD_AID = new byte[] {
            (byte) 0xF0, 0x00, 0x00, 0x00, 0x01, 0x01, 0x02, 0x03, 0x04
    };

    // Class of Instructions (CLA)
    public static final byte CLA_ISO7816 = (byte) 0x00;
    public static final byte CLA_IDENCARD = (byte) 0x80;

    // Standard ISO 7816 Instructions
    public static final byte INS_SELECT = (byte) 0xA4;

    // Proprietary IdenCard Instructions (INS)
    public static final byte INS_GET_CARD_INFO = (byte) 0x10;
    public static final byte INS_VERIFY_PIN = (byte) 0x20;
    public static final byte INS_GET_IDENTITY = (byte) 0x30;
    public static final byte INS_UPDATE_PIN = (byte) 0x40;

    // Status Words (SW)
    public static final short SW_NO_ERROR = (short) 0x9000;
    public static final short SW_PIN_VERIFICATION_FAILED = (short) 0x6300;
    public static final short SW_SECURITY_STATUS_NOT_SATISFIED = (short) 0x6982;
    public static final short SW_AUTH_METHOD_BLOCKED = (short) 0x6983;
    public static final short SW_CONDITIONS_NOT_SATISFIED = (short) 0x6985;
    public static final short SW_WRONG_LENGTH = (short) 0x6700;
    public static final short SW_UNKNOWN_INS = (short) 0x6D00;
    public static final short SW_UNKNOWN_CLA = (short) 0x6E00;
    public static final short SW_WRONG_P1P2 = (short) 0x6A86;
}
