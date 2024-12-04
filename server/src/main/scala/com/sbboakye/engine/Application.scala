package com.sbboakye.engine

import cats.*
import cats.effect.*
import cats.implicits.*
import com.sbboakye.engine.config.{AppConfig, Database}
import pureconfig.ConfigSource
import com.sbboakye.engine.config.syntax.*
import doobie.*
import doobie.implicits.*

object Application extends IOApp.Simple {

  override def run: IO[Unit] =
    ConfigSource.default.loadF[IO, AppConfig].flatMap { case AppConfig(dbConfig) =>
      val appResource = 
        for {
          xa     <- Database.makeDbResource[IO](dbConfig)
          result <- Resource.eval(sql"SELECT version()".query[String].unique.transact(xa))
        } yield result
      
      appResource.use(IO.println)
    }
}
