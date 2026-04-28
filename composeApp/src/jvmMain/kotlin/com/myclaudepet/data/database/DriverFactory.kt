package com.myclaudepet.data.database

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.myclaudepet.db.PetDatabase

class DriverFactory {

    fun create(): SqlDriver {
        val driver = JdbcSqliteDriver("jdbc:sqlite:${AppPaths.databaseFile.toAbsolutePath()}")
        migrateIfNeeded(driver)
        PetDatabase.Schema.create(driver)
        return driver
    }

    /**
     * 간단한 PRAGMA user_version 기반 스키마 관리.
     * 버전이 올라가면 기존 `pet_state` 를 drop 하고, 아래 `Schema.create` 가 새 정의로
     * 재생성한다. 저장된 데이터는 손실되지만, 앱 전체 재설치/업데이트 맥락에서
     * UX 상 "새로 키우기" 로 일관되므로 수용 가능.
     */
    private fun migrateIfNeeded(driver: SqlDriver) {
        val current = driver.executeQuery(
            identifier = null,
            sql = "PRAGMA user_version",
            mapper = { cursor ->
                QueryResult.Value(if (cursor.next().value) cursor.getLong(0) ?: 0L else 0L)
            },
            parameters = 0,
        ).value
        if (current < SCHEMA_VERSION) {
            driver.execute(null, "DROP TABLE IF EXISTS pet_state", 0)
            driver.execute(null, "PRAGMA user_version = $SCHEMA_VERSION", 0)
        }
    }

    private companion object {
        const val SCHEMA_VERSION = 3L
    }
}
