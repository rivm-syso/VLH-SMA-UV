package nl.rivm.uvsg.sensor.metadata;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum KippEnZonen_SMP3A {
    /*
     Manufacturer: OTT HydroMet > Kipp & Zonen
     Source:
         Modbus® Communication Manual [Smart_Pyranometer_-_Communications_Manual_V01-0324.pdf]
            Document Number: 0426210 | 01-0324
            Find the extended product manuals for all our Smart Pyranometers on OTTHydromet.com.
            Copyright © 2024 OTT HydroMet. All rights reserved.
     Protocol L7: Modbus
     Protocol L1: RS485
   */
//    MODBUS() {
//        int baudRate = 19200;
//    }
   /*
   ---------------------------------------------------------------------------------------
   >>> Device Type, Data Model Version, Real-time Processed Data, Batch- and Serial-Number
   ---------------------------------------------------------------------------------------
         Input Registers:
             HeaderRow:
                PDU ADDRESS
                PARAMETER
                R/W
                TYPE
                MODE
                DESCRIPTION
             Rows:
                    0   IO_DEVICE_TYPE        R   U16 All "Device type of the sensor"
                    1   IO_DATA_MODEL_VERSION R   U16 All "Version of the object data model"
                    2   IO_OPERATIONAL_MODE   R   U16 All "Operational mode: normal, service, calibration, and factory"
                    3   IO_STATUS_FLAGS       R   U16 All "Device Status flags";
                    4   IO_SCALE_FACTOR       R   S16 All Scale factor for sensor data (determines number of decimal places)"
                    5   IO_SENSOR1_DATA       R   S16 N,S Temperature compensated radiation in W/m2 (Net radiation for SGR)"
                    8   IO_BODY_TEMPERATURE   R   S16 N,S Body temperature in 0.1 °C"
                    9   IO_EXT_POWER_SENSOR   R   S16 N,S External power voltage"
                    41  IO_BATCH_NUMBER       R   U16 All Production batch number = year in YY
                    42  IO_SERIAL_NUMBER      R   U16 All Serial number
    */
    IO_DEVICE_TYPE(0, "Device type of the sensor"),
    IO_DATA_MODEL_VERSION(1, "Version of the object data model"),
    IO_OPERATIONAL_MODE(2, "Operational mode: normal, service, calibration, and factory"),
    IO_STATUS_FLAGS(3, "Device Status flags"),
    IO_SCALE_FACTOR(4, "Scale factor for sensor data (determines number of decimal places"),
    IO_SENSOR1_DATA(5, "Temperature compensated radiation in W/m2 (Net radiation for SGR"),
    IO_BODY_TEMPERATURE(8, "Body temperature in 0.1 °C"),
    IO_EXT_POWER_SENSOR(9, "External power voltage"),
    IO_BATCH_NUMBER(41, "Production batch number = year in YY"),
    IO_SERIAL_NUMBER(42, "Serial number");

    public final int address;
    public final String description;
}
