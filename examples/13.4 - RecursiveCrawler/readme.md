```bash
amm --class-based TestCrawler.sc
```

Diff from [13.3 - ParallelCrawler](https://github.com/handsonscala/handsonscala/tree/master/examples/13.13.3%20-%20ParallelCrawler):
```diff
diff --git a/13.3 - ParallelCrawler/Crawler.sc b/13.4 - RecursiveCrawler/Crawler.sc
index 59513c7..c021ac7 100644
--- a/13.3 - ParallelCrawler/Crawler.sc	
+++ b/13.4 - RecursiveCrawler/Crawler.sc	
@@ -1,14 +1,18 @@
 import $file.FetchLinks, FetchLinks._
 import scala.concurrent._, ExecutionContext.Implicits.global, duration.Duration.Inf
 
-def fetchAllLinksParallel(startTitle: String, depth: Int): Set[String] = {
-  var seen = Set(startTitle)
-  var current = Set(startTitle)
-  for (i <- Range(0, depth)) {
+def fetchAllLinksRec(startTitle: String, depth: Int): Set[String] = {
+  def rec(current: Set[String], seen: Set[String], recDepth: Int): Set[String] = {
+    if (recDepth >= depth) seen
+    else {
       val futures = for (title <- current) yield Future{ fetchLinks(title) }
       val nextTitleLists = futures.map(Await.result(_, Inf))
-    current = nextTitleLists.flatten.filter(!seen.contains(_))
-    seen = seen ++ current
+      rec(
+        nextTitleLists.flatten.filter(!seen.contains(_)),
+        seen ++ nextTitleLists.flatten,
+        recDepth + 1
+      )
     }
-  seen
+  }
+  rec(Set(startTitle), Set(startTitle), 0)
 }
diff --git a/13.3 - ParallelCrawler/TestCrawler.sc b/13.4 - RecursiveCrawler/TestCrawler.sc
index 687b7f7..7522fa7 100644
--- a/13.3 - ParallelCrawler/TestCrawler.sc	
+++ b/13.4 - RecursiveCrawler/TestCrawler.sc	
@@ -1,9 +1,9 @@
 import $file.Crawler, Crawler._
 
-val depth0Results = pprint.log(fetchAllLinksParallel("Singapore", 0))
-val depth1Results = pprint.log(fetchAllLinksParallel("Singapore", 1))
-val depth2Results = pprint.log(fetchAllLinksParallel("Singapore", 2))
-val depth3Results = pprint.log(fetchAllLinksParallel("Singapore", 3))
+val depth0Results = pprint.log(fetchAllLinksRec("Singapore", 0))
+val depth1Results = pprint.log(fetchAllLinksRec("Singapore", 1))
+val depth2Results = pprint.log(fetchAllLinksRec("Singapore", 2))
+val depth3Results = pprint.log(fetchAllLinksRec("Singapore", 3))
 
 pprint.log(depth0Results.size)
 pprint.log(depth1Results.size)
```