@echo off

SET JAVA_HOME=.\jre\bin

REM SET CLASSPATH=.
REM SET CLASSPATH=%CLASSPATH%;lib\google-api-services-analyticsreporting-v4-rev170-1.25.0.jar
REM SET CLASSPATH=%CLASSPATH%;lib\google-api-client-1.25.0.jar
REM SET CLASSPATH=%CLASSPATH%;lib\google-api-client-gson-1.30.5.jar
REM SET CLASSPATH=%CLASSPATH%;lib\google-http-client-1.32.1.jar
REM SET CLASSPATH=%CLASSPATH%;lib\google-http-client-gson-1.32.1.jar
REM SET CLASSPATH=%CLASSPATH%;lib\google-http-client-jackson2-1.25.0.jar
REM SET CLASSPATH=%CLASSPATH%;lib\google-oauth-client-1.25.0.jar
REM SET CLASSPATH=%CLASSPATH%;lib\grpc-context-1.22.1.jar
REM SET CLASSPATH=%CLASSPATH%;lib\gson-2.8.5.jar
REM SET CLASSPATH=%CLASSPATH%;lib\guava-20.0.jar
REM SET CLASSPATH=%CLASSPATH%;lib\httpclient-4.5.10.jar
REM SET CLASSPATH=%CLASSPATH%;lib\httpcore-4.4.12.jar
REM SET CLASSPATH=%CLASSPATH%;lib\j2objc-annotations-1.3.jar
REM SET CLASSPATH=%CLASSPATH%;lib\jackson-core-2.9.6.jar
REM SET CLASSPATH=%CLASSPATH%;lib\jsr305-3.0.2.jar
REM SET CLASSPATH=%CLASSPATH%;lib\opencensus-api-0.24.0.jar
REM SET CLASSPATH=%CLASSPATH%;lib\opencensus-contrib-http-util-0.24.0.jar
REM SET CLASSPATH=%CLASSPATH%;lib\poi-4.1.1.jar
REM SET CLASSPATH=%CLASSPATH%;lib\poi-ooxml-4.1.1.jar
REM SET CLASSPATH=%CLASSPATH%;lib\poi-ooxml-schemas-4.1.1.jar
REM SET CLASSPATH=%CLASSPATH%;lib\xmlbeans-3.1.0.jar
REM SET CLASSPATH=%CLASSPATH%;lib\commons-codec-1.13.jar
REM SET CLASSPATH=%CLASSPATH%;lib\commons-collections4-4.4.jar
REM SET CLASSPATH=%CLASSPATH%;lib\commons-compress-1.19.jar
REM SET CLASSPATH=%CLASSPATH%;lib\commons-logging-1.2.jar
REM SET CLASSPATH=%CLASSPATH%;lib\commons-math3-3.6.1.jar
REM SET CLASSPATH=%CLASSPATH%;lib\curvesapi-1.06.jar

REM echo java -cp %CLASSPATH% -jar GoogleAnalyticsReport-0.0.1-SNAPSHOT.jar
REM java -cp %CLASSPATH% -jar GoogleAnalyticsReport-0.0.1-SNAPSHOT.jar
%JAVA_HOME%\java -cp .;.\conf\  -jar GoogleAnalyticsReport-1.0.0-jar-with-dependencies.jar %1 %2