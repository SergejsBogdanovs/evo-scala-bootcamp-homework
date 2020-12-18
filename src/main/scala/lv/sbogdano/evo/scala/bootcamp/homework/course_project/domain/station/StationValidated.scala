package lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.station

case class StationValidated(
                             stationAddress: StationAddress,
                             construction: Construction,
                             yearOfManufacture: Year,
                             inServiceFrom: Year,
                             name: Name,
                             cityRegion: CityRegion,
                             latitude: Latitude,
                             longitude: Longitude,
                             zoneOfResponsibility: ZoneOfResponsibility
                           ) {
  def toStationEntity: StationEntity = StationEntity(
    s"${this.cityRegion.toString}_${this.name.toString}",
    this.stationAddress.toString,
    this.construction.toString,
    this.yearOfManufacture.year,
    this.inServiceFrom.year,
    this.name.toString,
    this.cityRegion.toString,
    this.latitude.coordinates,
    this.longitude.coordinates,
    this.zoneOfResponsibility.toString
  )
}
