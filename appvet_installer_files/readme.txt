This directory contains files required for the AppVet installer. There are
two ways to run AppVet Installer:

* From Eclipse: Right click on /src/gov/nist/appvet/installer/AppVetIntaller and 
  select Run As->Java Application.
  
* As Runnable JAR file: Right click on /src/gov/nist/appvet/installer/AppVetIntaller
  and select Export...->Java->Runnable JAR File and select the file name and
  directory to save the AppVet Installer JAR file (e.g., C:\appvetinstaller.jar). 
  Next, copy $AppVet/appvet_installer_files to the directory that you saved 
  the AppVet Installer JAR file (e.g., C:\appvet_install_files). Then from a console,
  cd into the directory that you installed the JAR file and run the JAR
  file. For example, execute "java -jar appvetinstaller.jar".
  