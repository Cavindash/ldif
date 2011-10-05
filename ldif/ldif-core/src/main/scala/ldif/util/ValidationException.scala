package ldif.util

import ldif.util.ValidationException.ValidationError

/**
 * Thrown if the configuration is not valid.
 */
class ValidationException(val errors: Seq[ValidationError], cause: Throwable) extends Exception(errors.map(_.message).mkString(" ")) {
  def this(errors: Seq[ValidationError]) = this(errors, null)

  def this(error: String, cause: Throwable) = this(ValidationError(error) :: Nil, cause)

  def this(error: String, id: Identifier) = this(ValidationError(error, Some(id)) :: Nil, null)

  def this(error: String, id: Identifier, elementType: String) = this(ValidationError(error, Some(id), Some(elementType)) :: Nil, null)

  def this(error: String) = this(ValidationError(error) :: Nil, null)

  override def toString = errors.mkString("\n")
}

object ValidationException {
  case class ValidationError(message: String, id: Option[Identifier] = None, elementType: Option[String] = None) {
    override def toString = id match {
      case Some(identifier) => "Validation error in " + elementType.getOrElse("element") + " with id '" + identifier + "': " + message
      case None => message
    }
  }
}