package com.sbboakye.engine.services

import cats.Applicative
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import org.checkerframework.checker.units.qual.A
import org.http4s.EntityEncoder
import org.http4s.circe.jsonEncoderOf

sealed trait ApiResponse[+A]

object ApiResponse:
  case class Success[A](data: A)  extends ApiResponse
  case class Error(error: String) extends ApiResponse

  given [A: Encoder]: Encoder[ApiResponse.Success[A]] = deriveEncoder[ApiResponse.Success[A]]
  given Encoder[ApiResponse.Error]                    = deriveEncoder[ApiResponse.Error]

  given [F[_]: Applicative, A: Encoder]: EntityEncoder[F, ApiResponse.Success[A]] =
    jsonEncoderOf[F, ApiResponse.Success[A]]

  given [F[_]: Applicative]: EntityEncoder[F, ApiResponse.Error] =
    jsonEncoderOf[F, ApiResponse.Error]
