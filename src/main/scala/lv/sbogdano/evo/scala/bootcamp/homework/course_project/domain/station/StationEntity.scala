package lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.station

case class StationEntity(
                          uniqueName: String,
                          stationAddress: String,
                          construction: String,
                          yearOfManufacture: Int,
                          inServiceFrom: Int,
                          name: String,
                          cityRegion: String,
                          latitude: Double,
                          longitude: Double,
                          zoneOfResponsibility: String
                        )
