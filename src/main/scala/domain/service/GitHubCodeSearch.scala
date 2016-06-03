package domain.service

trait GitHubCodeSearch[Search, Url, Doc, Code, Result] extends AnyRef
  with FetchDoc[Search, Url, Doc]
  with CodeSearch[Doc, Code, Result]
