/*
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.testdata;

import static com.google.base.TestBase.TESTDATA_SQL_PATH;
import static com.google.common.base.Suppliers.memoize;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.sql.SqlHelper.connectAndExecuteQueryAsUser;
import static com.google.sql.SqlHelper.executeQueries;
import static com.google.sql.SqlHelper.getSql;
import static java.lang.String.format;
import static java.lang.System.lineSeparator;
import static java.lang.System.nanoTime;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class providing test data generation methods
 */
public final class TestDataHelper {

  private static final Logger LOGGER = LoggerFactory.getLogger(TestDataHelper.class);

  private TestDataHelper() {
  }

  /**
   * @param connection DB connection parameter
   * @param userCount Repetition count
   */
  public static void generateUsers(Connection connection, int userCount) throws SQLException {
    final String userData = getSql(TESTDATA_SQL_PATH + "users_data.sql");
    final String password = randomUUID().toString();

    ImmutableList<String> sqlQueries =
        Stream.generate(memoize(() -> userData))
            .limit(userCount)
            .map(sqlQuery -> format(sqlQuery, getRandomUsername(), password))
            .collect(toImmutableList());

    executeQueries(connection, sqlQueries);

    LOGGER.info(
        format(
            "Generated %s new User(s):" + lineSeparator() + "%s",
            sqlQueries.size(),
            Joiner.on(lineSeparator()).join(sqlQueries).replaceAll(password, "####")));
  }

  /**
   * @param connection DB connection parameter
   * @param dbTablePairsCount Repetition count
   */
  public static void generateDbTablePairs(Connection connection, int dbTablePairsCount)
      throws SQLException {
    final String dbData = getSql(TESTDATA_SQL_PATH + "columns_data_1.sql");
    final String tableData = getSql(TESTDATA_SQL_PATH + "columns_data_2.sql");

    List<String> sqlQueries = new ArrayList<>();
    while (dbTablePairsCount > 0) {
      String dbName = getRandomDbName();
      String tableName = getRandomTableName();

      sqlQueries.add(format(dbData, dbName));
      sqlQueries.add(format(tableData, dbName, tableName));
      dbTablePairsCount--;
    }

    executeQueries(connection, sqlQueries);

    LOGGER.info(
        format(
            "Generated %s new DB and Table pair(s):" + lineSeparator() + "%s",
            sqlQueries.size() / 2, Joiner.on(lineSeparator()).join(sqlQueries)));
  }

  /**
   * @param connection DB connection parameter
   * @param functionCount Repetition count
   */
  public static void generateFunctions(Connection connection, int functionCount)
      throws SQLException {
    final String functioninfoData = getSql(TESTDATA_SQL_PATH + "functioninfo_data.sql");

    ImmutableList<String> sqlQueries =
        Stream.generate(memoize(() -> functioninfoData))
            .limit(functionCount)
            .map(sqlQuery -> format(sqlQuery, getRandomFunctionName()))
            .collect(toImmutableList());

    executeQueries(connection, sqlQueries);

    LOGGER.info(
        format(
            "Generated %s new function(s):" + lineSeparator() + "%s",
            sqlQueries.size(), Joiner.on(lineSeparator()).join(sqlQueries)));
  }

  /**
   * @param connection DB connection parameter
   * @param constraintsCount Repetition count
   */
  public static void generateConstraints(Connection connection, int constraintsCount)
      throws SQLException {
    final String allRiChildrenData1 = getSql(TESTDATA_SQL_PATH + "columns_data_1.sql");
    final String allRiChildrenData2 = getSql(TESTDATA_SQL_PATH + "all_ri_children_data_1.sql");
    final String allRiChildrenData3 = getSql(TESTDATA_SQL_PATH + "all_ri_children_data_2.sql");
    final String allRiChildrenData4 = getSql(TESTDATA_SQL_PATH + "all_ri_children_data_3.sql");

    List<String> sqlQueries = new ArrayList<>();
    while (constraintsCount > 0) {
      String parentDbName = getRandomDbName();
      String childDbName = getRandomDbName();

      sqlQueries.add(format(allRiChildrenData1, parentDbName));
      sqlQueries.add(format(allRiChildrenData1, childDbName));

      sqlQueries.add(format(allRiChildrenData2, parentDbName));
      sqlQueries.add(format(allRiChildrenData3, childDbName));

      sqlQueries.add(format(allRiChildrenData4, childDbName, parentDbName));
      constraintsCount--;
    }

    executeQueries(connection, sqlQueries);

    LOGGER.info(
        format(
            "Generated %s new constraint(s):" + lineSeparator() + "%s",
            sqlQueries.size(), Joiner.on(lineSeparator()).join(sqlQueries)));
  }

  /**
   * @param connection DB connection parameter
   * @param constraintsCount Repetition count
   */
  public static void generatePartitioningConstraints(Connection connection, int constraintsCount)
      throws SQLException {
    final String partitioningConstraintsData1 = getSql(TESTDATA_SQL_PATH + "columns_data_1.sql");
    final String partitioningConstraintsData2 = getSql(
        TESTDATA_SQL_PATH + "partitioning_constraints_data.sql");

    List<String> sqlQueries = new ArrayList<>();
    while (constraintsCount > 0) {
      String dbName = getRandomDbName();
      String tableName = getRandomTableName();

      sqlQueries.add(format(partitioningConstraintsData1, dbName));
      sqlQueries.add(format(partitioningConstraintsData2, dbName, tableName));
      constraintsCount--;
    }
    executeQueries(connection, sqlQueries);

    LOGGER.info(
        format(
            "Generated %s new partitioning constraint(s):" + lineSeparator() + "%s",
            sqlQueries.size(), Joiner.on(lineSeparator()).join(sqlQueries)));
  }

  /**
   * @param connection DB connection parameter
   * @param statsCount Repetition count
   */
  public static void generateStats(Connection connection, int statsCount)
      throws SQLException {
    final String statsData1 = getSql(TESTDATA_SQL_PATH + "columns_data_1.sql");
    final String statsData2 = getSql(TESTDATA_SQL_PATH + "columns_data_2.sql");
    final String statsData3 = getSql(TESTDATA_SQL_PATH + "stats_data.sql");

    List<String> sqlQueries = new ArrayList<>();
    while (statsCount > 0) {
      String dbName = getRandomDbName();
      String tableName = getRandomTableName();

      sqlQueries.add(format(statsData1, dbName));
      sqlQueries.add(format(statsData2, dbName, tableName));
      sqlQueries.add(format(statsData3, dbName, tableName));
      statsCount--;
    }
    executeQueries(connection, sqlQueries);

    LOGGER.info(
        format(
            "Generated %s new monitoring rule(s):" + lineSeparator() + "%s",
            sqlQueries.size() * 2, Joiner.on(lineSeparator()).join(sqlQueries)));
  }

  /**
   * @param connection DB connection parameter
   * @param rolesCount Repetition count
   */
  public static void generateRoles(Connection connection, int rolesCount)
      throws SQLException {
    final String rolesData1 = getSql(TESTDATA_SQL_PATH + "users_data.sql");
    final String rolesData2 = getSql(TESTDATA_SQL_PATH + "roles_data_1.sql");
    final String rolesData3 = getSql(TESTDATA_SQL_PATH + "roles_data_2.sql");

    List<String> sqlQueries = new ArrayList<>();

    while (rolesCount > 0) {
      String username = getRandomUsername();
      String password = randomUUID().toString();

      String createDbQuery = format(rolesData1, username, password);
      String grantRoleQuery = format(rolesData2, username);
      String createRoleQuery = format(rolesData3, getRandomRolename());

      sqlQueries.add(createDbQuery);
      sqlQueries.add(grantRoleQuery);
      sqlQueries.add(createRoleQuery);

      executeQueries(connection, asList(createDbQuery, grantRoleQuery));
      connectAndExecuteQueryAsUser(username, password, createRoleQuery);

      rolesCount--;
    }

    LOGGER.info(
        format(
            "Generated %s new role(s):" + lineSeparator() + "%s",
            sqlQueries.size(), Joiner.on(lineSeparator()).join(sqlQueries)));
  }

  private static String getRandomUsername() {
    return format("test_user_%s", nanoTime());
  }

  private static String getRandomDbName() {
    return format("test_db_%s", nanoTime());
  }

  private static String getRandomTableName() {
    return format("test_table_%s", nanoTime());
  }

  private static String getRandomFunctionName() {
    return format("test_func_%s", nanoTime());
  }

  private static String getRandomRolename() {
    return format("test_role_%s", nanoTime());
  }
}