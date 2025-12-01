# Integration Testing

### TestDatabaseContainer

Creates a test database with the testdependencies dependency.
Instantiates a single PostgreSQL container for specifically integration tests.
Singleton pattern to ensure only one instance of the container is created.


### BaseIntegrationTest