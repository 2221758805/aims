package aims.routing

import aims.util.Lists
import akka.http.model.Uri
import akka.http.model.Uri.Path
import akka.http.server.PathMatcher
import akka.http.server.PathMatcher.{ Matched, Unmatched }

import scala.collection.mutable.ListBuffer

/**
 * Component:
 * Description:
 * Date: 15/1/26
 * @author Andy Ai
 */

trait PatternMatcher {
  def apply(path: Uri.Path): Option[Any]
}

class AkkaPathMatcher(pm: PathMatcher[_]) extends PatternMatcher {
  override def apply(path: Path): Option[Any] = {
    pm.apply(path) match {
      case Matched(_, extractions) ⇒ Some(extractions)
      case Unmatched               ⇒ None
    }
  }
}

class AimsPathMatcher(pm: String) extends PatternMatcher {
  override def apply(path: Path): Option[Any] = {
    val tokens = pm.split("/")
    val parameters = ListBuffer[Int]()
    for (i ← 0 to (tokens.length - 1)) {
      val token = tokens(i)
      if (token.matches("""(:|#)\w+""")) {
        parameters += i
      }
    }
    val matcher = pm.replaceAll("""#\w+""", "\\\\d+").replaceAll(""":\w+""", "(\\\\w+-?)+")
    if (path.toString().matches(matcher)) {
      if (parameters.isEmpty) {
        Some(())
      } else {
        val segments = path.toString().split("/")
        Some(Lists.toTuple(parameters.map(segments(_)).toList))
      }
    } else {
      None
    }
  }
}

object PatternMatcher {
  def apply(pm: PathMatcher[_]): PatternMatcher = {
    new AkkaPathMatcher(pm)
  }

  def apply(pm: String): PatternMatcher = {
    new AimsPathMatcher(pm)
  }
}