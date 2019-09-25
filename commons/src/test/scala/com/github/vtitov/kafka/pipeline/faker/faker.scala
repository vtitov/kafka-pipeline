package com.github.vtitov.kafka.pipeline

import com.github.javafaker.Faker

package object faker {
  private lazy val faker = new Faker()

  def fileName(): String = faker.file().fileName(null,null,null,"/")
}
