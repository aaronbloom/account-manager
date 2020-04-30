# ACME Bank account-manager service

Self contained service exposing a HTTP REST API with an embedded H2 database to manage accounts and their balances.

## Key Technologies

- Java 8
- H2 Database
- Javalin
- Gradle
- JUnit
- Mockito

## Developing locally

You can develop and run this application using an IDE such as IntelliJ IDEA.

This can also be done via a command line terminal (examples given using `bash` shell).

### Run tests

```sh
./gradlew test
```

### Build jar

Output found as `./build/libs/account-manager-*.jar`

```sh
./gradlew jar
```

### Run application

```sh
./gradlew run
```

## Tests

JUnit and Mockito are used for Unit testing. Functional tests are tagged with the annotation `@Tag("Functional")`, they also use the UniRest REST testing tool.
 
## HTTP REST API specification

### `GET` Accounts balance

Example curl HTTP request to get account `12345678` balance:

```sh
curl --location --request GET 'http://localhost:8080/api/account/balance/12345678'
```

### `POST` Transfer between accounts

Example curl HTTP request to transfer `120` from account `12345678` to account `88888888`:

```sh
curl --location --request POST 'http://localhost:8080/api/account/transfer/from/12345678/to/88888888/amount/120'
```

## Configuration

Configuration can be supplied optionally via environment variables.

Optional environment variables:

- `ACCOUNT_MANAGER_PORT` - integer, defaults to `8080`
- `ACCOUNT_MANAGER_H2_URL` - H2 JDBC string, defaults to `jdbc:h2:./database/account-manager-db`
- `ACCOUNT_MANAGER_H2_USERNAME` - string, defaults to empty
- `ACCOUNT_MANAGER_H2_PASSWORD` - string, defaults to empty
