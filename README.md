
## *Tasks to be verified*

> - Prepare documentation
>
>>1. List all possible match/action keys/attributes, nodeTypes etc.
>>2. Add all sub attributes if any
>>3. Add some sample syntax
>>4. Add more examples (probably list out all the currently used in the documentation)
>
> - Code Review
>
>>1. Code Review to understand the current flow
>>2. Optimize the code to make it more flexible by consolidating multiple common Actions to one Action
>
> - Error Handling (for json mapping)
>
> - ~~Validators (Optional)~~
>
> - ~~Order of json and multiple jsons to be used~~
> 
>>1. ~~Create config.json where the order of the json to be executed is maintained~~
>>2. ~~Add config for validators to be used and ignored~~
>>3. ~~Pickup all the configs/json/output path relatively from the jar instead of expecting from the command~~
>
> - ~~Create Actions as a Java Type like Match, Step etc.. instead of having it as List<Map<Map>> - might be useful for readability and additional usage~~
>
> - ~~NodeMatcher Code Refactor~~
> 
>>1. ~~Add flags instead of return so all the conditions will be met before moving out the matches~~
>>2. ~~Populate new exception list where the conditions fail because of manual error caused in the json~~
>
> - Need to cover almost all scenarios by logs so that will be helpful for debugging purposes
>
> - Need to check the performance on huge codebases like having more than 10k files as well as files having more than 20k/30k lines of code
>>1. If there are huge lines of code and exception happens will it be properly logged?
>
> - Test jarTypeSolver with some sample projects to see whether we can link the classes from the jar with the current class file


## *spring boot moderne recipes*

Found these recipes:

1) remove-redundant-maven-compiler-plugin
    -> Remove standard maven-compiler plugin for applications with boot parent.
2) initialize-spring-boot-migration
    -> Initialize an application as Spring Boot application.
3) migrate-jndi-lookup
    -> Migrate JNDI lookup using InitialContext to Spring Boot
4) migrate-jpa-to-spring-boot
    -> Migrate JPA to Spring Boot
5) migrate-ejb-jar-deployment-descriptor
    -> Add or overrides @Stateless annotation as defined in ejb deployment descriptor
6) migrate-weblogic-ejb-deployment-descriptor
    -> Migrate weblogic-ejb-jar.xml deployment descriptor
7) mark-and-clean-remote-ejbs
    -> Search @Stateless EJBs implementing a @Remote interface
8) migrate-stateless-ejb
    -> Migration of stateless EJB to Spring components.
9) migrate-annotated-servlets
    -> Allow Spring Boot to deploy servlets annotated with @WebServlet
10) migrate-jax-ws
    -> Migrate Jax Web-Service implementation to Spring Boot bases Web-Service
11) migrate-jax-rs
    -> Any class has import starting with javax.ws.rs
12) migrate-mule-to-boot
    -> Migrate Mulesoft 3.9 to Spring Boot.
13) migrate-tx-to-spring-boot
    -> Migration of @TransactionAttribute to @Transactionsl
14) spring-context-xml-import
    -> Import Spring Framework xml bean configuration into Java configuration without converting them.
15) migrate-spring-xml-to-java-config
    -> Migrate Spring Framework xml bean configuration to Java configuration.
16) migrate-jms
    -> Convert JEE JMS app into Spring Boot JMS app
17) documentation-actions
    -> Create Documentation for Actions
18) migrate-jsf-2.x-to-spring-boot
    -> Use joinfaces to integrate JSF 2.x with Spring Boot.
19) cn-spring-cloud-config-server
    -> Externalize properties to Spring Cloud Config Server
20) boot-2.4-2.5-upgrade-report
    -> Create Upgrade Report for a Spring Boot 2.4 Application
21) boot-2.7-3.0-dependency-version-update
    -> Bump spring-boot-starter-parent from 2.7.x to 3.0.0
22) boot-autoconfiguration-update
    -> Create org.springframework.boot.autoconfigure.AutoConfiguration.imports file for new spring 2.7
23) boot-2.4-2.5-datasource-initializer
    -> Replace deprecated spring.datasource.* properties
24) boot-2.4-2.5-spring-data-jpa
    -> Rename JpaRepository methods getId() and calls to getOne()
25) boot-2.4-2.5-dependency-version-update
    -> Update Spring Boot dependencies from 2.4 to 2.5
26) boot-2.7-3.0-upgrade-report
    -> Create a report for Spring Boot Upgrade from 2.7.x to 3.0.0-M3
27) boot-2.4-2.5-sql-init-properties
    -> Replace deprecated spring.datasource.* properties
28) sbu30-report
    -> Create a report for Spring Boot Upgrade from 2.7.x to 3.0.x
29) sbu30-upgrade-dependencies
    -> Spring boot 3.0 Upgrade - Upgrade dependencies
30) sbu30-set-java-version
    -> Spring boot 3.0 Upgrade - Set java version property in build file
31) sbu30-add-milestone-repositories
    -> Spring boot 3.0 Upgrade - Add milestone repository for dependencies and plugins
32) sbu30-migrate-spring-data-properties
    -> Spring boot 3.0 Upgrade - Migrate 'spring.data' properties to new property names
33) sbu30-remove-construtor-binding
    -> Spring boot 3.0 Upgrade - Remove redundant @ConstructorBinding annotations
34) sbu30-migrate-to-jakarta-packages
    -> Spring boot 3.0 Upgrade - Migrate javax packages to new jakarta packages
35) sbu30-johnzon-dependency-update
    -> Spring boot 3.0 Upgrade - Specify version number for johnzon-core
36) sbu30-225-logging-date-format
    -> Spring boot 3.0 Upgrade - Logging Date Format
37) sbu30-auto-configuration
    -> Move EnableAutoConfiguration Property from spring.factories to AutoConfiguration.imports
38) sbu30-upgrade-spring-cloud-dependency
    -> Upgrade Spring Cloud Dependencies
39) sbu30-upgrade-boot-version
    -> Spring boot 3.0 Upgrade - Upgrade Spring Boot version
40) sbu30-remove-image-banner
    -> Spring boot 3.0 Upgrade - Remove the image banner at src/main/resources
41) sbu30-paging-and-sorting-repository
    -> Spring boot 3.0 Upgrade - Add CrudRepository interface extension additionally to PagingAndSortingRepository
42) migrate-raml-to-spring-mvc
    -> Create Spring Boot @RestController from .raml files.
43) migrate-boot-2.3-2.4
    -> Migrate from Spring Boot 2.3 to 2.4
44) upgrade-boot-1x-to-2x
    -> Migrate applications built on previous versions of Spring Boot to the latest Spring Boot 2.7 release. This recipe will modify an application's build files, make changes to deprecated/preferred APIs, and migrate configuration settings that have changes across Spring Boot versions. This recipe will also chain additional framework migrations (Spring Framework, Spring Data, JUnit, etc) that are required as part of the migration to Spring Boot 2.7.