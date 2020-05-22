```bash
amm StreamingDownloadProcessReupload.sc

ls -lh base64.gz
```


Diff from [7.6 - StreamingDownloadProcessReupload2](https://github.com/handsonscala/handsonscala/tree/master/examples/7.7.6%20-%20StreamingDownloadProcessReupload2):
```diff
diff --git a/7.6 - StreamingDownloadProcessReupload2/StreamingDownloadProcessReupload.sc b/7.8 - StreamingDownloadProcessReuploadTee/StreamingDownloadProcessReupload.sc
index 142abbb..bb550a0 100644
--- a/7.6 - StreamingDownloadProcessReupload2/StreamingDownloadProcessReupload.sc	
+++ b/7.8 - StreamingDownloadProcessReuploadTee/StreamingDownloadProcessReupload.sc	
@@ -1,9 +1,10 @@
 val download = os.proc( "curl", "https://api.github.com/repos/lihaoyi/mill/releases").spawn()
 val base64 = os.proc("base64").spawn(stdin = download.stdout)
 val gzip = os.proc("gzip").spawn(stdin = base64.stdout)
+val tee = os.proc("tee", "base64.gz").spawn(stdin = gzip.stdout)
 val upload = os
   .proc("curl", "-X", "PUT", "-d", "@-", "https://httpbin.org/anything")
-  .spawn(stdin = gzip.stdout)
+  .spawn(stdin = tee.stdout)
 
 val contentLength = upload.stdout.lines.filter(_.contains("Content-Length"))
 pprint.log(contentLength)
```