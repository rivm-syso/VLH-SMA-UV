**TODO**
- replace booleans for mocking with proper integration testing

**Notes**
- Various Slaves were copied from the project `modbus-lib-benchmark`
- The master/slave combinations used for testing are described in `modbus-lib-benchmark/Readme`

**Testing**

- For testing associate two paths with a drive letter, as being the local and remote drive to store the data, use windows commands:
  - `subst D: C:\_WORKSPACES\uv-sensor-gateway-0.1\data\_RIVM-UVSG-DATA-LocalDrive`
  - `subst N: C:\_WORKSPACES\uv-sensor-gateway-0.1\data\_RIVM-UVSG-DATA-NetworkDrive`


- For testing Network-drive unavailability, use windows command: `subst N: /d`

 
- Modbus4jRs485MasterApplicationUVSG can run in the following test modes:

  - **Two sensors connected:**
      - two sensors (slaveId 1 and 6) connected via RS485-to-USB converter to serial port COM3:
      - mockSensorExplorer=false
      - mockRadiation=false
      - mockRadiation=true for mocking a series of radiation values and checking the avg and std calculation
      - run Modbus4jRs485MasterApplicationUVSG

  - **Two sensors simulated:**

    - HHD Virtual Serial Port Tools (or other VSP tool)
     
      - Create Bridge port COM3 to COM4
      - Create Split port COM4 to 5 and 6
      
    - Start test slave RS485Slave05_SlaveId06 (listening on COM5), see also modbus-lib-benchmark / Readme.md
    - Start test slave RS485Slave06_SlaveId01 (listening on COM6), see also modbus-lib-benchmark / Readme.md
    - mockSensorExplorer=true
    - mockRadiation=false
    - mockRadiation=true for mocking a series of radiation values and checking the avg and std calculation
    - run Modbus4jRs485MasterApplicationUVSG
