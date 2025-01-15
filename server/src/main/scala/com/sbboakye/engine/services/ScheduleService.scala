package com.sbboakye.engine.services

import cats.*
import cats.syntax.all.*
import cats.effect.*
import com.sbboakye.engine.contexts.RepositoryContext
import com.sbboakye.engine.contexts.RepositoryContext.NoHelper
import com.sbboakye.engine.domain.Schedule
import com.sbboakye.engine.repositories.schedule.SchedulesRepository
import doobie.Transactor

import java.util.UUID

class ScheduleService[F[_]](repositoryContext: RepositoryContext[F])(using
    repoContext: repositoryContext.schedulesRepositorySetup.type,
    xa: Transactor[F]
) extends CoreService[F, SchedulesRepository, NoHelper, Schedule]:

  override def findAll: F[Seq[Schedule]] = withDependencies { (repo, _, _) =>
    repo.findAll(0, 10)
  }

  override def findById(id: UUID): F[Option[Schedule]] = withDependencies { (repo, _, _) =>
    repo.findById(id)
  }

  override def create(entity: Schedule): F[UUID] = withDependencies { (repo, _, _) =>
    repo.create(entity)
  }

  override def update(id: UUID, entity: Schedule): F[Option[Int]] = withDependencies {
    (repo, _, _) =>
      repo.update(id, entity)
  }

  override def delete(id: UUID): F[Option[Int]] = withDependencies { (repo, _, _) =>
    repo.delete(id)
  }
