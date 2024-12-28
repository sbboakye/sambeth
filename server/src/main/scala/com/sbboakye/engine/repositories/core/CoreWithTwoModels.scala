//package com.sbboakye.engine.repositories.core
//
//import cats.*
//import cats.effect.*
//import cats.syntax.all.*
//import doobie.*
//import doobie.implicits.*
//import doobie.postgres.*
//import doobie.postgres.implicits.*
//import org.typelevel.log4cats.Logger
//
//import java.util.UUID
//import scala.reflect.ClassTag
//
//trait CoreWithTwoModels[F[
//    _
//]: MonadCancelThrow: Logger, Entity: Read: ClassTag]
//    extends Core[F, Entity]:
//
//  private def entityName(using ct: ClassTag[Entity]): String =
//    ct.runtimeClass.getSimpleName
//
//  override def findAll(select: Fragment, offset: Int, limit: Int, func: Fragment => F[Seq[Entity]])(
//      using xa: Transactor[F]
//  ): F[Seq[Entity]] =
//    Logger[F].info(s"Fetching all ${entityName} with offset: $offset, limit: $limit") *> func(
//      select
//    )
////      queryForJoins(
////        (select ++ fr"LIMIT $limit OFFSET $offset")
////          .query[(Entity1, Entity2)]
////          .to[Seq]
////      )
//
////  override def findByID(select: Fragment, where: Fragment)(using
////      xa: Transactor[F]
////  ): F[Option[(Entity1, Entity2)]] =
////    Logger[F].info(s"Fetching ${entityName}") *>
////      queryForJoins(
////        (select ++ where)
////          .query[(Entity1, Entity2)]
////          .option
////      )
////.map { rows =>
////  rows
////    .groupBy(_._1)
////    .map { case (stage, connectors) =>
////      stage.copy(connectors = connectors.flatMap(_._2).toList)
////    }
////    .toSeq
////}
