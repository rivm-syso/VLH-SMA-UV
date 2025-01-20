package nl.rivm.uvsg.jserialcomm;

import com.fazecast.jSerialComm.SerialPort;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;

/*
NOTE MMo - This class was moved from `modbus-lib-benchmark`
 */
@Slf4j
public class RS485SlaveCOM1_SlaveId01 extends Thread {

    public static final int CAPACITY = 1024;

    @Override
    public void run() {
        main(null);
    }

    public static void main(String[] args) {

        SerialPort serialPort = SerialPort.getCommPort("COM1");
        serialPort.setComPortParameters(19200, 8, SerialPort.ONE_STOP_BIT, SerialPort.EVEN_PARITY);
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);


        // TODO MMo GBC:
        if (serialPort.openPort()) {

            do {
                log.info("RS485 Slave (Server) listening...");

                try {
                    InputStream input = serialPort.getInputStream();
                    byte[] buffer = new byte[CAPACITY];
                    int bytesRead;
                    int totalBytesRead = 0;
                    int nReads = 0;
                    log.info("Calling InputStream.read(...)");
                    do {
                        bytesRead = input.read(buffer);
                        totalBytesRead += bytesRead;
                        log.info("Bytes received: {}", bytesToString(bytesRead, buffer));
                        nReads++;
                    } while (bytesRead > 0 && totalBytesRead < 8);
                    log.info("InputStream.read(...)-loop returned after {} reads.", nReads);

                    byte slaveId = buffer[0];
                    if(slaveId == 1) {

                        //TODO MMo do we need some delay here?
                        //Thread.sleep(300);

                        // Send response back to the master
                        byte[] responseBuffer = getResponseBuffer();
                        log.info("Calling OutputStream.write(...)");
                        serialPort.writeBytes(responseBuffer, responseBuffer.length);
                        log.info("Bytes sent: {}", bytesToString(responseBuffer.length, responseBuffer));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            while (true);
        } else {
            log.error("Unable to open the port");
        }
    }

    private static byte[] getResponseBuffer() {
        byte[] responseBuffer = {
                (byte) 0x01,
                (byte) 0x04,
                (byte) 0x10,
                (byte) 0x00, (byte) 0x01,
                (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x04,
                (byte) 0x03, (byte) 0xE5,
                (byte) 0x03, (byte) 0xE5,
                (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0xF8,
                (byte) 0x00, (byte) 0xEA,
                (byte) 0x73, (byte) 0x22
        };
        return responseBuffer;
    }

    // TODO MMo - use toByteString() oslt
    private static String bytesToString(int nrOfBytes, byte[] buffer) {
        StringBuffer bytes = new StringBuffer(CAPACITY);

        for(int i = 0; i < nrOfBytes; i++) {
            bytes.append(String.format("%02X", buffer[i]));
        }
        return bytes.toString();
    }
}
