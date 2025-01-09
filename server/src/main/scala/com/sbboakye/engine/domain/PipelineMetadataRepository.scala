package com.sbboakye.engine.domain

import com.sbboakye.engine.domain.CustomTypes.PipelineId

trait PipelineMetadataRepository[F[_]] {
  def findMetadataById(id: PipelineId): F[Option[PipelineMetadata]]
}
