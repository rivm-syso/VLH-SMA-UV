...The UV Sensor Gateway connects to the UV Sensor's Modbus/RS485 COM port as a Modbus master and reads data from the connected sensors. The data is written to data files as configured in the file `application.yml`. 



If you only need to upgrade the application go to ...

For building the application GitHub's build server / ...action features .... might be used. Below however we describe the steps for building the application on a local machine using `Git` and `Maven`, which can be downloaded from:

- https://git-scm.com/downloads/win (`apache-maven-3.9.9-bin.zip` or a more recent version)
- https://maven.apache.org/download.cgi (`Git-2.47.1.2-64-bit.exe` or a more recent version)





## Build the application locally

..If you just want to install the latest version of the application go to...

- **Create or Update a Local Copy of the Codebase**

  - The codebase of the UV Sensor Gateway  is stored on GitHub in the repository: https://github.com/rivm-syso/VLH-SMA-UV

  - When you do not already have a local copy of the repository got to your project directory and enter the command:

     `git clone https://github.com/rivm-syso/VLH-SMA-UV.git`  

  - Once you've cloned the repository and you want to download the latest changes from the remote, use the command:

    `git pull`

- **Build the Application**

  - To compile and build the application and package it into an executable java jar file, use the Maven command:

    `mvn package`

  - If command `mvn package` resulted in `[INFO] BUILD SUCCESS` , you'll find the following java jar file in the subdirectory **target**:
  
    `uv-sensor-gateway-<version>.jar` 
  
- **Install UV Sensor Gateway Service**

  To install the UV Sensor Gateway as a Windows service with NSSM, which is a lightweight tool for managing Windows services:

  - install the service manager **NSSM**

    - find NSSM in the following zip file in the subdirectory **tools**: ` nssm-2.24.zip`
    - unzip it in C:\Program Files\

    - Extend the System Environment Variable **Path**
      - Win-key / env / click on `Edit the system environment variables` (**not** on `Edit environment variables for your account`)
      - Cick on `Environment Variables`
      - In the `System variables` pane select `Path` and click `New`
      - Paste the value `C:\Program Files\nssm-2.24\win64`
      - Click `Ok` twice

  - install the service **uv-sensor-gateway**

    - Windows / Power Shell - As Administrator
    - nssm install uv-sensor-gateway
    - Application
      - Path: C:\ ... \java.exe
      - Startup directory: C:\ ... Radiometers\
      - Arguments: -jar C:\_DBD_Tests\windows-service-tests\uv-sensor-gateway-0.0.5-SNAPSHOT.jar
    - Details
      - Display name: UV Sensor Gateway
      - Description: Connects to the UV Sensor's Modbus/RS485 COM port as a Modbus master and reads data from the connected sensors as configured in application.yml (...zhb)
      - Startup type: Automatic
    - Exit Actions
      - Restart: Restart application
      - Delay restart by: 5000 ms
    - I/O
      - Output (stdout): C: ... \logs\uv-sensor-gateway-stdout.log
      - Error (stderr): C: ... \logs\uv-sensor-gateway-stderr.log
  
    

## **Download and Install UV Sensor Gateway**

...To...

- Go to the GitHub directory containing the latest version of the executable java jar file:

​	https://github.com/rivm-syso/VLH-SMA-UV/tree/main/target-delivery

- Click on `uv-sensor-gateway-<version>.jar` 

- Click on the download icon at the top right

- Save it to your working directory

- ...start NSSM and update the version in the field ....



## Logging

- ...log directory and files
- ...log level(s)
- ...errorlog should be empty
- ...live viewing of the logs (>>?)
