package com.sbboakye.engine.config

import cats.effect.Async
import cats.effect.Resource
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import doobie.LogHandler
import doobie.util.log.LogEvent

object Database {

  private def printSQLLogHandler[F[_]](using F: Async[F]): LogHandler[F] = new LogHandler[F] {
    def run(logEvent: LogEvent): F[Unit] = F.pure(println(logEvent.sql)) // li F[_]
  }

  def makeDbResource[F[_]: Async](config: DBConfig): Resource[F, HikariTransactor[F]] =
    for {
      ec <- ExecutionContexts.fixedThreadPool[F](config.nThreads)
      xa <- HikariTransactor.newHikariTransactor[F](
        driverClassName = config.driver,
        url = s"jdbc:postgresql://${config.host}:${config.port}/${config.database}",
        user = config.user,
        pass = config.password,
        connectEC = ec,
        logHandler = Some(printSQLLogHandler)
      )
    } yield xa
}
