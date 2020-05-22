```bash
amm StreamingDownloadProcessReupload.sc
```


Diff from [7.5 - StreamingDownloadProcessReupload1](https://github.com/handsonscala/handsonscala/tree/master/examples/7.7.5%20-%20StreamingDownloadProcessReupload1):
```diff
diff --git a/7.5 - StreamingDownloadProcessReupload1/StreamingDownloadProcessReupload.sc b/7.6 - StreamingDownloadProcessReupload2/StreamingDownloadProcessReupload.sc
index a088334..142abbb 100644
--- a/7.5 - StreamingDownloadProcessReupload1/StreamingDownloadProcessReupload.sc	
+++ b/7.6 - StreamingDownloadProcessReupload2/StreamingDownloadProcessReupload.sc	
@@ -1,5 +1,6 @@
 val download = os.proc( "curl", "https://api.github.com/repos/lihaoyi/mill/releases").spawn()
-val gzip = os.proc("gzip").spawn(stdin = download.stdout)
+val base64 = os.proc("base64").spawn(stdin = download.stdout)
+val gzip = os.proc("gzip").spawn(stdin = base64.stdout)
 val upload = os
   .proc("curl", "-X", "PUT", "-d", "@-", "https://httpbin.org/anything")
   .spawn(stdin = gzip.stdout)
```