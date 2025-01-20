package nl.rivm.uvsg.jserialcomm;

import com.fazecast.jSerialComm.SerialPort;
import com.ghgande.j2mod.modbus.util.ModbusUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;

/*
NOTE MMo - This class was moved from `modbus-lib-benchmark`
TODO MMo - Only keep this emulator, rename it and remove all others
 */
@Slf4j
public class RS485SlaveCOM1_SlaveId01WithCrcCalculation extends Thread {

    public static final int CAPACITY = 1024;

    private static short sensorValueByte0 = 0;
    private static short sensorValueByte1 = -1;

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
                        byte[] sensorDataRequest = {0x01, 0x04, 0x00, 0x02, 0x00, 0x08};
                        byte[] deviceInfoRequest = {0x01, 0x04, 0x00, 0x00, 0x00, 0x2B};
                        if (compare(sensorDataRequest, buffer)) {
                            if (sensorValueByte1 < (short)0xFF) {
                                sensorValueByte1++;
                            } else {
                                sensorValueByte0++;
                                sensorValueByte1 = 0;
                            }
                            byte[] responseBytes = getResponseBytes((byte) sensorValueByte0, (byte) sensorValueByte1);
                            log.info("Calling OutputStream.write(...[{},{}]...)", sensorValueByte0, sensorValueByte1);
                            serialPort.writeBytes(responseBytes, responseBytes.length);
                            log.info("Bytes sent: {}", bytesToString(responseBytes.length, responseBytes));

                        } else if (compare(deviceInfoRequest, buffer)) {
                            byte[] responseBytes = getResponseBytes1();
                            log.info("Calling OutputStream.write(...[{},{}]...)", sensorValueByte0, sensorValueByte1);
                            serialPort.writeBytes(responseBytes, responseBytes.length);
                            log.info("Bytes sent: {}", bytesToString(responseBytes.length, responseBytes));
                        }
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

    private static boolean compare(byte[] expected, byte[] actual) {
        for (int i = 0; i < expected.length - 2; i++) {
            if (expected[i] != actual[i]) {
                return false;
            }
        }
        return true;
    }

    private static byte[] getResponseBytes1() {

        byte[] responseBytes = {
                (byte) 0x01,
                (byte) 0x04,
                (byte) 0x56,
                (byte) 0x02, (byte) 0x71,
                (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x19,
                (byte) 0x00, (byte) 0x63
        };
        // TODO MMo - Move one level up
        return addCrc(responseBytes);
    }

    private static byte[] getResponseBytes(byte sensorValueByte0, byte sensorValueByte1) {

        byte[] responseBytes = {
                (byte) 0x01,
                (byte) 0x04,
                (byte) 0x10,
                (byte) 0x00, (byte) 0x01,
                (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00,
                sensorValueByte0, sensorValueByte1,
                (byte) 0x03, (byte) 0xE5,
                (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0xF8,
                (byte) 0x00, (byte) 0xEA
        };
        // TODO MMo - Move one level up
        return addCrc(responseBytes);
    }

    private static byte[] addCrc(byte[] responseBytes) {
        int length = responseBytes.length;
        byte[] responseBytesWithCrc = new byte[length + 2];
        System.arraycopy(responseBytes, 0, responseBytesWithCrc, 0, length);
        int[] crc = ModbusUtil.calculateCRC(responseBytes, 0, length);
        responseBytesWithCrc[length] = (byte) crc[0];
        responseBytesWithCrc[length + 1] = (byte) crc[1];
        return responseBytesWithCrc;
    }

    // TODO MMo - use toByteString() oslt
    private static String bytesToString(int nrOfBytes, byte[] buffer) {
        StringBuffer bytes = new StringBuffer(CAPACITY);

        for(int i = 0; i < nrOfBytes; i++) {
            bytes.append(String.format("%02X", buffer[i]));
        }
        return bytes.toString();
    }

    private static byte[] unsignedShortToBytes(int value) {

        if (value < 0 || value > Short.MAX_VALUE) {
            throw new IllegalArgumentException("Value out of range [0.." + Short.MAX_VALUE + "]: " + value);
        }
        byte[] bytes = new byte[2];
        bytes[0] = (byte) (value >> 24);
        bytes[1] = (byte) (value >> 16);
        return bytes;
    }
}
