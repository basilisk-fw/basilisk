Basic Basilisk JavaFX/Java project
---------------------------------

You have just created a basic Basilisk application with JavaFX as UI toolkit
and Java as main language. The project has the following file structure

    .
    ├── build.gradle
    ├── basilisk-app
    │   ├── conf
    │   ├── controllers
    │   ├── i18n
    │   ├── lifecycle
    │   ├── models
    │   ├── resources
    │   ├── services
    │   └── views
    ├── pom.xml
    └── src
        ├── integration-test
        │   └── java
        ├── main
        │   ├── java
        │   └── resources
        └── test
            ├── java
            └── resources

Simply add your source files to `src/main/java`, your test cases to
`src/test/java` and then you will be able to build your project with

    gradle build
    gradle test
    gradle run

Don't forget to add any extra JAR dependencies to `build.gradle`!

If you prefer building with Maven then execute the following commands

    mvn compile
    mvn test
    mvn -Prun

Don't forget to add any extra JAR dependencies to `pom.xml`!
