+          for (error <- errorOpt) yield i(color.red)(error),
           form(action := "/", method := "post")(