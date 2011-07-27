package ldif.util

/**
 * Represents a URI.
 */
@serializable class Uri(val uri : String)
{
  /**
   * The turtle representation of this Uri.
   *
   * Examples:
   * - dbpedia:Berlin
   * - <http://dbpedia.org/resource/Berlin>
   */
  def toTurtle(implicit prefixes : Prefixes = Prefixes.empty) : String =
  {
    for((id, namespace) <- prefixes if uri.startsWith(namespace))
    {
      return id + ":" + uri.substring(namespace.length)
    }

    "<" + uri + ">"
  }

  override def toString = uri
}

object Uri
{
  /**
   * Builds a URI from a string.
   */
  implicit def fromURI(uri : String) : Uri =
  {
    new Uri(uri)
  }

  /**
   * Builds a URI from a qualified name.
   *
   * @param qualifiedName The qualified name e.g. dbpedia:Berlin
   * @param prefixes The prefixes which will be used to resolve the qualified name
   */
  def fromQualifiedName(qualifiedName : String, prefixes : Map[String, String]) =
  {
    new Uri(resolvePrefix(qualifiedName, prefixes))
  }

  /**
   * Parses an URI in turtle notation.
   *
   * Examples:
   * - dbpedia:Berlin
   * - <http://dbpedia.org/resource/Berlin>
   */
  def parse(str : String, prefixes : Map[String, String] = Map.empty) =
  {
    if(str.startsWith("<"))
    {
      fromURI(str.substring(1, str.length - 1))
    }
    else
    {
      fromQualifiedName(str, prefixes)
    }
  }

  private def resolvePrefix(qualifiedName : String, prefixes : Map[String, String]) = qualifiedName.split(":", 2) match
  {
    case Array(prefix, suffix) => prefixes.get(prefix) match
    {
      case Some(resolvedPrefix) => resolvedPrefix + suffix
      case None => throw new IllegalArgumentException("Unknown prefix: " + prefix)
    }
    case _ => throw new IllegalArgumentException("No prefix found in " + qualifiedName)
  }
}
