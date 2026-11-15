package com.idencard.applet;

import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.OwnerPIN;
import javacard.framework.Util;

/**
 * IdenCard JavaCard Applet.
 * Compliant with JavaCard 3.0.4 / 3.0.5 specifications.
 *
 * AID: F00000000101020304
 */
public class IdenCardApplet extends Applet {

    // Proprietary Instructions
    public static final byte CLA_IDENCARD = (byte) 0x80;
    public static final byte INS_GET_CARD_INFO = (byte) 0x10;
    public static final byte INS_VERIFY_PIN = (byte) 0x20;
    public static final byte INS_GET_IDENTITY = (byte) 0x30;
    public static final byte INS_UPDATE_PIN = (byte) 0x40;

    // Status Words
    public static final short SW_PIN_VERIFICATION_FAILED = (short) 0x6300;
    public static final short SW_SECURITY_STATUS_NOT_SATISFIED = (short) 0x6982;
    public static final short SW_AUTH_METHOD_BLOCKED = (short) 0x6983;

    // PIN Configuration
    private static final byte PIN_MAX_TRIES = (byte) 3;
    private static final byte PIN_MAX_SIZE = (byte) 8;
    private static final byte[] DEFAULT_PIN = new byte[] { (byte) 0x31, (byte) 0x32, (byte) 0x33, (byte) 0x34 }; // "1234"

    // Card Info Data (Serial: IC-2026-0001)
    private static final byte[] CARD_SERIAL = new byte[] {
            'I', 'C', '-', '2', '0', '2', '6', '-', '0', '0', '0', '1'
    };

    // Identity payload: "IDENCARD:CARDHOLDER:ACTIVE"
    private static final byte[] CARD_IDENTITY = new byte[] {
            'I', 'D', 'E', 'N', 'C', 'A', 'R', 'D', ':', 'C', 'A', 'R', 'D', 'H', 'O', 'L', 'D', 'E', 'R', ':', 'A', 'C', 'T', 'I', 'V', 'E'
    };

    private final OwnerPIN pin;

    protected IdenCardApplet() {
        pin = new OwnerPIN(PIN_MAX_TRIES, PIN_MAX_SIZE);
        pin.update(DEFAULT_PIN, (short) 0, (byte) DEFAULT_PIN.length);
        register();
    }

    public static void install(byte[] bArray, short bOffset, byte bLength) {
        new IdenCardApplet();
    }

    @Override
    public boolean select() {
        // Reset PIN validation on card selection
        pin.reset();
        return true;
    }

    @Override
    public void deselect() {
        pin.reset();
    }

    @Override
    public void process(APDU apdu) {
        byte[] buffer = apdu.getBuffer();

        // Check for SELECT command
        if (selectingApplet()) {
            return;
        }

        // Verify Class byte
        if (buffer[ISO7816.OFFSET_CLA] != CLA_IDENCARD) {
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
        }

        byte ins = buffer[ISO7816.OFFSET_INS];
        switch (ins) {
            case INS_GET_CARD_INFO:
                handleGetCardInfo(apdu);
                break;

            case INS_VERIFY_PIN:
                handleVerifyPin(apdu);
                break;

            case INS_GET_IDENTITY:
                handleGetIdentity(apdu);
                break;

            case INS_UPDATE_PIN:
                handleUpdatePin(apdu);
                break;

            default:
                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }
    }

    private void handleGetCardInfo(APDU apdu) {
        byte[] buffer = apdu.getBuffer();
        short le = apdu.setOutgoing();

        // Format: [1 byte pin tries left] [1 byte is_validated (0/1)] [N bytes Serial]
        buffer[0] = pin.getTriesRemaining();
        buffer[1] = (byte) (pin.isValidated() ? 1 : 0);
        Util.arrayCopy(CARD_SERIAL, (short) 0, buffer, (short) 2, (short) CARD_SERIAL.length);

        short totalLen = (short) (2 + CARD_SERIAL.length);
        apdu.setOutgoingLength(totalLen);
        apdu.sendBytes((short) 0, totalLen);
    }

    private void handleVerifyPin(APDU apdu) {
        byte[] buffer = apdu.getBuffer();
        byte byteRead = (byte) apdu.setIncomingAndReceive();

        if (pin.getTriesRemaining() == 0) {
            ISOException.throwIt(SW_AUTH_METHOD_BLOCKED);
        }

        if (!pin.check(buffer, ISO7816.OFFSET_CDATA, byteRead)) {
            short triesLeft = (short) pin.getTriesRemaining();
            ISOException.throwIt((short) (SW_PIN_VERIFICATION_FAILED | triesLeft));
        }
    }

    private void handleGetIdentity(APDU apdu) {
        if (!pin.isValidated()) {
            ISOException.throwIt(SW_SECURITY_STATUS_NOT_SATISFIED);
        }

        byte[] buffer = apdu.getBuffer();
        apdu.setOutgoing();
        apdu.setOutgoingLength((short) CARD_IDENTITY.length);
        Util.arrayCopy(CARD_IDENTITY, (short) 0, buffer, (short) 0, (short) CARD_IDENTITY.length);
        apdu.sendBytes((short) 0, (short) CARD_IDENTITY.length);
    }

    private void handleUpdatePin(APDU apdu) {
        if (!pin.isValidated()) {
            ISOException.throwIt(SW_SECURITY_STATUS_NOT_SATISFIED);
        }

        byte[] buffer = apdu.getBuffer();
        byte byteRead = (byte) apdu.setIncomingAndReceive();

        if (byteRead < 4 || byteRead > PIN_MAX_SIZE) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }

        pin.update(buffer, ISO7816.OFFSET_CDATA, byteRead);
    }
}
