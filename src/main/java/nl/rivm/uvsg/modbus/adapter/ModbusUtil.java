package nl.rivm.uvsg.modbus.adapter;

import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public final class ModbusUtil {

    public static int bytePairToInt(byte[] bytes) {
        byte[] intBytes = new byte[4];
        intBytes[2] = bytes[0];
        intBytes[3] = bytes[1];
        return ByteBuffer.wrap(intBytes).getInt();
    }

    public static int bytePairToShort(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getShort();
    }

    //TODO MMo - If not used, move it to some JavaBox (check comparable code in DataFileWriter)
    public static String formatAsFLoat(int value, int nrOfFractionalDigits) {
        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.setMaximumFractionDigits(nrOfFractionalDigits);
        decimalFormat.setMinimumFractionDigits(nrOfFractionalDigits);
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
        decimalFormatSymbols.setDecimalSeparator('.');
        decimalFormat.setDecimalFormatSymbols(decimalFormatSymbols);

        double valueScaled = value / Math.pow(10, nrOfFractionalDigits);
        return decimalFormat.format(valueScaled);
    }
}
