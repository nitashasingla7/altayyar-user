
# altayyar-users
Provides Users and verification code functionality.

The AlTayyar-Users application is a command-line executable Web application with an _embedded instance of the Tomcat server_.
The application exposes REST-ful APIs for user code generation and verification.

It can be run in the following ways :
1. Run the following mvn command if you are using maven:
  mvn spring-boot:run
2. War file 'altayyar-users-0.0.1-SNAPSHOT' can be deployed in any application server.

If deploying on tomcat OR starting using maven it will be deployed at port 8080 by default. So following URLs can be used to access application  
 	http://localhost:8080/verification_code/{user_id}.xml
	http://localhost:8080/verification_code/{user_id}/{code}.xml
	
Application uses in-memory database (H2). To see the user table for testing purpose following property is added in application.properties.

spring.h2.console.enabled=true, so H2 console can be accessed at http://localhost:8080/h2-console using jdbc connection string "jdbc:h2:mem:testdb"
You should be able to see USER table after connecting to H2 console. 

