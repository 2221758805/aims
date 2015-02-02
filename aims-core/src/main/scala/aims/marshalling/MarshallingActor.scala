package aims.marshalling

import aims.core.Pagination
import aims.core.model.headers.XTotalCount
import aims.json.Jackson
import aims.model.Marshalling
import akka.actor.{ Actor, ActorLogging }
import akka.http.model.StatusCodes._
import akka.http.model.headers.{ Link, LinkParams, LinkValue }
import akka.http.model.{ ContentTypes, HttpEntity, HttpRequest, HttpResponse }

import scala.collection.immutable
import scala.runtime.BoxedUnit

/**
 * Component:
 * Description:
 * Date: 15/2/2
 * @author Andy Ai
 */
class MarshallingActor extends Actor with ActorLogging {
  override def receive: Receive = {
    case Marshalling(response: HttpResponse, _, responder)          ⇒ responder ! response
    case Marshalling(unit: BoxedUnit, _, responder)                 ⇒ responder ! HttpResponse(OK)
    case Marshalling(str: String, _, responder)                     ⇒ responder ! HttpResponse(OK, entity = str)
    case Marshalling(number: Number, _, responder)                  ⇒ responder ! HttpResponse(OK, entity = number.toString)
    case Marshalling(pagination: Pagination[_], request, responder) ⇒ responder ! makePagination(pagination, request)
    case m ⇒
      println(m)
  }

  private def makePagination(pagination: Pagination[_], request: HttpRequest): HttpResponse = {
    if (pagination.items.isEmpty) {
      return HttpResponse(entity = HttpEntity(ContentTypes.`application/json`, "[]"))
    }
    pagination.links.filter {
      case LinkParams.next  ⇒ pagination.page < pagination.totalPage
      case LinkParams.prev  ⇒ pagination.page - 1 > 1
      case LinkParams.first ⇒ pagination.page > 1
      case LinkParams.last  ⇒ pagination.page < pagination.totalPage
    }.map {
      case LinkParams.next  ⇒ LinkValue(request.uri.withQuery(request.uri.query.+:("page", (pagination.page + 1).toString)), LinkParams.next)
      case LinkParams.prev  ⇒ LinkValue(request.uri.withQuery(request.uri.query.+:("page", (pagination.page - 1).toString)), LinkParams.prev)
      case LinkParams.first ⇒ LinkValue(request.uri.withQuery(request.uri.query.+:("page", 1.toString)), LinkParams.first)
      case LinkParams.last  ⇒ LinkValue(request.uri.withQuery(request.uri.query.+:("page", pagination.totalPage.toString)), LinkParams.last)
    } match {
      case Nil   ⇒ HttpResponse(status = OK, headers = immutable.Seq(XTotalCount(pagination.totalCount)), entity = HttpEntity(ContentTypes.`application/json`, Jackson.mapper.writeValueAsString(pagination.items)))
      case links ⇒ HttpResponse(status = OK, headers = immutable.Seq(Link(links: _*), XTotalCount(pagination.totalCount)), entity = HttpEntity(ContentTypes.`application/json`, Jackson.mapper.writeValueAsString(pagination.items)))
    }
  }
}

object MarshallingActor {
  val name = "marshaller"
}