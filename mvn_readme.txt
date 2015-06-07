
Zum hinfzuegen der libraries einfach in lib ordner gehen und folgende befehle ausführen

1. Spics jar in locales maven repo hinzufügen
	mvn install:install-file -Dfile=spics.jsf.jar -DgroupId=tuwien.ac.at -DartifactId=spics-jsf -Dversion=1.0.0 -Dpackaging=jar
	
2. gnujaxp in locales maven repo hinzufügen
	mvn install:install-file -Dfile=gnujaxp.jar -DgroupId=gnujaxp -DartifactId=gnujaxp -Dversion=1.0.0 -Dpackaging=jar


damit die maven dependencies verwendet werden können muss auch noch das maven-ant-task plugin für ant installiert werden
http://maven.apache.org/ant-tasks/installation.html