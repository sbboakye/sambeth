package com.sbboakye.engine.domain

sealed trait DomainValidation {
  def errorMessage: String
}

case object InvalidCronExpression extends DomainValidation {
  override def errorMessage: String = "Invalid cron expression"
}

case object InvalidTimezone extends DomainValidation {
  override def errorMessage: String = "Invalid timezone"
}
