package com.sbboakye.engine.config

import pureconfig.ConfigReader

case class AppConfig(dbConfig: DBConfig) derives ConfigReader
