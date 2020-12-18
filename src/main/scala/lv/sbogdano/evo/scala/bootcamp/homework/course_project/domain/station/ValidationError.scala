package lv.sbogdano.evo.scala.bootcamp.homework.course_project.domain.station

sealed trait ValidationError {
  def errorMessage: String
}

// Address validation
case object StreetNameInvalidFormat extends ValidationError {
  override def errorMessage: String = "Invalid Address: Street name must contain only letters"
}

case object StreetNumberInvalidFormat extends ValidationError {
  override def errorMessage: String = "Invalid Address: Street number must contain only numbers"
}

case object StreetNumberIsNegative extends ValidationError {
  override def errorMessage: String = "Invalid Address: Street number must be > 0"
}

// Construction validation
case object ConstructionInvalid extends ValidationError {
  override def errorMessage: String = "Invalid Construction: Construction must be Indoor or Outdoor"
}

// Year validation
case object YearInvalidFormat extends ValidationError {
  override def errorMessage: String = "Invalid Year: Year must contain only numbers"
}

case object YearInvalidLength extends ValidationError {
  override def errorMessage: String = "Invalid Year: Year must be 4 number length"
}

// Object number validation
case object ObjectNumberInvalidFormat extends ValidationError {
  override def errorMessage: String = "Invalid Object number: Object number must contain only numbers"
}

case object ObjectNumberInvalidLength extends ValidationError {
  override def errorMessage: String = "Invalid Object number: Object number must be between 0 - 9999"
}

// CityRegion validation
case object CityRegionInvalid extends ValidationError {
  override def errorMessage: String = "Invalid CityRegion: City region must be Riga, Daugavpils, Liepaja, Jelgava, Jurmala, Ventspils, Rezekne, Jekabpils, Valmiera, Ogre, Cesis"
}

// Object type validation
case object ObjectTypeInvalid extends ValidationError {
  override def errorMessage: String = "Invalid ObjectType: Object type must be TP, KTP, FP, SP or AST"
}

// Latitude invalid
case object LatitudeInvalid extends ValidationError {
  override def errorMessage: String = "Invalid Latitude: Latitude must not contain only numbers"
}

// Longitude invalid
case object LongitudeInvalid extends ValidationError {
  override def errorMessage: String = "Invalid Longitude: Longitude must not contain only numbers"
}

// ZoneOfResponsibility validation
case object ZoneOfResponsibilityInvalid extends ValidationError {
  override def errorMessage: String = "Invalid ZoneOfResponsibility: Zone of responsibility must be Latgale, Kurzeme, Zemgale or Vidzeme"
}

