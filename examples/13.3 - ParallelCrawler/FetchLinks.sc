def fetchLinks(title: String): Seq[String] = {
  val resp = requests.get(
    "https://en.wikipedia.org/w/api.php",
    params = Seq(
      "action" -> "query",
      "titles" -> title,
      "prop" -> "links",
      "format" -> "json"
    )
  )
  ujson
    .read(resp.text())("query")("pages")
    .obj
    .values
    .filter(_.obj.contains("links"))
    .flatMap(_("links").arr).map(_("title").str)
    .toSeq
}
