The files in this directory somewhat overlap similar files in other AppVet packages
but are different in that they are geared toward installation of the AppVet and
do not instantiate certain classes that are required for running the AppVet
(e.g., Logger class must be instantiated when running the AppVet but not when 
installing AppVet).

There are two ways to run the AppVet Installer:

* From Eclipse: Right click on /src/gov/nist/appvet/installer/AppVetIntaller and 
  select Run As->Java Application.
  
* As Runnable JAR file: Right click on /src/gov/nist/appvet/installer/AppVetIntaller
  and select Export...->Java->Runnable JAR File and select the file name and
  directory to save the AppVet Installer JAR file (e.g., C:\appvetinstaller.jar). 
  Next, copy $AppVet/appvet_installer_files to the directory that you saved 
  the AppVet Installer JAR file (e.g., C:\appvet_install_files). Then from a console,
  cd into the directory that you installed the JAR file and run the JAR
  file. For example, execute "java -jar appvetinstaller.jar".

