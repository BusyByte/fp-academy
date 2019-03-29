package net.nomadicalien.fp.homework

package db {

  import java.sql.Connection
  import java.util.UUID

  import cats.effect.Sync

  final case class PersonId(id: UUID)

  final case class PersonName(value: String) extends AnyVal

  final case class Person(id: PersonId, name: PersonName)

  sealed trait DBOp[A]

  final case class InsertOp(p: Person) extends DBOp[Int]

  final case class UpdateOp(p: Person) extends DBOp[Int]

  final case class GetOp(id: PersonId) extends DBOp[Person]

  sealed trait DBConverter[A, DB]

  sealed trait ToDB[A, DB] extends DBConverter[A, DB] {
    def to(a: A): DB
  }

  sealed trait FromDB[A, DB] extends DBConverter[A, DB] {
    def from(b: DB): A
  }

  sealed trait Meta[A, DB] extends DBConverter[A, DB] with ToDB[A, DB] with FromDB[A, DB]

  object DBOp {
    def eval[A, F[_]](dbio: DBOp[A])(con: Connection)(
        uuidMeta: Meta[PersonId, String],
        nameMeta: Meta[PersonName, String])(implicit F: Sync[F]): F[A] = {
      import F.catchNonFatal
      import cats.implicits._
      dbio match {
        case InsertOp(person) =>
          val p: F[Int] = for {
            pstmt <- catchNonFatal(con.prepareStatement("insert into people(id, name) value(?, ?)"))
            _ <- catchNonFatal(pstmt.setString(1, uuidMeta.to(person.id)))
            _ <- catchNonFatal(pstmt.setString(2, nameMeta.to(person.name)))
            rowCount <- catchNonFatal(pstmt.executeUpdate())
          } yield rowCount

          p

        case UpdateOp(person) =>
          val p: F[Int] = for {
            pstmt <- catchNonFatal(con.prepareStatement("update people set name = ? where id = ?"))
            _ <- catchNonFatal(pstmt.setString(1, nameMeta.to(person.name)))
            _ <- catchNonFatal(pstmt.setString(2, uuidMeta.to(person.id)))
            rowCount <- catchNonFatal(pstmt.executeUpdate())
          } yield rowCount

          p

        case GetOp(id) =>
          val p: F[Person] = for {
            pstmt <- catchNonFatal(con.prepareStatement("select id, name from people where id = ?"))
            _ <- catchNonFatal(pstmt.setString(1, uuidMeta.to(id)))
            rs <- catchNonFatal(pstmt.executeQuery())
            rsId <- catchNonFatal(rs.getString("id"))
            rsName <- catchNonFatal(rs.getString("name"))
          } yield Person(uuidMeta.from(rsId), nameMeta.from(rsName))

          p

      }
    }

  }
}
