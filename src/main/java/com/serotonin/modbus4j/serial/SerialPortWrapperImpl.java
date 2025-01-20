package com.serotonin.modbus4j.serial;

import com.fazecast.jSerialComm.SerialPort;

import java.io.InputStream;
import java.io.OutputStream;

public class SerialPortWrapperImpl implements SerialPortWrapper {

    private final SerialPort serialPort;

    public SerialPortWrapperImpl(String portName, int baudRate, int dataBits, int stopBits, int parity) {
        serialPort = SerialPort.getCommPort(portName);
        serialPort.setComPortParameters(baudRate, dataBits, stopBits, parity);
        serialPort.setComPortTimeouts(
                SerialPort.TIMEOUT_READ_SEMI_BLOCKING,
                1000,
                1000);
    }

    @Override
    public void close() throws Exception {
        serialPort.closePort();
    }

    @Override
    public InputStream getInputStream() {
        return serialPort.getInputStream();
    }

    @Override
    public OutputStream getOutputStream() {
        return serialPort.getOutputStream();
    }

    @Override
    public int getBaudRate() {
        return serialPort.getBaudRate();
    }

    @Override
    public int getDataBits() {
        return serialPort.getNumDataBits();
    }

    @Override
    public int getStopBits() {
        return serialPort.getNumStopBits();
    }

    @Override
    public int getParity() {
        return serialPort.getParity();
    }

    @Override
    public void open() {
        if (!serialPort.openPort()) {
            throw new RuntimeException("Failed to open the serial port.");
        }
    }
}
