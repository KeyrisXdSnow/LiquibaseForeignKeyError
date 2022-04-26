import liquibase.exception.DatabaseException;

import java.sql.SQLException;

public class App {
    public static void main(String[] args) throws SQLException, DatabaseException {
        LiquibaseExecutor executor = new LiquibaseExecutor("jdbc:mariadb://localhost:3306/keyTest", "root", "11111111");
        executor.executeLiquibase("0.createSchema.yaml");
        // drop foreign key changeSet failed
        executor.executeLiquibase("2.dropKey.yaml");
        // foreign key changeSet ran successfully
        //executor.executeLiquibase("2.dropKeyWithoutPreCondition.yaml");
    }
}
