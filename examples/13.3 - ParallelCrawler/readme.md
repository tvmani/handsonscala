```bash
amm --class-based TestCrawler.sc
```

Diff from [13.2 - Crawler](https://github.com/handsonscala/handsonscala/tree/master/examples/13.13.2%20-%20Crawler):
```diff
diff --git a/13.2 - Crawler/Crawler.sc b/13.3 - ParallelCrawler/Crawler.sc
index d03a105..59513c7 100644
--- a/13.2 - Crawler/Crawler.sc	
+++ b/13.3 - ParallelCrawler/Crawler.sc	
@@ -1,10 +1,12 @@
 import $file.FetchLinks, FetchLinks._
+import scala.concurrent._, ExecutionContext.Implicits.global, duration.Duration.Inf
 
-def fetchAllLinks(startTitle: String, depth: Int): Set[String] = {
+def fetchAllLinksParallel(startTitle: String, depth: Int): Set[String] = {
   var seen = Set(startTitle)
   var current = Set(startTitle)
   for (i <- Range(0, depth)) {
-    val nextTitleLists = for (title <- current) yield fetchLinks(title)
+    val futures = for (title <- current) yield Future{ fetchLinks(title) }
+    val nextTitleLists = futures.map(Await.result(_, Inf))
     current = nextTitleLists.flatten.filter(!seen.contains(_))
     seen = seen ++ current
   }
diff --git a/13.2 - Crawler/TestCrawler.sc b/13.3 - ParallelCrawler/TestCrawler.sc
index 20a2753..687b7f7 100644
--- a/13.2 - Crawler/TestCrawler.sc	
+++ b/13.3 - ParallelCrawler/TestCrawler.sc	
@@ -1,9 +1,9 @@
 import $file.Crawler, Crawler._
 
-val depth0Results = pprint.log(fetchAllLinks("Singapore", 0))
-val depth1Results = pprint.log(fetchAllLinks("Singapore", 1))
-val depth2Results = pprint.log(fetchAllLinks("Singapore", 2))
-val depth3Results = pprint.log(fetchAllLinks("Singapore", 3))
+val depth0Results = pprint.log(fetchAllLinksParallel("Singapore", 0))
+val depth1Results = pprint.log(fetchAllLinksParallel("Singapore", 1))
+val depth2Results = pprint.log(fetchAllLinksParallel("Singapore", 2))
+val depth3Results = pprint.log(fetchAllLinksParallel("Singapore", 3))
 
 pprint.log(depth0Results.size)
 pprint.log(depth1Results.size)
```