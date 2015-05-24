# sme2015lab3

## Corrective Maintenance
Correct the following four bugs. Document all necessary steps to correct these bugs.

**Bug report 1 (Martin done)**

Last development steps caused a severe bug which prevents the application from starting. (See the JBoss logs for further details.)

**Bug report 2 (Martin done)**

The customer complained about following error, when creating new documents and there are currently no patients created and a new patient should be recorded a wrong hint-message is shown "Es konnten keine Patienten mit der gegebenen ID gefunden werden!", as there is no search run for any patients the message should be: "Es wurde noch kein Patient für diese Dokumentation angelegt".

**Bug report 3 (Martin done)**

The search for users (menu item "Administration") is defective. With the provided test data a search for "mus" leads to no results. Instead the user "Dr. Muster" should be found. Correct this defect and make sure that the fileds "Name" and "Anzeigename" are searched case-insensitive.

**Bug report 4 (Martin)**

While running some tests a strange error occured: when documenting wound healing ("Wundheilung") with a single documentation sheet (menu item "Blätter definieren") the upwards arrow is visible the arrow is not visible in the "WHAT Studie". Find the cause of this error and correct it. (Hint: it does not have to be a programming error.)

## Adaptive Maintenance 
**Replace the currently used database with a MySQL or Postgres database. Document all necessary steps. (Martin)** 

**Additional task for groups of three**

At the moment SPICS is currently running only on a JBoss server. In meetings with the customer the customer complaint about that fact and stated that he wanted us to support mulitple application servers. Concretely they want us to support Glassfish. Evaluate the porting to Glassfish and make a cost estimation. (Bonus points will be awarded for providing an implementation.)

Document all necessary steps and changes you have made.

**Preventive Maintenance**
With the current build system all dependencies have to be managed manually. This could lead to problems when upgrading new versions. The standard for dependency management in java is the freely available Apache Maven project.

Use maven to manage the dependencies and change the ant-build-script so that it uses the libs from the maven repository. The build system should not be changed. (Hint: you could use the ant-maven-plugin. As spics.jsf.jar is available in the official maven repositories you have to add it to your local repository.)

**Perfectioning Maintenance**
At the moment the data export is implemented in a very inefficient way which leads to the problem that the JVM is running out of memory when exporting large amounts of data. As a result of that the system is getting unstable and will eventually crash. Design a solution to prevent this problem and implement your solution.

Document all necessary steps and changes you have made. Don't forget to measure your improvements and compare them to the measurement results before the improvements.

Zuletzt geändert: Donnerstag, 21. Mai 2015, 13:10
