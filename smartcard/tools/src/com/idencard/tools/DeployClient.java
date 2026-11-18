package com.idencard.tools;

import java.io.File;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Properties;

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.smartcardio.TerminalFactory;

import com.oracle.javacard.ams.AMService;
import com.oracle.javacard.ams.AMServiceFactory;
import com.oracle.javacard.ams.AMSession;
import com.oracle.javacard.ams.config.AID;
import com.oracle.javacard.ams.config.CAPFile;

public class DeployClient {

    private static final String ISD_AID = "aid:A000000151000000";
    private static final String PKG_AID = "aid:F00000000101";
    private static final String APPLET_CLASS_AID = "aid:F00000000101020304";
    private static final String APPLET_INSTANCE_AID = "aid:F00000000101020304";

    public static void main(String[] args) {
        String capPath = args.length > 0 ? args[0] : "smartcard/deliverables/com/idencard/applet/javacard/applet.cap";
        String propsPath = args.length > 1 ? args[1] : "smartcard/client.config.properties";
        String host = args.length > 2 ? args[2] : "localhost";
        int port = args.length > 3 ? Integer.parseInt(args[3]) : 9025;

        System.out.println("=== IdenCard Applet Deployer ===");
        System.out.println("CAP: " + capPath);
        System.out.println("Connecting to simulator at " + host + ":" + port + "...");

        try {
            File capFile = new File(capPath);
            if (!capFile.exists()) {
                System.err.println("Error: CAP file not found at " + capPath);
                System.exit(1);
            }

            Properties props = new Properties();
            if (propsPath != null && !propsPath.isEmpty()) {
                File propFile = new File(propsPath);
                if (propFile.exists()) {
                    try (FileInputStream fis = new FileInputStream(propFile)) {
                        props.load(fis);
                    }
                }
            }
            if (props.isEmpty()) {
                // Default Oracle JavaCard Simulator SCP03 test keys
                props.setProperty("A000000151000000_scp03enc_01", "404142434445464748494A4B4C4D4E4F");
                props.setProperty("A000000151000000_scp03mac_01", "404142434445464748494A4B4C4D4E4F");
                props.setProperty("A000000151000000_scp03dek_01", "404142434445464748494A4B4C4D4E4F");
            }

            TerminalFactory tf = TerminalFactory.getInstance(
                    "SocketCardTerminalFactoryType",
                    List.of(new InetSocketAddress(host, port)),
                    "SocketCardTerminalProvider");
            CardTerminal terminal = tf.terminals().list().get(0);

            if (!terminal.waitForCardPresent(5000)) {
                System.err.println("Error: No response from Java Card simulator on " + host + ":" + port);
                System.exit(1);
            }

            Card card = terminal.connect("*");
            System.out.println("Simulator connected. ATR: " + bytesToHex(card.getATR().getBytes()));
            CardChannel channel = card.getBasicChannel();

            AMService ams = AMServiceFactory.getInstance("GP2.2");
            ams.setProperties(props);

            CAPFile cap = CAPFile.from(capPath);

            System.out.println("Deploying CAP to simulator...");
            try {
                AMSession undeploy = ams.openSession(ISD_AID)
                        .uninstall(APPLET_INSTANCE_AID)
                        .unload(PKG_AID)
                        .close();
                undeploy.run(channel);
                System.out.println("Previous applet instance uninstalled.");
            } catch (Exception ignored) {
                // Not previously installed
            }

            AMSession deploy = ams.openSession(ISD_AID)
                    .load(PKG_AID, cap.getBytes())
                    .install(PKG_AID, APPLET_CLASS_AID, APPLET_INSTANCE_AID)
                    .close();
            deploy.run(channel);
            System.out.println("Applet deployed successfully!");

            // Test selecting applet
            CommandAPDU selectApdu = new CommandAPDU(0x00, 0xA4, 0x04, 0x00, AID.from(APPLET_INSTANCE_AID).toBytes(), 256);
            System.out.println("Selecting Applet AID " + APPLET_INSTANCE_AID + "...");
            ResponseAPDU resp = channel.transmit(selectApdu);
            System.out.printf("Select Response: SW=%04X\n", resp.getSW());

            if (resp.getSW() == 0x9000) {
                System.out.println(">>> Applet selected successfully (SW=9000)! Applet is running on simulator port " + port);

                // Test GET_CARD_INFO: CLA=80, INS=10, P1=00, P2=00, Le=256
                CommandAPDU getInfoApdu = new CommandAPDU(0x80, 0x10, 0x00, 0x00, 256);
                ResponseAPDU infoResp = channel.transmit(getInfoApdu);
                System.out.printf("GET_CARD_INFO: SW=%04X, Data=%s\n", infoResp.getSW(), new String(infoResp.getData()));
            } else {
                System.err.println("Warning: Select returned non-9000 status: " + Integer.toHexString(resp.getSW()));
            }

            card.disconnect(true);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }
}
