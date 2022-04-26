import liquibase.Liquibase;
import liquibase.Scope;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.executor.ExecutorService;
import liquibase.repackaged.org.apache.commons.lang3.StringUtils;
import liquibase.resource.FileSystemResourceAccessor;

import java.io.File;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class LiquibaseExecutor {

    private String dbUrl;
    private String user;
    private String password;

    public LiquibaseExecutor(String dbUrl, String user, String password) {
        this.dbUrl = dbUrl;
        this.user = user;
        this.password = password;
    }

    public void executeLiquibase(String fileName) throws SQLException, DatabaseException {

        if (fileName.isEmpty()) {
            throw new IllegalArgumentException("Liquibase create table schema file is required");
        }

        var connection = DriverManager.getConnection(dbUrl,user,password);
        Liquibase liquibase = null;

        try {
            liquibase = createLiquibase(connection, fileName);
            liquibase.update(StringUtils.EMPTY);
        } catch (LiquibaseException | MalformedURLException e) {
            throw new DatabaseException(e);
        } finally {
            if (liquibase != null) {
                try {
                    liquibase.forceReleaseLocks();
                } catch (LiquibaseException e) {
                    System.out.println(e.getMessage());
                }
            }
            if (connection != null) {
                try {
                    connection.rollback();
                    connection.close();
                } catch (SQLException e) {
                    // nothing to do
                }
            }
        }
    }

    private Liquibase createLiquibase(Connection connection, String changeLog) throws LiquibaseException, MalformedURLException {

        if (connection == null) {
            throw new IllegalArgumentException("connection is required!");
        }

        var database = DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(new JdbcConnection(connection));
        Scope.getCurrentScope().getSingleton(ExecutorService.class).clearExecutor("jdbc", database);
        database.resetInternalState();

        File dirOfChangeLog;
        dirOfChangeLog = new File(System.getProperty("user.dir") + "/src/main/resources/");

        return new Liquibase(changeLog, new FileSystemResourceAccessor(dirOfChangeLog), database);
    }
}
