package lv.sbogdano.evo.scala.bootcamp.homework.cassesandtraits

object ClassesAndTraits extends App {

  // Homework
  //
  // Add additional 2D shapes such as triangle and square.
  //
  // In addition to the 2D shapes classes, add also 3D shapes classes
  // (origin, point, sphere, cube, cuboid, 3D triangle - you can add
  // others if you think they are a good fit).
  //
  // Add method `area` to 2D shapes.
  //
  // Add methods `surfaceArea` and `volume` to 3D shapes.
  //
  // If some of the implementation involves advanced math, it is OK
  // to skip it (leave unimplemented), the primary intent of this
  // exercise is modelling using case classes and traits, and not math.

  sealed trait Shape[T] extends Located with Bounded

  sealed trait Shapes2D[T] extends Shape[T] {
    def area: Double
  }

  sealed trait Shapes3D[T] extends Shape[T] {
    def z: Double
    def minZ: Double
    def maxZ: Double
    def surfaceArea: Double
    def volume: Double
  }

  sealed trait Located {
    def x: Double
    def y: Double
  }

  sealed trait Bounded {
    def minX: Double
    def maxX: Double
    def minY: Double
    def maxY: Double
  }

  sealed trait Movable[T <: Shape[T]] {
    def move(dx: Double, dy: Double, dz: Double): Shape[T]
  }

  final case class Point(x: Double, y: Double) extends Shape[Point] with Movable[Point] {
    override def minX: Double = x
    override def maxX: Double = x
    override def minY: Double = y
    override def maxY: Double = y

    override def move(dx: Double, dy: Double, dz: Double): Point = Point(x + dx, y + dy)
  }

  final case class Circle(centerX: Double, centerY: Double, radius: Double) extends Shapes2D[Circle] with Movable[Circle]  {
    override def x: Double = centerX
    override def y: Double = centerY

    override def minX: Double = centerX - radius
    override def maxX: Double = centerX + radius
    override def minY: Double = centerY - radius
    override def maxY: Double = centerY + radius

    override def move(dx: Double, dy: Double, dz: Double): Circle = Circle(x + dx, y + dy, radius)
    override def area: Double = math.Pi * radius * radius
  }

  final case class Rectangle(x: Double, y: Double, sideA: Double, sideB: Double) extends Shapes2D[Rectangle] with Movable[Rectangle] {
    override def minX: Double = x
    override def maxX: Double = x + sideA
    override def minY: Double = y
    override def maxY: Double = y + sideB

    override def move(dx: Double, dy: Double, dz: Double): Rectangle = Rectangle(x + dx, y + dy, sideA, sideB)

    override def area: Double = sideA * sideB
  }

  final case class Square(x: Double, y: Double, side: Double) extends Shapes2D[Square] with Movable[Square] {
    override def minX: Double = x
    override def maxX: Double = x + side
    override def minY: Double = y
    override def maxY: Double = y + side

    override def move(dx: Double, dy: Double, dz: Double): Square = Square(x + dx, y + dy, side)

    override def area: Double = side * side
  }

  final case class Triangle(sideA: Double, sideB: Double, sideC: Double) extends Shapes2D[Triangle] with Movable[Triangle] {
    override def x: Double = ???
    override def y: Double = ???

    override def minX: Double = ???
    override def maxX: Double = ???
    override def minY: Double = ???
    override def maxY: Double = ???

    override def move(dx: Double, dy: Double, dz: Double): Triangle = ???

    override def area: Double = {
      val s = (sideA + sideB + sideC) / 2
      math.sqrt(s * (s - sideA) * (s - sideB) * (s - sideC))
    }

    final case class Sphere(centerX: Double, centerY: Double, centerZ: Double, radius: Double) extends Shapes3D[Sphere] with Movable[Sphere] {
      override def x: Double = centerX
      override def y: Double = centerY
      override def z: Double = centerZ

      override def minX: Double = centerX - radius
      override def maxX: Double = centerX + radius
      override def minY: Double = centerY - radius
      override def maxY: Double = centerY + radius
      override def minZ: Double = centerZ - radius
      override def maxZ: Double = centerZ + radius

      override def move(dx: Double, dy: Double, dz: Double): Sphere = Sphere(x + dx, y + dy, z + dz, radius)

      override def surfaceArea: Double = 4 * math.Pi * radius * radius

      override def volume: Double = (4 * math.Pi * math.pow(radius, 3)) / 3
    }

    final case class Cone(centerX: Double, centerY: Double, centerZ: Double, height: Double, radius: Double, sideLength: Double) extends Shapes3D[Sphere] with Movable[Sphere] {
      override def x: Double = centerX
      override def y: Double = centerY
      override def z: Double = centerZ

      override def minX: Double = centerX - radius
      override def maxX: Double = centerX + radius
      override def minY: Double = centerY
      override def maxY: Double = centerY + height
      override def minZ: Double = centerZ - radius
      override def maxZ: Double = centerZ + radius

      override def move(dx: Double, dy: Double, dz: Double): Cone = Cone(x + dx, y + dy, z + dz, height, radius, sideLength)

      override def surfaceArea: Double = (math.Pi * radius * sideLength) + (math.Pi * radius * radius)

      override def volume: Double = (math.Pi * radius * radius * height) / 3
    }

    final case class Cube(x: Double, y: Double, z: Double, side: Double) extends Shapes3D[Cube] with Movable[Cube] {
      override def minX: Double = x
      override def maxX: Double = x + side
      override def minY: Double = y
      override def maxY: Double = y + side
      override def minZ: Double = z
      override def maxZ: Double = z + side

      override def move(dx: Double, dy: Double, dz: Double): Cube = Cube(x + dx, y + dy, z + dz, side)

      override def surfaceArea: Double = 6 * side * side

      override def volume: Double = math.pow(side, 3)
    }

  }


}
