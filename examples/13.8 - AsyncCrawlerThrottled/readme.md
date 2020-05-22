```bash
amm --class-based TestCrawler.sc
```

Diff from [13.5 - AsyncCrawler](https://github.com/handsonscala/handsonscala/tree/master/examples/13.13.5%20-%20AsyncCrawler):
```diff
diff --git a/13.5 - AsyncCrawler/Crawler.sc b/13.8 - AsyncCrawlerThrottled/Crawler.sc
index 568f1d1..8dfec48 100644
--- a/13.5 - AsyncCrawler/Crawler.sc	
+++ b/13.8 - AsyncCrawlerThrottled/Crawler.sc	
@@ -1,19 +1,25 @@
 import $file.FetchLinksAsync, FetchLinksAsync._
-import scala.concurrent._, ExecutionContext.Implicits.global
+import scala.concurrent._, ExecutionContext.Implicits.global, duration.Duration.Inf
 
-def fetchAllLinksAsync(startTitle: String, depth: Int): Future[Set[String]] = {
-  def rec(current: Set[String], seen: Set[String], recDepth: Int): Future[Set[String]] = {
-    if (recDepth >= depth) Future.successful(seen)
+def fetchAllLinksAsync(startTitle: String, maxDepth: Int, maxConcurrency: Int): Future[Set[String]] = {
+  def rec(current: Seq[(String, Int)], seen: Set[String]): Future[Set[String]] = {
+    pprint.log((maxDepth, current.size, seen.size))
+    if (current.isEmpty) Future.successful(seen)
     else {
-      val futures = for (title <- current) yield fetchLinksAsync(title)
+      val (throttled, remaining) = current.splitAt(maxConcurrency)
+      val futures =
+        for ((title, depth) <- throttled)
+        yield fetchLinksAsync(title).map((_, depth))
+
       Future.sequence(futures).map{nextTitleLists =>
-        rec(
-          nextTitleLists.flatten.filter(!seen.contains(_)),
-          seen ++ nextTitleLists.flatten,
-          recDepth + 1
-        )
+        val flattened = for{
+          (titles, depth) <- nextTitleLists
+          title <- titles
+          if !seen.contains(title) && depth < maxDepth
+        } yield (title, depth + 1)
+        rec(remaining ++ flattened, seen ++ flattened.map(_._1))
       }.flatten
     }
   }
-  rec(Set(startTitle), Set(startTitle), 0)
+  rec(Seq(startTitle -> 0), Set(startTitle))
 }
diff --git a/13.5 - AsyncCrawler/TestCrawler.sc b/13.8 - AsyncCrawlerThrottled/TestCrawler.sc
index 52e0a01..9770648 100644
--- a/13.5 - AsyncCrawler/TestCrawler.sc	
+++ b/13.8 - AsyncCrawlerThrottled/TestCrawler.sc	
@@ -1,10 +1,10 @@
 import $file.Crawler, Crawler._
 import scala.concurrent._, duration.Duration.Inf
 
-val depth0Results = pprint.log(Await.result(fetchAllLinksAsync("Singapore", 0), Inf))
-val depth1Results = pprint.log(Await.result(fetchAllLinksAsync("Singapore", 1), Inf))
-val depth2Results = pprint.log(Await.result(fetchAllLinksAsync("Singapore", 2), Inf))
-val depth3Results = pprint.log(Await.result(fetchAllLinksAsync("Singapore", 3), Inf))
+val depth0Results = pprint.log(Await.result(fetchAllLinksAsync("Singapore", 0, 16), Inf))
+val depth1Results = pprint.log(Await.result(fetchAllLinksAsync("Singapore", 1, 16), Inf))
+val depth2Results = pprint.log(Await.result(fetchAllLinksAsync("Singapore", 2, 16), Inf))
+val depth3Results = pprint.log(Await.result(fetchAllLinksAsync("Singapore", 3, 16), Inf))
 
 pprint.log(depth0Results.size)
 pprint.log(depth1Results.size)
```