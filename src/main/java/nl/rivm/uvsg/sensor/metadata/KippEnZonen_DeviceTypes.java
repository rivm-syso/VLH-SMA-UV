package nl.rivm.uvsg.sensor.metadata;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum KippEnZonen_DeviceTypes {

    SMP3A(602, "SMP3A"),
    SUVE(629, "SUVE"),
    SUVA(625, "SUVA"),
    // TODO MMo - currently used in the unit test only
    UNKNOWN(999, "DeviceTypeUnknown"),
    // TODO MMo - is DEFAULT needed here?
    // TODO MMo - use 'Type{}' or remove it
    DEFAULT(null, "Type{}");

    public final Integer value;
    public final String name;
}
