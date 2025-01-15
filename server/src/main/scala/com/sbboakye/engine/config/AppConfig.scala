package com.sbboakye.engine.config

import pureconfig.ConfigReader

case class AppConfig(emberConfig: EmberConfig, dbConfig: DBConfig) derives ConfigReader
